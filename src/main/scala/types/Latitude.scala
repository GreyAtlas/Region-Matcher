package types

final case class Latitude private (value: Float)

object Latitude:
  def apply(value: Float): Option[Latitude] =
    if (value >= -90 && value <= 90) Some(new Latitude(value))
    else None
