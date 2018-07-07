import org.scalatest._
import atomicOps.Operation
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class AtomicOperationSpec extends FlatSpec with Matchers {

  "A perform" should "result in a value" in {
    import scala.concurrent.ExecutionContext.Implicits.global

    val future = Operation perform {
      0
    } recover {
      case _: Throwable =>
        0
    } toFuture

    future.onComplete {
      case Success(s) =>
        s should be(0)
      case _ =>
    }

    Await.result(future, 10 seconds)
  }

  "A recover" should "undo perform" in {
    import scala.concurrent.ExecutionContext.Implicits.global

    val future = Operation perform[Int] {
      throw new Exception("")
    } recover {
      case _: Throwable =>
        2
    } toFuture

    future.onComplete {
      case Success(s) =>
        s should be(2)
      case _ =>
    }

    Await.result(future, 10 seconds)
  }

  "A perform" should "compose with perform" in {
    import scala.concurrent.ExecutionContext.Implicits.global

    val future = Operation perform {
      0
    } recover {
      case _ =>
        0
    } perform { i: Int =>
      i + 1
    } recover {
      case _ =>
        0
    } toFuture

    future.onComplete {
      case Success(s) =>
        s should be(1)
      case _ =>
    }

    Await.result(future, 10 seconds)
  }

  "A recover" should "run before previous recovers" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    var r = collection.mutable.ListBuffer[String]("r")

    val future = Operation perform {
      0
    } recover {
      case _ =>
        r += "r1"
        1
    } perform { _: Int =>
      throw new Exception("")
    } recover {
      case _ =>
        r += "r2"
        2
    } toFuture

    future.onComplete {
      case Success(s) =>
        s should be(2)
        r should be(Seq("r", "r2", "r1"))
      case _ =>
    }

    Await.result(future, 10 seconds)
  }

  "A recover" should "run before previous recovers and not run next recovers" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    var r = collection.mutable.ListBuffer[String]("r")

    val future = Operation perform {
      0
    } recover {
      case _ =>
        r += "r1"
        1
    } perform { _: Int =>
      throw new Exception("")
    } recover {
      case _ =>
        r += "r2"
        2
    } perform { i: Int =>
      i + 1
    } recover {
      case _ =>
        r += "r3"
        3
    } toFuture

    future.onComplete {
      case Success(s) =>
        s should be(2)
        r should be(Seq("r", "r2", "r1"))
      case _ =>
    }

    Await.result(future, 10 seconds)
  }

  "An exception in recover" should " result in a failed future" in {
    import scala.concurrent.ExecutionContext.Implicits.global

    val future = Operation perform {
      throw new Exception("exception")
    } recover {
      case t: Throwable =>
        throw t
    } toFuture()

    future.onComplete {
      case Failure(t) =>
        t.getMessage should be ("exception")
      case _ =>
    }

    try {
      Await.result(future, 10 seconds)
    } catch {
      case _: Throwable =>
    }
  }
}
