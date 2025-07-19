package modules.solvers

import types.Location
import types.LocationMatchResult
import types.Region

trait Solver {
  def matchRegionsToLocations(
      regions: List[Region],
      locations: List[Location]
  ): List[LocationMatchResult]
}
