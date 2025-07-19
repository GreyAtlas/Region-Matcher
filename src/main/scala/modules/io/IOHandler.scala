package modules.IO

object IOHandler {
  def readFileIntoMemory(path: String): String =
    os.read(os.pwd / os.RelPath(path))

  def writeToFile(path: String, results: String): Unit =
    os.write(os.pwd / os.RelPath(path), results)

  def writeToConsole(contentsToWrite: String): Unit =
    println(contentsToWrite)

  def parseArgs(list: List[String]): Map[String, String] = list match {
    case key :: value :: tail if key.startsWith("--") =>
      parseArgs(tail) + (key.drop(2) -> value)
    case Nil => Map()
    case _   => throw new IllegalArgumentException("Invalid arguments")
  }

}
