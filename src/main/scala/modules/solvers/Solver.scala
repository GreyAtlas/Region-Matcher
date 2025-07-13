package modules.solvers

import types.Location
import types.Region
import types.LocationMatchResult

trait Solver {
  def matchRegionsToLocations(
      regions: List[Region],
      locations: List[Location]
  ): List[LocationMatchResult]
}
