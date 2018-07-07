package atomicOps

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

final class Recover[T](private[atomicOps] val context: Context[T]) {
  def recover(pf: PartialFunction[Throwable, T])(implicit executor: ExecutionContext): Perform[T] = {

    val promise = Promise[Results[T]]()

    context.future.onComplete {
      case Success(Results(_, Some(t), _)) =>
        promise.success(Results(valueFromPerform = Some(t)))
      case Success(Results(Some(e), _, _)) =>
        Future {
          val s = pf(e)
          context.recovers.foreach(r => r(e))
          promise.success(Results(valueFromRecover = Some(s)))
        }.recover {
          case e: Throwable =>
            promise.failure(e)
        }
      case Failure(e) =>
        promise.failure(e)
      case _ =>
        throw new Exception("")
    }

    val nextRecovers = if (context.recovers.isEmpty) List(pf) else pf :: context.recovers
    new Perform(context.copy(recovers = nextRecovers,  future = promise.future))
  }
}
