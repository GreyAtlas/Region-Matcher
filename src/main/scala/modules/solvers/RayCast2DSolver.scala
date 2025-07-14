package modules.solvers

import types.Location

import types.LocationMatchResult

import types.Region
import types.Point
import types.Longitude
import types.Latitude
import types.Line
import types.Polygon
import scala.math

// An implementation of the ray cast point in polygon algorithm
// Assumes [Longitude, Latitude] coordinates are on a flat 2D grid.
// Thus it will be inaccurate for edges covering long distances.
// But handles the discontinuity at the antimeridian as long as the polygon doesn't cover more than
object RayCast2DSolver extends Solver {
  def matchRegionsToLocations(
      regions: List[Region],
      locations: List[Location]
  ): List[LocationMatchResult] =
    regions.map(region => matchLocationsToRegion(locations, region))

  private def matchLocationsToRegion(
      locations: List[Location],
      region: Region
  ): LocationMatchResult =
    val locationsMatchedToRegion = for {
      location <- locations
      polygon <- region.polygons
      if isPointInPolygon(location.coordinates, polygon)
    } yield {
      location.name
    }
    LocationMatchResult(region.name, locationsMatchedToRegion)

  // For each Edge checks if it intersects a ray cast from the point in the positive longitude direction
  // and XORs the booleans from each edge check.
  private def isPointInPolygon(testPoint: Point, polygon: Polygon): Boolean =
    polygon.vertices
      .sliding(2, 1)
      .foldLeft(false)((accumulated, edge) =>
        val pointsAdjustedForComparison =
          shiftLongitude(testPoint, edge(0), edge(1))
        accumulated ^ doesEdgeIntersectHorizontalRay(
          pointsAdjustedForComparison._1,
          pointsAdjustedForComparison._2
        )
      )

  // To handle the discontinuity at the dateline I shift to the negative the test point and edge points longitudes
  private def shiftLongitude(
      testPoint: Point,
      edgeStart: Point,
      edgeEnd: Point
  ): (Point, Line) =
    def shiftFunction(
        pointToShift: Point,
        minPositiveLongitude: Float
    ): Point = pointToShift.copy(longitude =
      Longitude(
        math.abs(pointToShift.longitude.value - minPositiveLongitude) - 180
      ).get
    )
    val minPositiveLongitude = (Vector[Float](
      testPoint.longitude.value,
      edgeStart.longitude.value,
      edgeEnd.longitude.value
    ).filter(_ >= 0).minOption).getOrElse(0.0f) 
    (
      shiftFunction(testPoint, minPositiveLongitude),
      Line(
        shiftFunction(edgeStart, minPositiveLongitude),
        shiftFunction(edgeEnd, minPositiveLongitude)
      )
    )

  private def doesEdgeIntersectHorizontalRay(
      testPoint: Point,
      edge: Line
  ): Boolean =
    ((testPoint.latitude.value > math.min(
      edge.start.latitude.value,
      edge.end.latitude.value
    )) &&
      (testPoint.latitude.value <= math.max(
        edge.start.latitude.value,
        edge.end.latitude.value
      )) &&
      (testPoint.longitude.value <= math.max(
        edge.start.longitude.value,
        edge.end.longitude.value
      ))) match {
      case true       => isIntersectionToTheRightOfPoint(testPoint, edge)
      case _: Boolean => false;
    }
  
  private def isIntersectionToTheRightOfPoint(
      testPoint: Point,
      edge: Line
  ): Boolean =
    val longitudeIntersection =
      ((testPoint.latitude.value - edge.start.latitude.value)
        * (edge.end.longitude.value - edge.start.longitude.value)
        / (edge.end.latitude.value - edge.start.latitude.value) + edge.start.longitude.value)
    (testPoint.longitude.value <= longitudeIntersection)

}
