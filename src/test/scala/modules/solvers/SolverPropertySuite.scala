import modules.solvers.Solver
import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.*
import types.Latitude
import types.Location
import types.LocationMatchResult
import types.Longitude
import types.Point
import types.Polygon
import types.Region

trait SolverPropertySuite extends ScalaCheckSuite {

  def solverInstance: Solver

  val rectangleAcrossAntimeridian = Region(
    "region1",
    Vector[Polygon](
      Polygon(
        Vector[Point](
          Point(Longitude(170).get, Latitude(10).get),
          Point(Longitude(170).get, Latitude(-10).get),
          Point(Longitude(-170).get, Latitude(-10).get),
          Point(Longitude(-170).get, Latitude(10).get),
          Point(Longitude(170).get, Latitude(10).get)
        )
      ).get
    )
  )
  def pointGenerator(
      longitudeBound: (Float, Float),
      latitudeBound: (Float, Float)
  ): Gen[Option[Point]] =
    for {
      longitude <- Gen.choose(longitudeBound._1, longitudeBound._2)
      latitude <- Gen.choose(latitudeBound._1, latitudeBound._2)
    } yield (
      Point(Longitude(longitude), Latitude(latitude))
    )

  def pointRectGen(
      longitudeBound: (Float, Float),
      latitudeBound: (Float, Float)
  ): Gen[(Location, Region, List[LocationMatchResult])] =
    pointGenerator(
      (longitudeBound._1, longitudeBound._2),
      (latitudeBound._1, latitudeBound._2)
    ).flatMap {
      case Some(point) => {
        val location = Location("location", point)
        (
          location,
          rectangleAcrossAntimeridian,
          List(
            LocationMatchResult(
              rectangleAcrossAntimeridian.name,
              List(location.name)
            )
          )
        )
      }
      case None => Gen.fail
    }

  val pointInsideRectGen: Gen[(Location, Region, List[LocationMatchResult])] =
    pointRectGen((170f + 0.01f, 180f), (-10f + 0.01f, 10f - 0.01f))

  val pointOutsideRectGen: Gen[(Location, Region, List[LocationMatchResult])] =
    pointRectGen((-180f, 170f - 0.01f), (-10f + 0.01f, 10f - 0.01f))

  property(
    "Location inside rectangle region should return location name in match result"
  ) {
    forAll(pointInsideRectGen) { case (location, region, locationMatchResult) =>
      val result = solverInstance.matchRegionsToLocations(
        List[Region](region),
        List[Location](location)
      )
      result match {
        case Left(error) =>
          fail("Error occured during solving", clues(error, result))
        case Right(result) => assertEquals(result, locationMatchResult)
      }

    }
  }
}
