package modules.json

import cats.effect.IO
import io.circe.*
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
  ): IO[Either[String, String]] =
    content match {
      case head :: next => IO(Right(content.asJson.toString))
      case Nil          => IO(Left(s"Region matching result list is empty"))
    }

  def parseLocationJson(
      locationsJsonFile: String
  ): IO[Either[String, List[Location]]] =
    parser.decode[List[Location]](locationsJsonFile) match {
      case Right(locations) => IO(Right(locations))
      case Left(error)      => IO(Left(s"Error while parsing JSON: ${error}"))
    }

  def parseRegionJson(
      regionsJsonFile: String
  ): IO[Either[String, List[Region]]] =
    parser.decode[List[Region]](regionsJsonFile) match {
      case Right(regions) => IO(Right(regions))
      case Left(error)    => IO(Left(s"Error while parsing JSON: ${error}"))
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
                  s"longitude must be a float between -180 and 180, found=$longitude",
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
