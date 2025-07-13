package modules.IO

import os.Path
import types.LocationMatchResult

object IOHandler {
  def readFileIntoMemory(path: String): String =
    os.read(os.pwd / os.RelPath(path))

  def writeToFile(path: String, results: String) =
    os.write(os.pwd / os.RelPath(path), results)

  def writeToConsole(contentsToWrite: String) =
    println(contentsToWrite)

}
