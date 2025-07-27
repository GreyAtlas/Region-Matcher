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

  val coordinateTolerance = 1e-5f
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
  val rectangleAcrossPrimeMeridian = Region(
    "region1",
    Vector[Polygon](
      Polygon(
        Vector[Point](
          Point(Longitude(10).get, Latitude(10).get),
          Point(Longitude(10).get, Latitude(-10).get),
          Point(Longitude(-10).get, Latitude(-10).get),
          Point(Longitude(-10).get, Latitude(10).get),
          Point(Longitude(10).get, Latitude(10).get)
        )
      ).get
    )
  )
  val longNorthernRectangle = Region(
    "region1",
    Vector[Polygon](
      Polygon(
        Vector[Point](
          Point(Longitude(-180).get, Latitude(70).get),
          Point(Longitude(-180).get, Latitude(10).get),
          Point(Longitude(0).get, Latitude(10).get),
          Point(Longitude(0).get, Latitude(70).get),
          Point(Longitude(-180).get, Latitude(70).get)
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

  def testParameterGenerator(
      longitudeBound: (Float, Float),
      latitudeBound: (Float, Float),
      region: Region
  ): Gen[(Location, Region, List[LocationMatchResult])] =
    pointGenerator(
      (longitudeBound._1, longitudeBound._2),
      (latitudeBound._1, latitudeBound._2)
    ).flatMap {
      case Some(point) => {
        val location = Location("location", point)
        (
          location,
          region,
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

  val pointCloseToOutsideEquatorEdgeOfLongRectangle
      : Gen[(Location, Region, List[LocationMatchResult])] =
    testParameterGenerator(
      (-180f, 0f),
      (10f - coordinateTolerance, 10f - coordinateTolerance),
      longNorthernRectangle
    )
  val pointCloseToOutsidePolarEdgeOfLongRectangle
      : Gen[(Location, Region, List[LocationMatchResult])] =
    testParameterGenerator(
      (-180f, 0f),
      (70f + coordinateTolerance, 70f + coordinateTolerance),
      longNorthernRectangle
    )
  val pointOutsidePrimeMeridianGen
      : Gen[(Location, Region, List[LocationMatchResult])] =
    Gen.oneOf(
      testParameterGenerator(
        (-180f, -10f + coordinateTolerance),
        (-90f, 90f),
        rectangleAcrossPrimeMeridian
      ),
      testParameterGenerator(
        (10f + coordinateTolerance, 180f),
        (-90f, 90f),
        rectangleAcrossPrimeMeridian
      )
    )
  val pointOutsideAntimeridianRectGen
      : Gen[(Location, Region, List[LocationMatchResult])] =
    testParameterGenerator(
      (-170f + coordinateTolerance, 170f - coordinateTolerance),
      (-90f, 90f),
      rectangleAcrossAntimeridian
    )
  val generatedResultCannotEqualCases = List(
    ("Location outside prime meridian rectangle", pointOutsidePrimeMeridianGen),
    (
      "Location close the equator facing outside edge of long rectangle",
      pointCloseToOutsideEquatorEdgeOfLongRectangle
    ),
    (
      "Location close to the pole facing outside edge of long rectangle",
      pointCloseToOutsidePolarEdgeOfLongRectangle
    ),
    (
      "Location outside antimeridian rectangle",
      pointOutsideAntimeridianRectGen
    )
  )

  generatedResultCannotEqualCases.foreach { case (name, pointGenerator) =>
    property(
      s"Generated result should not equal solver result: ${name}"
    ) {
      forAll(pointGenerator) { case (location, region, locationMatchResult) =>
        val result = solverInstance.matchRegionsToLocations(
          List[Region](region),
          List[Location](location)
        )
        result match {
          case Left(error) =>
            fail("Error occured during solving", clues(error, result))
          case Right(result: List[LocationMatchResult]) =>
            assertNotEquals(result, locationMatchResult)
        }
      }
    }
  }
  val pointInsidePrimeMeridianRectGen
      : Gen[(Location, Region, List[LocationMatchResult])] =
    testParameterGenerator(
      (-10f + coordinateTolerance, 10f - coordinateTolerance),
      (-10f + coordinateTolerance, 10f - coordinateTolerance),
      rectangleAcrossPrimeMeridian
    )

  val pointInsideNegativeAntimeridianRectGen
      : Gen[(Location, Region, List[LocationMatchResult])] =
    testParameterGenerator(
      (-180f, -170f - coordinateTolerance),
      (-10f + coordinateTolerance, 10f - coordinateTolerance),
      rectangleAcrossAntimeridian
    )
  val pointInsidePositiveAntimeridianRectGen
      : Gen[(Location, Region, List[LocationMatchResult])] =
    testParameterGenerator(
      (170f + coordinateTolerance, 180f),
      (-10f + coordinateTolerance, 10f - coordinateTolerance),
      rectangleAcrossAntimeridian
    )
  val pointCloseToInsideEquatorEdgeOfLongRectangle
      : Gen[(Location, Region, List[LocationMatchResult])] =
    testParameterGenerator(
      (-180f, 0f),
      (10f + coordinateTolerance, 10f + coordinateTolerance),
      longNorthernRectangle
    )
  val generatedResultMustEqualCases = List(
    (
      "Location inside prime meridian rectangle",
      pointInsidePrimeMeridianRectGen
    ),
    (
      "Location inside the negative side of an antimeridian rectangle",
      pointInsideNegativeAntimeridianRectGen
    ),
    (
      "Location inside the positive side of an antimeridian rectangle",
      pointInsidePositiveAntimeridianRectGen
    ),
    (
      "Location close to the inside equator facing edge of a long rectangle",
      pointCloseToInsideEquatorEdgeOfLongRectangle
    )
  )

  generatedResultMustEqualCases.foreach { case (name, pointGenerator) =>
    property(
      s"Generated result must equal solver result: ${name}"
    ) {
      forAll(pointGenerator) { case (location, region, locationMatchResult) =>
        val result = solverInstance.matchRegionsToLocations(
          List[Region](region),
          List[Location](location)
        )
        result match {
          case Left(error) =>
            fail("Error occured during solving", clues(error, result))
          case Right(result: List[LocationMatchResult]) =>
            assertEquals(result, locationMatchResult)
        }
      }
    }
  }
}
