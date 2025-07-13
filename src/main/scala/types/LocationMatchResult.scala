package types

final case class LocationMatchResult(
    regionName: String,
    matchedLocationNames: List[String]
)
