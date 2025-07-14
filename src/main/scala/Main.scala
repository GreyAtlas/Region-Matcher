package com.regionmatcher

import types.Latitude
import types.Longitude
import types.Point
import types.Polygon

import types.Location
import types.Region
import modules.solvers.RayCast2DSolver
import types.Line
import modules.IO.IOHandler
import modules.json.CirceJsonParser
import types.LocationMatchResult

def parseArgs(list: List[String]): Map[String, String] = list match {
  case key :: value :: tail if key.startsWith("--") =>
    parseArgs(tail) + (key.drop(2) -> value)
  case Nil => Map()
  case _   => throw new IllegalArgumentException("Invalid arguments")
}
val locationsPattern = "[--locations | --l <location-path>]"
val regionPattern = "[--locations | --l <location-path>]"
val outputPattern = "[--locations | --l <location-path>]"
val usage = s"""
  Usage: $locationsPattern $regionPattern $outputPattern
"""

def handleError(error: Error): Nothing = sys.exit(1);

@main def Main(args: String*): Unit =
  if (args.isEmpty) {
    println(usage)
    sys.exit(1)
  }

  val argMap = Map.newBuilder[String, String]
  args.sliding(2, 2).toList.collect {
    case Seq("--locations" | "--l", locations: String) =>
      argMap.+=("locationsPath" -> locations)
    case Seq("--regions" | "--r", regions: String) =>
      argMap.+=("regionsPath" -> regions)
    case Seq("--output" | "--o", output: String) =>
      argMap.+=("outputPath" -> output)
  }
  val locationsArg: Option[String] = argMap.result().get("locationsPath")
  val regionsArg: Option[String] = argMap.result().get("regionsPath")
  val outputArg: Option[String] = argMap.result().get("outputPath")

  val locationsJson: Either[Error, String] = locationsArg match {
    case Some(locations) => Right(IOHandler.readFileIntoMemory(locations))
    case None => Left(Error(s"locations file path required. $locationsPattern"))
  }

  if locationsJson.isLeft then handleError(locationsJson.left.get)

  val regionsJson: Either[Error, String] = regionsArg match {
    case Some(regions) => Right(IOHandler.readFileIntoMemory(regions))
    case None => Left(Error(s"regions file path required. $regionPattern"))
  }
  if regionsJson.isLeft then handleError(regionsJson.left.get)

  val locationParseResult =
    CirceJsonParser.parseLocationJson(locationsJson.right.get)

  val regionParseResult =
    CirceJsonParser.parseRegionJson(regionsJson.right.get)

  val solverResult = (locationParseResult, regionParseResult) match {
    case (Right(locations: List[Location]), Right(regions: List[Region])) =>
      Right(RayCast2DSolver.matchRegionsToLocations(regions, locations))
    case (Left(locationError), _) => Left(locationError)
    case (_, Left(regionError))   => Left(regionError)
  }

  val outputPath: Either[Error, String] = outputArg match {
    case Some(outputPath) => Right(outputPath)
    case None => Left(Error(s"output file path required. $outputPattern"))
  }
  if outputPath.isLeft then handleError(outputPath.left.get)

  solverResult match {
    case Right(results: List[LocationMatchResult]) =>
      IOHandler.writeToFile(
        outputPath.right.get,
        CirceJsonParser.encodeResultsToJson(results)
      )
    case Left(error) => handleError(error)
  }
  IOHandler.writeToConsole(s"Results written to ${outputPath.right.get}")
