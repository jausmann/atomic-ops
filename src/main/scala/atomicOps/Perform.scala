package atomicOps

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

final class Perform[T](private val context: Context[T]) {
  def perform(f: T => T)(implicit executor: ExecutionContext): Recover[T] = {

    val promise = Promise[Results[T]]()

    context.future.onComplete {
      case Success(Results(_, Some(t), _)) =>
        Try {
          promise.success(Results(valueFromPerform = Some(f(t))))
        }.recover {
          case e: Throwable =>
            promise.success(Results(throwableFromPerform = Some(e)))
        }
      case Success(Results(_, _, Some(t))) =>
        promise.success(Results(valueFromPerform = Some(t)))
      case Failure(t) =>
        promise.failure(t)
      case _ =>
        throw new Exception("")
    }

    new Recover(context.copy(future = promise.future))
  }

  def toFuture()(implicit executor: ExecutionContext): Future[T] = {
    val promise = Promise[T]()

    context.future.onComplete {
      case Success(Results(_, Some(t), _)) =>
        promise.success(t)
      case Success(Results(_, _, Some(t))) =>
        promise.success(t)
      case Failure(e) =>
        promise.failure(e)
      case _ =>
        throw new Exception("")
    }

    promise.future
  }
}
