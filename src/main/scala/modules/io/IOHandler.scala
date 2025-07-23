package modules.IO

import cats.effect.IO

object IOHandler {
  def readFileIntoMemory(path: String): IO[String] =
    IO.blocking(
      os.read(os.pwd / os.RelPath(path))
    )

  def writeToFile(path: String, results: String): IO[String] =
    IO.blocking {
      os.write(os.pwd / os.RelPath(path), results)
      s"Results were written to ${os.pwd / os.RelPath(path)}"
    }

}
