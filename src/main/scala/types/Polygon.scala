package types



final case class Polygon private (vertices: Vector[Point])

object Polygon:
  private def validatePolygon(vertices: Vector[Point]): Option[Polygon] =
    if (
      vertices.length >= 4 &
        (vertices(0) == vertices.last)
    )
      Some(new Polygon(vertices))
    else None

  def apply(vertices: Vector[Point]): Option[Polygon] =
    validatePolygon(vertices)

  def apply(vertices: Option[Vector[Point]]): Option[Polygon] =
    vertices match {
      case None                      => None
      case Some(verticesWithoutNone) => validatePolygon(verticesWithoutNone)
    }
