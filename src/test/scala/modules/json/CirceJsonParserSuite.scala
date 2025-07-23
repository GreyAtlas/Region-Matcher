package modules.json

import io.circe.*
import modules.json.CirceJsonParser
import types.Latitude
import types.LocationMatchResult
import types.Longitude
import types.Point
import types.Polygon

class CirceJsonParserSuite extends munit.FunSuite {
  test(
    "When decoding longitude and decoded coordinate is within bounds"
  ) {
    val expected = Longitude(0.41561616)
    val jsonString = """0.41561616"""

    val decodedLongitude =
      parser.decode[Longitude](jsonString)(using
        CirceJsonParser.longitudeDecoder
      )

    (expected, decodedLongitude) match {
      case (_, Left(error)) =>
        fail(s"Decoding failed: ${error}")
      case (None, _) => fail("The expected point is incorrectly formated")
      case (Some(expected), Right(obtained)) =>
        assertEquals(obtained, expected)
    }
  }
  test(
    "When decoding longitude and decoded coordinate is outside of bounds"
  ) {
    val jsonString = """-360.41561616"""

    val decodedLongitude =
      parser.decode[Longitude](jsonString)(using
        CirceJsonParser.longitudeDecoder
      )

    decodedLongitude match {
      case Left(error: DecodingFailure) =>
        ()
      case Left(parsingError: ParsingFailure) =>
        fail("Json should be parsed correctly", clues(decodedLongitude))
      case Right(obtained) =>
        fail(s" Should not return a right ", clues(decodedLongitude))
    }
  }
  test(
    "When decoding latitude and decoded coordinate is within bounds"
  ) {
    val expected = Latitude(0.41561616)
    val jsonString = """0.41561616"""

    val decodedLatitude =
      parser.decode[Latitude](jsonString)(using
        CirceJsonParser.latitudeDecoder
      )

    (expected, decodedLatitude) match {
      case (_, Left(error: DecodingFailure)) =>
        fail(s"Failed when decoding json: ${error}")
      case (_, Left(error: ParsingFailure)) =>
        fail(s"Failed when parsing json ${error}")
      case (None, _) =>
        fail("The expected point is incorrectly formated", clues(expected))
      case (Some(expected), Right(obtained)) =>
        assertEquals(obtained, expected)
    }
  }
  test(
    "When decoding latitude and decoded coordinate is outside of bounds"
  ) {
    val jsonString = """-360.41561616"""

    val decodedLatitude =
      parser.decode[Latitude](jsonString)(using
        CirceJsonParser.latitudeDecoder
      )

    decodedLatitude match {
      case Left(error: DecodingFailure) =>
        ()
      case Left(parsingError: ParsingFailure) =>
        fail("Json should be parsed correctly", clues(decodedLatitude))
      case Right(obtained) =>
        fail(s" Should not return a right ", clues(decodedLatitude))
    }
  }

  test(
    "When decoding point and coordinates are within bounds"
  ) {
    val expected = Point(0, 0)

    val jsonString = """[0.0,0.0]"""
    val decodedPoint =
      parser.decode[Point](jsonString)(using CirceJsonParser.pointDecoder)

    (expected, decodedPoint) match {
      case (_, Left(error: DecodingFailure)) =>
        fail(s"Failed when decoding json: ${error}")
      case (_, Left(error: ParsingFailure)) =>
        fail(s"Failed when parsing json ${error}")
      case (None, _) =>
        fail("The expected point is incorrectly formated", clues(expected))
      case (Some(expected), Right(obtained)) => assertEquals(obtained, expected)

    }
  }
  val pointDecodingFailureCases = List(
    ("Longitude out of bounds", "[-360,0]"),
    ("Longitude out of bounds", "[360,0]"),
    ("Latitude out of bounds", "[0,-180]"),
    ("Latitude out of bounds", "[0,180]"),
    ("Both coordinates are out of bounds", "[-360,-180]"),
    ("Both coordinates are out of bounds", "[360,180]")
  )
  pointDecodingFailureCases.foreach { case (name, jsonString) =>
    test(
      s"Out of bounds point decoding test: $name"
    ) {
      val decodedPoint =
        parser.decode[Point](jsonString)(using CirceJsonParser.pointDecoder)

      decodedPoint match {
        case Left(error: DecodingFailure) =>
          ()
        case Left(parsingError: ParsingFailure) =>
          fail("Json should be parsed correctly", clues(decodedPoint))
        case Right(obtained) =>
          fail("Decoding shouldn't return a right", clues(decodedPoint))

      }
    }
  }
  val polygonDecodingFailureCases = List(
    (
      "List contains less than 3 distinct vertices",
      """    
      [
        [
          23.728463251292055,
          54.85806510526285
        ],
        [
          23.834518591254124,
          54.815780434232124
        ],
        [
          23.728463251292055,
          54.85806510526285
        ]
      ]
    """
    ),
    (
      "Last vertex doesn't equal the first",
      """    
      [
        [
          23.728463251292055,
          54.85806510526285
        ],
        [
          23.834518591254124,
          54.815780434232124
        ],
        [
          24.02623401349132,
          54.815780434232124
        ],
        [
          -23.728463251292055,
          -54.85806510526285
        ]
      ]
    """
    )
  )
  polygonDecodingFailureCases.foreach { case (name, jsonString) =>
    test(
      s"Out of bounds polygon decoding test: $name"
    ) {
      val decodedPolygon =
        parser.decode[Polygon](jsonString)(using CirceJsonParser.polygonDecoder)

      decodedPolygon match {
        case Left(error: DecodingFailure) =>
          ()
        case Left(parsingError: ParsingFailure) =>
          fail("Json should be parsed correctly", clues(decodedPolygon))
        case Right(obtained) =>
          fail("Decoding shouldn't return a right", clues(decodedPolygon))

      }
    }
  }

  test(
    "When encoding a list of LocationMatchResult and the list is empty"
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
