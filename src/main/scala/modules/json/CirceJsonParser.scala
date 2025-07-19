package modules.json

import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*
import types.Latitude
import types.Location
import types.LocationMatchResult
import types.Longitude
import types.Point
import types.Polygon
import types.Region

object CirceJsonParser {

  def encodeResultsToJson(content: List[LocationMatchResult]): String =
    content.asJson.toString

  def parseLocationJson(
      locationsJsonFile: String
  ): Either[java.lang.Error, List[Location]] =
    parser.decode[List[Location]](locationsJsonFile) match {
      case Right(locations) => Right(locations)
      case Left(error)      => Left(java.lang.Error(error.getMessage()))
    }

  def parseRegionJson(
      regionsJsonFile: String
  ): Either[java.lang.Error, List[Region]] =
    parser.decode[List[Region]](regionsJsonFile) match {
      case Right(regions) => Right(regions)
      case Left(error)    => Left(java.lang.Error(error.getMessage()))
    }

  implicit val locationMatchResultEncoder: Encoder[LocationMatchResult] =
    new Encoder[LocationMatchResult] {
      final def apply(result: LocationMatchResult): Json = Json.obj(
        ("region", result.regionName.asJson),
        ("matched_locations", result.matchedLocationNames.asJson)
      )
    }
  implicit val pointDecoder: Decoder[Point] =
    (hCursor: HCursor) => {
      for {
        arr <- hCursor.as[Vector[Float]]
        res <- arr match {
          case Vector(longitude, latitude) =>
            for {
              longitudeResult <- Longitude(longitude).toRight(
                DecodingFailure(
                  s"longitude must be a float between 0 and 360, found=$longitude",
                  hCursor.history
                )
              )
              latitudeResult <- Latitude(latitude).toRight(
                DecodingFailure(
                  s"latitude must be a float between -90 and 90, found=$latitude",
                  hCursor.history
                )
              )
            } yield Point(longitudeResult, latitudeResult)

          case _ =>
            Left(
              DecodingFailure(
                "Expected JSON array of exactly two floats",
                hCursor.history
              )
            )

        }
      } yield res
    }

  implicit val locationDecoder: Decoder[Location] =
    (hCursor: HCursor) => {
      for {
        name <- hCursor.get[String]("name")
        point <- hCursor.downField("coordinates").as[Point]
      } yield Location(
        name,
        point
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

  implicit val regionDecoder: Decoder[Region] =
    (hCursor: HCursor) => {
      for {
        name <- hCursor.get[String]("name")
        polygon <- hCursor.downField("coordinates").as[Vector[Polygon]]
      } yield Region(
        name,
        polygon
      )
    }
}
