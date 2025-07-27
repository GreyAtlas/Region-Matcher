package modules.solvers

import types.Latitude
import types.Line
import types.Location
import types.LocationMatchResult
import types.Point
import types.Polygon
import types.Region

import scala.math

// An implementation of the ray cast point in polygon algorithm
// Assumes [Longitude, Latitude] coordinates are on a flat 2D grid.
// Thus it will be inaccurate for edges covering long distances.
// But handles the discontinuity at the antimeridian as long as the polygon doesn't cover more than
object RayCast2DSolver extends Solver {
  private case class State(
      previousCrossingPoint: Option[Point],
      accumulatedEdges: Vector[Line]
  )
  private case class LongitudeAdjustedPoint(
      longitude: Float,
      latitude: Latitude
  )
  override def matchRegionsToLocations(
      regions: List[Region],
      locations: List[Location]
  ): Either[String, List[LocationMatchResult]] =
    Right(
      regions.map(region =>
        matchLocationsToRegion(locations, region) match {
          case Right(value: LocationMatchResult) => value
          case Left(value)                       => ???
        }
      )
    )

  override def matchLocationsToRegion(
      locations: List[Location],
      region: Region
  ): Either[String, LocationMatchResult] = {
    val adjustedPolygons =
      region.coordinates.map(polygon => adjustPolygon(polygon))

    val result = for {
      adjustedPolygon <- adjustedPolygons
      location <- locations
      if isPointInPolygon(location.coordinates, adjustedPolygon)
    } yield location.name

    Right(LocationMatchResult(region.name, result.toList))
  }

  private def adjustPolygon(polygon: Polygon): Vector[Line] = {
    val initialState = State(None, Vector.empty[Line])
    polygon.vertices
      .sliding(2, 1)
      .foldLeft(initialState) {
        case (state, nextEdge) if (edgeCrossesAntimeridian(nextEdge)) =>
          constructNewEdgesAtCrossingPoint(state, nextEdge)
        case (state, nextEdge) =>
          State(
            state.previousCrossingPoint,
            state.accumulatedEdges.appended(Line(nextEdge(0), nextEdge(1)))
          )
      }
      .accumulatedEdges
  }
  private def constructNewEdgesAtCrossingPoint(
      state: State,
      edge: Vector[Point]
  ): State = {
    val startPoint = edge(0)
    val endPoint = edge(1)
    val crossingLatitude = interpolateLatitudeAtGivenLongitude(
      normalizePointAroundAntimeridian(startPoint),
      normalizePointAroundAntimeridian(endPoint),
      180
    )
    val crossingPointOpt = Point(180.0f, crossingLatitude)
    val newEdges = {
      val (x, y) =
        if (startPoint.longitude.value > endPoint.longitude.value)
          (startPoint, endPoint)
        else (endPoint, startPoint)

      crossingPointOpt match {
        case None => ???
        case Some(crossingPoint) =>
          Vector(Line(x, crossingPoint), Line(crossingPoint, y))
      }
    }

    state match {
      case State(None, accumulatedEdges) =>
        State(
          Some(crossingPointOpt.get),
          accumulatedEdges.appendedAll(newEdges)
        )
      case State(Some(previousCrossingLatitude), accumulatedEdges) =>
        State(
          None,
          accumulatedEdges
            .appendedAll(newEdges)
            .appended(
              Line(
                previousCrossingLatitude,
                crossingPointOpt.get
              )
            )
        )
    }
  }
  private def normalizePointAroundAntimeridian(
      point: Point
  ): LongitudeAdjustedPoint = {
    val newLongitude =
      if (point.longitude.value < 0)
        point.longitude.value + 360
      else point.longitude.value
    LongitudeAdjustedPoint(newLongitude, point.latitude)
  }

  private def edgeCrossesAntimeridian(edge: Vector[Point]): Boolean =
    math.abs(edge(0).longitude.value - edge(1).longitude.value) > 180.0f

  private def interpolateLatitudeAtGivenLongitude(
      start: LongitudeAdjustedPoint,
      end: LongitudeAdjustedPoint,
      interpolationLongitude: Float
  ): Float =
    (interpolationLongitude - start.longitude)
      * (end.latitude.value - start.latitude.value)
      / (end.longitude - start.longitude)
      + (start.latitude.value)

  // For each Edge checks if it intersects a ray cast from the point in the positive longitude direction
  // and XORs the booleans from each edge check.
  private def isPointInPolygon(
      testPoint: Point,
      polygon: Vector[Line]
  ): Boolean =
    polygon
      .foldLeft(false)((accumulated, edge) =>
        accumulated ^ doesEdgeIntersectHorizontalRay(
          testPoint,
          edge
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
  ): Boolean = {
    val longitudeIntersection =
      ((testPoint.latitude.value - edge.start.latitude.value)
        * (edge.end.longitude.value - edge.start.longitude.value)
        / (edge.end.latitude.value - edge.start.latitude.value) + edge.start.longitude.value)
    (testPoint.longitude.value <= longitudeIntersection)
  }
}
