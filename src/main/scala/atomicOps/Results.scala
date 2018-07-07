package atomicOps

case class Results[T](throwableFromPerform: Option[Throwable] = None,
                      valueFromPerform: Option[T] = None,
                      valueFromRecover: Option[T] = None)
