package types

final case class Polygon private (vertices: Vector[Point])

object Polygon:
  def apply(vertices: Vector[Point]): Option[Polygon] =
    if (
      vertices.length >= 4 &
        (vertices(0) == vertices.last)
    )
      Some(new Polygon(vertices))
    else None
