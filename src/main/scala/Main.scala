import types.Latitude
import types.Longitude
import types.Point
import types.Polygon
import io.circe.parser
import io.circe.generic.semiauto.deriveDecoder

import scala.collection.mutable
import io.circe._, io.circe.generic.auto._, io.circe.parser._
import types.Location

case class Person(name: String, age: Int)
case class Staff(name: String)
object SimpleDecoder {
  def main(args: Array[String]): Unit = {
    val input =
      """
        {
          "name": "John Doe"
        }
      """.stripMargin

    val staffDecoder = deriveDecoder[Staff]
    val decodeResult = parser.decode[Staff](input)

    decodeResult match {
      case Right(staff) => println(staff.name)
      case Left(error)  => println(error.getMessage())
    }
  }
}
@main def hello(): Unit =
  println("Hello world!")
  println(msg)
  val locationsJson = os.read(os.pwd / "input" / "locations.json")
  val regionsJson = os.read(os.pwd / "input" / "regions.json")
  // val mappedArray =
  // val person: mutable.Map[String, ujson.Value] = locationsJson.obj

  val parseResult = parse(locationsJson);

  val jsonString = """{ "name": "Alice", "age": 30 }"""
  val TESTLOCATIONSTRING =
    """
    [
      {
        "name": "location1",
        "coordinates": [
          25.21051562929364,
          554.64057937965808,
          555555.55512
        ]
      }
    ]
    """.stripMargin
  val testPointString =
    """
  [
    {
      "name": "location1",
      "coordinates": [
        25.21051562929364,
        54.64057937965808
      ]
    }
  ]
  """.stripMargin
  // implicit val pointDecoder: Decoder[Point] =
  //   (hCursor: HCursor) => {
  //     for {
  //       coordinates <- hCursor.value.as[Vector[Float]]
  //       point <- hCursor.downField("coordinates").as[Point]
  //     } yield Point(, point)
  //   }
  implicit val longitudeDecoder: Decoder[Point] =
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

  // implicit val pointDecoder: Decoder[Point] =
  //   (hCursor: HCursor) => {
  //     for {
  //       coordinates <- hCursor.as[Vector[Float]]
  //     } yield Point(
  //       Longitude(coordinates(0)).getOrElse(hCursor.),
  //       Latitude(coordinates(1)).get
  //     )
  //   }
  implicit val locationDecoder: Decoder[Location] =
    (hCursor: HCursor) => {
      for {
        name <- hCursor.get[String]("name")
        point <- hCursor.downField("coordinates").as[Point]
      } yield Location(
        name,
        point
        // Point(Longitude(point(0)).get, Latitude(point(1)).get)
      )
    }
  val decodingResult = parser.decode[List[Location]](TESTLOCATIONSTRING)
  decodingResult match {
    case Right(locations) => locations.map(println)
    case Left(error)      => println(error.getMessage())
  }

  // val result: Either[Error, Person] = decode[Person](jsonString)
  // // val locationDecoder = deriveDecoder[Location]
  // val pointDecoder = deriveDecoder[Point]
  // val decodeResult = parser.decode[List[Location]](TESTLOCATIONSTRING)

  // decodeResult match {
  //   case Right(staff) => println(staff)
  //   case Left(error)  => println(error.getMessage())
  // }
  // result match {
  //   case Right(person) => println(s"Parsed person: $person")
  //   case Left(error)   => println(s"Failed to parse JSON: $error")
  // }
  println(test.getOrElse("TEST FAIL"))
  println(test1.getOrElse("TEST FAIL"))
  println(test2.getOrElse("TEST FAIL"))
  println(test3.getOrElse("TEST FAIL"))
  println(Point(test5.get, test4.get).longitude.value)
  println(
    Polygon(
      Vector(
        Point(test5.get, test.get),
        Point(test5.get, test3.get),
        Point(test6.get, test4.get),
        Point(test5.get, test.get)
      )
    ).get
  )

def msg = "I was compiled by Scala 3. :)"
def test = Latitude(15.1f)
def test1 = Latitude(-91.0f)
def test2 = Latitude(91)
def test3 = Latitude(-90)
def test4 = Latitude(90)
def test5 = Longitude(100)
def test6 = Longitude(200)
