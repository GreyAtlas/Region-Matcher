package modules.solvers

import cats.syntax.all.*
import types.Latitude
import types.Line
import types.Location
import types.LocationMatchResult
import types.Longitude
import types.Point
import types.Polygon
import types.Region

import scala.math

/* An implementation of the ray cast point in polygon algorithm
  Assumes [Longitude, Latitude] coordinates are on a flat 2D grid.
  Thus it SHOULD be inaccurate for edges covering long distances.
  The antimeridian is handled by creating an extra edge at every crossing
  which projects the inner part of the crossing
  onto the positive 180 degree antimeridian.
 */
object RayCast2DSolver extends Solver {
  private case class PolygonConversionState(
      previousCrossingLatitude: Option[Latitude],
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
    regions.map(region => matchLocationsToRegion(locations, region)).sequence

  override def matchLocationsToRegion(
      locations: List[Location],
      region: Region
  ): Either[String, LocationMatchResult] = {
    val adjustedPolygonsEither =
      region.coordinates.map(polygon => adjustPolygon(polygon)).sequence

    adjustedPolygonsEither match {
      case Left(error) => Left(error)
      case Right(adjustedPolygons) => {
        val matchedLocations = for {
          adjustedPolygon <- adjustedPolygons
          location <- locations
          if isPointInPolygon(location.coordinates, adjustedPolygon)
        } yield location.name
        Right(LocationMatchResult(region.name, matchedLocations.toList))
      }

    }
  }

  // this method is responsible for cutting the polygon along the antimeridian.
  // Unless i messed up somewhere, it should solve correctly. however with how verbose this got
  // its very likely that i missed a mistake somewhere.
  private def adjustPolygon(polygon: Polygon): Either[String, Vector[Line]] = {
    val initialState = Right(PolygonConversionState(None, Vector.empty[Line]))
    polygon.vertices
      .sliding(2, 1)
      .foldLeft[Either[String, PolygonConversionState]](initialState) {
        (stateEither, nextEdge) =>
          stateEither.flatMap(state =>
            state match {
              case state if (edgeCrossesAntimeridian(nextEdge)) =>
                constructNewEdgesAtCrossingPoint(state, nextEdge)
              case state =>
                Right(
                  PolygonConversionState(
                    state.previousCrossingLatitude,
                    state.accumulatedEdges
                      .appended(Line(nextEdge(0), nextEdge(1)))
                  )
                )
            }
          )

      } match {
      case Left(error)  => Left(error)
      case Right(value) => Right(value.accumulatedEdges)
    }
  }
  private def constructNewEdgesAtCrossingPoint(
      state: PolygonConversionState,
      edge: Vector[Point]
  ): Either[String, PolygonConversionState] = {
    val startPoint = edge(0)
    val endPoint = edge(1)
    val crossingLatitude = interpolateLatitudeAtGivenLongitude(
      normalizePointAroundAntimeridian(startPoint),
      normalizePointAroundAntimeridian(endPoint),
      180
    )
    val crossingLatitudeOpt = Latitude(crossingLatitude)
    val (positiveLongitudePoint, negativeLongitudePoint) =
      if (startPoint.longitude.value > 0)
        (startPoint, endPoint)
      else (endPoint, startPoint)

    (state, crossingLatitudeOpt) match {
      case (_, None) => Left("Failed to find valid crossing point")
      case (
            PolygonConversionState(None, accumulatedEdges),
            (Some(latitude))
          ) => {
        val newEdges = Vector(
          Line(
            positiveLongitudePoint,
            Point(Longitude.getPositiveAntimeridian, latitude)
          ),
          Line(
            negativeLongitudePoint,
            Point(Longitude.getNegativeAntimeridian, latitude)
          )
        )
        Right(
          PolygonConversionState(
            Some(crossingLatitudeOpt.get),
            accumulatedEdges.appendedAll(newEdges)
          )
        )
      }
      case (
            PolygonConversionState(
              Some(previousCrossingLatitude),
              accumulatedEdges
            ),
            Some(crossingLatitude)
          ) => {

        val newEdges = Vector(
          Line(
            positiveLongitudePoint,
            Point(Longitude.getPositiveAntimeridian, crossingLatitude)
          ),
          Line(
            negativeLongitudePoint,
            Point(Longitude.getNegativeAntimeridian, crossingLatitude)
          ),
          Line(
            Point(Longitude.getPositiveAntimeridian, previousCrossingLatitude),
            Point(Longitude.getPositiveAntimeridian, crossingLatitude)
          )
        )
        Right(
          PolygonConversionState(
            Some(crossingLatitudeOpt.get),
            accumulatedEdges.appendedAll(newEdges)
          )
        )
      }

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
        accumulated ^
          (doesEdgeIntersectHorizontalRay(testPoint, edge)
            && isIntersectionToTheRightOfPoint(testPoint, edge))
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
      )))

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
