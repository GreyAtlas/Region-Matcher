package modules.json

import cats.syntax.all.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import types.Latitude
import types.Location
import types.LocationMatchResult
import types.Longitude
import types.Point
import types.Polygon
import types.Region

object CirceJsonParser {

  def encodeResultsToJson(
      content: List[LocationMatchResult]
  ): Either[String, String] =
    content match {
      case head :: next => Right(content.asJson.toString)
      case Nil          => Left(s"Region matching result list is empty")
    }

  def parseLocationJson(
      locationsJsonFile: String
  ): Either[String, List[Location]] =
    parser.decode[List[Location]](locationsJsonFile) match {
      case Right(locations) => Right(locations)
      case Left(error) => Left(s"Error while parsing Locations JSON: ${error}")
    }

  def parseRegionJson(
      regionsJsonFile: String
  ): Either[String, List[Region]] =
    parser.decode[List[Region]](regionsJsonFile) match {
      case Right(regions) => Right(regions)
      case Left(error)    => Left(s"Error while parsing Regions JSON: ${error}")
    }

  implicit val locationMatchResultEncoder: Encoder[LocationMatchResult] =
    new Encoder[LocationMatchResult] {
      final def apply(result: LocationMatchResult): Json = Json.obj(
        ("region", result.regionName.asJson),
        ("matched_locations", result.matchedLocationNames.asJson)
      )
    }

  implicit val longitudeDecoder: Decoder[Longitude] =
    (hCursor: HCursor) => {
      hCursor.as[Float].flatMap { case longitude =>
        Longitude(longitude).toRight(
          DecodingFailure(
            s"longitude must be a float between -180 and 180, found=${longitude}",
            hCursor.history
          )
        )
      }
    }

  implicit val latitudeDecoder: Decoder[Latitude] =
    (hCursor: HCursor) => {
      hCursor.as[Float].flatMap { case latitude =>
        Latitude(latitude).toRight(
          DecodingFailure(
            s"longitude must be a float between -90 and 90, found=${latitude}",
            hCursor.history
          )
        )
      }
    }

  implicit val pointDecoder: Decoder[Point] =
    (hCursor: HCursor) => {
      hCursor
        .as[(Longitude, Latitude)]
        .flatMap { case (longitude, latitude) =>
          Right(Point(longitude, latitude))
        }
        .leftMap(error =>
          DecodingFailure(
            s"Expected JSON array of exactly two floats found =  ${hCursor.value.noSpaces}",
            hCursor.history
          )
        )

    }

  implicit val polygonDecoder: Decoder[Polygon] =
    (hCursor: HCursor) => {
      for {
        arr <- hCursor.as[Vector[Point]]
        polygonResult <- Polygon(arr).toRight(
          DecodingFailure(
            "Polygon is incorrectly formatted, last coordinate must match the first and polygon must include at least 3 vertices ",
            hCursor.history
          )
        )

      } yield polygonResult
    }

}
