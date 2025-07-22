package modules.json

import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import modules.json.CirceJsonParser
import types.Latitude
import types.LocationMatchResult
import types.Longitude
import types.Point

class CirceJsonParserSuite extends munit.FunSuite {
  test(
    "When decoding point and coordinates are within bounds"
  ) {
    val expected = Point(Longitude(0).get, Latitude(0).get)

    val jsonString = expected.asJson.noSpaces
    val decodedPoint = parser.decode[Point](jsonString)

    decodedPoint match {
      case Left(value) =>
        fail(s"Decoding failed: ${value}")
      case Right(value) => assertEquals(value, expected)
    }
  }
  test(
    "When encoding a list of LocationMatchResult and the list is empty returns "
  ) {
    val locationMatchResults: List[LocationMatchResult] = List.empty

    val encodedToJson =
      CirceJsonParser.encodeResultsToJson(locationMatchResults)

    encodedToJson match {
      case Left(_)      => ()
      case Right(value) => fail("Method shouldn't return a Right")
    }
  }
}
