package atomicOps

import scala.concurrent.Future

private[atomicOps] case class Context[T](recovers: List[PartialFunction[Throwable, T]],
                                         future: Future[Results[T]])
