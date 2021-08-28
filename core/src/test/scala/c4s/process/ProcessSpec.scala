package c4s.process

import java.nio.file._
import java.util.Comparator

import cats.effect._
import cats.syntax.all._
import munit.CatsEffectSuite
import java.io.File
import cats.effect.Resource

class ProcessSpec extends CatsEffectSuite {
  import c4s.process.syntax._

  test("It should run commands") {
    withProcess.use(implicit shell =>
      Process
        .run("ls -la")
        .map(x => assertEquals(x.exitCode, ExitCode.Success))
    )
  }

  test("It should strict run commands") {
    withProcess.use(implicit shell =>
      Process
        .run("ls -la")
        .strict
        .map(x => assertEquals(x._1, ExitCode.Success))
    )
  }

  test("It should strict run commands when it is failing") {
    withProcess.use(implicit shell =>
      Process
        .run("ls foo")
        .strict
        .attempt
        .map(x =>
          assert(
            x.swap.exists(_.getMessage().startsWith("Failed to execute command")),
            "It should failed to execute command"
          )
        )
    )
  }

  test("It should get the output as string and execute commands in different folder") {
    withProcess.use { implicit shell =>
      createTmpDirectory[IO].use { path =>
        Process.runInPath("mkdir foo", path) >>
          Process.runInPath("ls", path).string.map { output =>
            assert(output.contains("foo"), "foo")
          }
      }
    }
  }

  test("It should get lines and execute commands in different folder") {
    withProcess.use { implicit shell =>
      createTmpDirectory[IO].use { path =>
        Process.runInPath("mkdir foo", path) >>
          Process.runInPath("mkdir bar", path) >>
          Process.runInPath("ls", path).lines.map { output =>
            assertEquals(output, List("bar", "foo"))
          }
      }
    }
  }

  test("It should run command reading a stream from another command") {
    withProcess.use { implicit shell =>
      createTmpDirectory[IO].use { path =>
        Process.runInPath("touch test-file", path) >>
          Process.runInPath("ls", path) >>= (result =>
          Process
            .run("wc", result.output)
            .string
            .map(resultStream =>
              assertEquals(
                resultStream
                  .replaceAll(" ", "")
                  .trim
                  .toInt,
                1110
              )
            )
        )
      }
    }
  }

  val withProcess: Resource[IO, Process[IO]] =
    Resource.unit[IO]
      .map(x => Process.impl[IO](x))

  def createTmpDirectory[F[_]: Sync]: Resource[F, Path] =
    Resource.make(Sync[F].delay(Files.createTempDirectory("tmp")))(path =>
      Sync[F].delay {
        Files.walk(path).sorted(Comparator.reverseOrder()).map[File](_.toFile).map[Boolean](_.delete())
      }.void
    )

}
