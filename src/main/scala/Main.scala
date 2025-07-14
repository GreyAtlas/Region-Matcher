package com.regionmatcher

import types.Latitude
import types.Longitude
import types.Point
import types.Polygon
import io.circe.parser
import io.circe.generic.semiauto.deriveDecoder

import io.circe._, io.circe.generic.auto._, io.circe.parser._
import types.Location
import types.Region
import modules.solvers.RayCast2DSolver
import types.Line
import modules.IO.IOHandler
import modules.json.CirceJsonParser
import types.LocationMatchResult

@main def Main(args: String*): Unit =
  println(args)

// println(
//   "SO CLOSE, all thats really left is making a CLI and unit tests. And of course unit tests.\n Anyways, the results are in output/results1.json"
// )

// val locationsJson =
//   IOHandler.readFileIntoMemory("input/locations.json")

// val regionsJson =
//   IOHandler.readFileIntoMemory("input/regions.json")

// val locationParseResult = CirceJsonParser.parseLocationJson(locationsJson)
// val regionParseResult = CirceJsonParser.parseRegionJson(regionsJson)

// val solverResult = (locationParseResult, regionParseResult) match {
//   case (Right(locations: List[Location]), Right(regions: List[Region])) =>
//     Right(RayCast2DSolver.matchRegionsToLocations(regions, locations))
//   case (Left(locationError: String), _) => Left(locationError)
//   case (_, Left(regionError: String))   => Left(regionError)
// }

// solverResult match {
//   case Right(results: List[LocationMatchResult]) =>
//     IOHandler.writeToFile(
//       "output/results1.json",
//       CirceJsonParser.encodeResultsToJson(results)
//     )
//   case Left(error) => IOHandler.writeToConsole(error)
// }
