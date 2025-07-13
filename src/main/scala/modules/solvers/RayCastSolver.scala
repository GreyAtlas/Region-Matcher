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

object RayCastSolver extends Solver {
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

  private def isPointInPolygon(point: Point, polygon: Polygon): Boolean =
    polygon.vertices
      .sliding(2, 1)
      .foldLeft(false)((isInPolygon, edge) =>
        isInPolygon ^ doesEdgeIntersectHorizontalRay(
          point,
          Line(edge(0), edge(1))
        )
      )

  private def doesEdgeIntersectHorizontalRay(
      point: Point,
      edge: Line
  ): Boolean =
    ((point.latitude.value > math.min(
      edge.start.latitude.value,
      edge.end.latitude.value
    )) &&
      (point.latitude.value <= math.max(
        edge.start.latitude.value,
        edge.end.latitude.value
      )) &&
      (point.longitude.value <= math.max(
        edge.start.longitude.value,
        edge.end.longitude.value
      ))) match {
      case true       => isIntersectionToTheRightOfPoint(point, edge)
      case _: Boolean => false;
    }

  private def isIntersectionToTheRightOfPoint(
      point: Point,
      edge: Line
  ): Boolean =
    val longitudeIntersection =
      ((point.latitude.value - edge.start.latitude.value)
        * (edge.end.longitude.value - edge.start.longitude.value)
        / (edge.end.latitude.value - edge.start.latitude.value) + edge.start.longitude.value)
    (point.longitude.value <= longitudeIntersection)

}
