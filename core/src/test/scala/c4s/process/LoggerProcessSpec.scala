package c4s.process

import cats.effect._
import org.typelevel.log4cats.testing.TestingLogger
import org.typelevel.log4cats.testing.TestingLogger._
import munit.CatsEffectSuite
import cats.effect.Resource

class LoggerProcessSpec extends CatsEffectSuite {
  import c4s.process.syntax._

  test("It should log what we are doing and still getting the command when it is succeeded") {
    withProcess.use { case (shell, logger) =>
      for {
        output <- Process.run("ls -la")(shell).string
        log <- logger.logged
      } yield assert(
        log
          .collect { case INFO(message, _) => message }
          .contains(output)
      )
    }
  }

  test("It should log what we are doing and still getting the command when it fails") {
    withProcess.use { case (shell, logger) =>
      for {
        result <- Process.run("ls foo")(shell)
        error <- result.error.asString
        log <- logger.logged
      } yield assert(
        log
          .collect { case ERROR(message, _) => message }
          .contains(error)
      )
    }
  }

  def withProcess: Resource[IO, (Process[IO], TestingLogger[IO])] = {
    val logger = TestingLogger.impl[IO]()
    Resource.unit[IO]
      .map(x => (Process.impl[IO](x).withLogger(logger), logger))
  }

}
