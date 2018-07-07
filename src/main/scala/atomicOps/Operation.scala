package atomicOps

import scala.concurrent.{ExecutionContext, Future, Promise}

object Operation {
  def perform[T](f: => T)(implicit executor: ExecutionContext): Recover[T] = {

    val promise = Promise[Results[T]]()

    Future {
      val t = f
      promise.success(Results(valueFromPerform = Some(t)))
    }.recover {
      case e: Throwable =>
        promise.success(Results(throwableFromPerform = Some(e)))
    }

    new Recover(context = Context(recovers = List.empty, future = promise.future))
  }
}
