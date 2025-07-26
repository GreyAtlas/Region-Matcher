package modules.solvers

import cats.syntax.all.*
import org.locationtech.spatial4j.context.SpatialContext
import org.locationtech.spatial4j.context.jts.JtsSpatialContext
import org.locationtech.spatial4j.shape
import org.locationtech.spatial4j.shape.ShapeFactory
import org.locationtech.spatial4j.shape.SpatialRelation
import types.Location
import types.LocationMatchResult
import types.Region

import scala.util.Try

object Spatial4jSolver extends Solver {
  private val spatial4jFacade: SpatialContext = JtsSpatialContext.GEO
  private val factory: ShapeFactory = spatial4jFacade.getShapeFactory
  private case class Spatial4jLocation(name: String, point: shape.Point)

  override def matchRegionsToLocations(
      regions: List[Region],
      locations: List[Location]
  ): Either[String, List[LocationMatchResult]] =
    regions.traverse(region => matchLocationsToRegion(locations, region))

  override def matchLocationsToRegion(
      locations: List[Location],
      region: Region
  ): Either[String, LocationMatchResult] =
    val spatial4jMatchResult = for {
      spatial4jLocations <- locations.traverse(location =>
        convertPointToSpatial4JPoint(location.coordinates).map(spatial4jPoint =>
          Spatial4jLocation(location.name, spatial4jPoint)
        )
      )
      spatial4jPolygons <- region.coordinates.traverse(polygon =>
        convertPolygonToSpatial4jPolygon(polygon)
      )
    } yield for {
      spatial4jLocation <- spatial4jLocations
      spatial4jPolygon <- spatial4jPolygons
      if doesPolygonContainPoint(spatial4jPolygon, spatial4jLocation.point)
    } yield spatial4jLocation.name
    spatial4jMatchResult match {
      case Left(error) =>
        Left(
          s"An error occured while matching locations to regions using Spatial4j: ${error.toString()}"
        )
      case Right(value) => Right(LocationMatchResult(region.name, value))
    }

  /* 
  This method should also be wrapped in a Try, but i couldnt figure out how to use it
  as the filter in a for comprehension. Which probably means im misusing the for comprehension
  or not handling using java libraries in scala properly.
   */

  private def doesPolygonContainPoint(
      polygon: shape.Shape,
      point: shape.Point
  ): Boolean =
    polygon.relate(point) match {
      case SpatialRelation.CONTAINS => true
      case _                        => false
    }

  private def convertPolygonToSpatial4jPolygon(
      polygon: types.Polygon
  ): Either[Throwable, shape.Shape] =
    Try({
      val polygonBuilder = factory.polygon()
      polygon.vertices.foreach(vertex =>
        polygonBuilder.pointLatLon(
          vertex.latitude.value,
          vertex.longitude.value
        )
      )
      polygonBuilder.build()
    }).toEither

  private def convertPointToSpatial4JPoint(
      point: types.Point
  ): Either[Throwable, shape.Point] =
    Try(
      factory.pointLatLon(point.latitude.value, point.longitude.value)
    ).toEither
}
