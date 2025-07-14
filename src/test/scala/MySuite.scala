// For more information on writing tests, see
import types.LocationMatchResult
import types.Region
import types.Polygon
import types.Point
import types.Longitude
import types.Latitude
import modules.solvers.RayCast2DSolver
import types.Location

// https://scalameta.org/munit/docs/getting-started.html
class MySuite extends munit.FunSuite {
  test("example test that succeeds") {
    val obtained = 42
    val expected = 42
    assertEquals(obtained, expected)
  }

}
