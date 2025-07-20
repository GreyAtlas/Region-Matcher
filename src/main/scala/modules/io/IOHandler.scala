package modules.IO

import cats.effect.IO

object IOHandler {
  def readFileIntoMemory(path: String): IO[Either[String, String]] =
    IO.blocking(
      Right(os.read(os.pwd / os.RelPath(path)))
    ).handleError(error => Left(s"Error while reading file: ${error}"))

  def writeToFile(path: String, results: String): IO[Either[String, String]] =
    IO.blocking {
      os.write(os.pwd / os.RelPath(path), results)
      Right(s"Results written to ${os.pwd / os.RelPath(path)}")
    }.handleError(error => Left(s"Error while writing to file: ${error}"))

}
