package types

final case class Point(longitude: Longitude, latitude: Latitude)

object Point:
  def apply(
      longitude: Option[Longitude],
      latitude: Option[Latitude]
  ): Option[Point] =
    (longitude, latitude) match {
      case (Some(longitude), Some(latitude)) =>
        Some(new Point(longitude, latitude))
      case (_, _) => None
    }

  def apply(
      longitude: Float,
      latitude: Float
  ): Option[Point] =
    (Longitude(longitude), Latitude(latitude)) match {
      case (Some(longitude), Some(latitude)) =>
        Some(new Point(longitude, latitude))
      case (_, _) => None
    }
