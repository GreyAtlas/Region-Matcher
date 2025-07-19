package modules.solvers
// For more information on writing tests, see
import modules.solvers.RayCast2DSolver
import types.Latitude
import types.Location
import types.LocationMatchResult
import types.Longitude
import types.Point
import types.Polygon
import types.Region

class RayCastSolverSuite extends munit.FunSuite {

  test(
    "When Polygon edges cross Antimeridian and Point is inside Polygon Test Succeeds"
  ) {
    val expected = List[LocationMatchResult](
      LocationMatchResult("region1", List[String]("location1"))
    )
    val region = Region(
      "region1",
      Vector[Polygon](
        Polygon(
          Vector[Point](
            Point(Longitude(179).get, Latitude(1).get),
            Point(Longitude(179).get, Latitude(10).get),
            Point(Longitude(-179).get, Latitude(10).get),
            Point(Longitude(-179).get, Latitude(1).get),
            Point(Longitude(179).get, Latitude(1).get)
          )
        ).get
      )
    )
    val location =
      Location("location1", Point(Longitude(179.95).get, Latitude(5).get))
    val result = RayCast2DSolver.matchRegionsToLocations(
      List[Region](region),
      List[Location](location)
    )
    assertEquals(result, expected)
  }

  test(
    "When Polygon edges cross Antimeridian and Point is outside of the polygon Test Succeeds"
  ) {
    val expected = List[LocationMatchResult](
      LocationMatchResult("region1", List[String]())
    )
    val region = Region(
      "region1",
      Vector[Polygon](
        Polygon(
          Vector[Point](
            Point(Longitude(-179).get, Latitude(1).get),
            Point(Longitude(-179).get, Latitude(10).get),
            Point(Longitude(179).get, Latitude(10).get),
            Point(Longitude(179).get, Latitude(1).get),
            Point(Longitude(-179).get, Latitude(1).get)
          )
        ).get
      )
    )
    val location =
      Location("location1", Point(Longitude(175).get, Latitude(5).get))
    val result = RayCast2DSolver.matchRegionsToLocations(
      List[Region](region),
      List[Location](location)
    )
    assertEquals(result, expected)
  }
  test(
    "When Polygon edges cross Antimeridian and Point is outside of the polygon on the opossite side of the sphere Test Succeeds"
  ) {
    val expected = List[LocationMatchResult](
      LocationMatchResult("region1", List[String]())
    )
    val region = Region(
      "region1",
      Vector[Polygon](
        Polygon(
          Vector[Point](
            Point(Longitude(-179).get, Latitude(1).get),
            Point(Longitude(-179).get, Latitude(10).get),
            Point(Longitude(179).get, Latitude(10).get),
            Point(Longitude(179).get, Latitude(1).get),
            Point(Longitude(-179).get, Latitude(1).get)
          )
        ).get
      )
    )
    val location =
      Location("location1", Point(Longitude(0).get, Latitude(5).get))
    val result = RayCast2DSolver.matchRegionsToLocations(
      List[Region](region),
      List[Location](location)
    )
    assertEquals(result, expected)
  }
  test(
    "When Polygon edges cross equator and Point is inside of the polygon Test Succeeds"
  ) {
    val expected = List[LocationMatchResult](
      LocationMatchResult("region1", List[String]("location1"))
    )
    val region = Region(
      "region1",
      Vector[Polygon](
        Polygon(
          Vector[Point](
            Point(Longitude(0).get, Latitude(10).get),
            Point(Longitude(10).get, Latitude(-10).get),
            Point(Longitude(15).get, Latitude(-15).get),
            Point(Longitude(15).get, Latitude(10).get),
            Point(Longitude(0).get, Latitude(10).get)
          )
        ).get
      )
    )
    val location =
      Location(
        "location1",
        Point(
          Longitude(11.85643298748869).get,
          Latitude(-4.788821988519017).get
        )
      )
    val result = RayCast2DSolver.matchRegionsToLocations(
      List[Region](region),
      List[Location](location)
    )
    assertEquals(result, expected)
  }
  test(
    "When Polygon edges cross equator and Point is outside of the polygon Test Succeeds"
  ) {
    val expected = List[LocationMatchResult](
      LocationMatchResult("region1", List[String]())
    )
    val region = Region(
      "region1",
      Vector[Polygon](
        Polygon(
          Vector[Point](
            Point(Longitude(0).get, Latitude(10).get),
            Point(Longitude(10).get, Latitude(-10).get),
            Point(Longitude(15).get, Latitude(-15).get),
            Point(Longitude(15).get, Latitude(10).get),
            Point(Longitude(0).get, Latitude(10).get)
          )
        ).get
      )
    )
    val location =
      Location(
        "location1",
        Point(
          Longitude(-34.88259993048736).get,
          Latitude(-7.126545504760394).get
        )
      )
    val result = RayCast2DSolver.matchRegionsToLocations(
      List[Region](region),
      List[Location](location)
    )
    assertEquals(result, expected)
  }
  test(
    "When all coordinates are below 0 Test succeeds"
  ) {
    val expected = List[LocationMatchResult](
      LocationMatchResult("region1", List[String]("location1"))
    )
    val region = Region(
      "region1",
      Vector[Polygon](
        Polygon(
          Vector[Point](
            Point(Longitude(-75).get, Latitude(-10).get),
            Point(Longitude(-70).get, Latitude(-10).get),
            Point(Longitude(-70).get, Latitude(-15).get),
            Point(Longitude(-75).get, Latitude(-15).get),
            Point(Longitude(-75).get, Latitude(-10).get)
          )
        ).get
      )
    )
    val location =
      Location(
        "location1",
        Point(
          Longitude(-72.54553246581928).get,
          Latitude(-13.163335231358104).get
        )
      )
    val result = RayCast2DSolver.matchRegionsToLocations(
      List[Region](region),
      List[Location](location)
    )
    assertEquals(result, expected)
  }
  test(
    "When polygon coordinates are below 0, but the coordinates for the test point are positive Test succeeds"
  ) {
    val expected = List[LocationMatchResult](
      LocationMatchResult("region1", List[String]())
    )
    val region = Region(
      "region1",
      Vector[Polygon](
        Polygon(
          Vector[Point](
            Point(Longitude(-75).get, Latitude(-10).get),
            Point(Longitude(-70).get, Latitude(-10).get),
            Point(Longitude(-70).get, Latitude(-15).get),
            Point(Longitude(-75).get, Latitude(-15).get),
            Point(Longitude(-75).get, Latitude(-10).get)
          )
        ).get
      )
    )
    val location =
      Location(
        "location1",
        Point(
          Longitude(23.95775681867698).get,
          Latitude(54.90396142127144).get
        )
      )
    val result = RayCast2DSolver.matchRegionsToLocations(
      List[Region](region),
      List[Location](location)
    )
    assertEquals(result, expected)
  }
  test(
    "When the polygon encircles the north pole and the point is outside Test Succeeds"
  ) {
    val expected = List[LocationMatchResult](
      LocationMatchResult("region1", List[String]())
    )
    val region = Region(
      "region1",
      Vector[Polygon](
        Polygon(
          Vector[Point](
            Point(Longitude(-180).get, Latitude(80).get),
            Point(Longitude(0).get, Latitude(90).get),
            Point(Longitude(-180).get, Latitude(90).get),
            Point(Longitude(0).get, Latitude(90).get),
            Point(Longitude(180).get, Latitude(90).get),
            Point(Longitude(0).get, Latitude(90).get),
            Point(Longitude(180).get, Latitude(80).get),
            Point(Longitude(0).get, Latitude(90).get),
            Point(Longitude(-180).get, Latitude(80).get)
          )
        ).get
      )
    )
    val location =
      Location(
        "location1",
        Point(
          Longitude(0).get,
          Latitude(50).get
        )
      )
    val result = RayCast2DSolver.matchRegionsToLocations(
      List[Region](region),
      List[Location](location)
    )
    assertEquals(result, expected)
  }
}
