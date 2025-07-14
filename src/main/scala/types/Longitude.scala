package types

final case class Longitude private (value: Float)

object Longitude:
  def apply(value: Float): Option[Longitude] =
    if (value >= -180 && value <= 180) Some(new Longitude(value))
    else None
