import cats.data.EitherT
import cats.effect.*
import cats.syntax.all.*
import com.monovore.decline.*
import com.monovore.decline.effect.*
import modules.IO.IOHandler
import modules.json.CirceJsonParser
import modules.solvers.RayCast2DSolver

case class MatcherConfig(
    locationsPath: String,
    regionsPath: String,
    outputPath: String
)

object Main
    extends CommandIOApp(
      name = "region matcher",
      header =
        "This application matches the provided locations to the regions they are located in",
      version = "1.0.0"
    ) {
  val locationsPathOpt = Opts
    .option[String](
      "locations",
      short = "l",
      metavar = "path",
      help = "Set the locations.json path"
    )
  val regionsPathOpt = Opts
    .option[String](
      "regions",
      short = "r",
      metavar = "path",
      help = "Set the locations.json path"
    )
  val outputPathOpt = Opts
    .option[String](
      "output",
      short = "o",
      metavar = "path",
      help = "Set the output.json path"
    )
  val configOpts: Opts[MatcherConfig] =
    (locationsPathOpt, regionsPathOpt, outputPathOpt).mapN(MatcherConfig.apply)

  override def main: Opts[IO[ExitCode]] =
    (locationsPathOpt, regionsPathOpt, outputPathOpt)
      .mapN { (locationsPath, regionsPath, outputPath) =>
        runApp(MatcherConfig(locationsPath, regionsPath, outputPath)).value
          .flatMap({
            case Right(value) => IO.println(value).as(ExitCode.Success)
            case Left(value)  => IO.println(value).as(ExitCode.Error)
          })
      }

  def runApp(
      config: MatcherConfig
  ): EitherT[IO, String, String] = {
    for {
      locationsFile <- EitherT.liftF(
        IOHandler.readFileIntoMemory(config.locationsPath)
      )
      locations <- EitherT.fromEither(
        CirceJsonParser.parseLocationJson(locationsFile)
      )
      regionsFile <- EitherT.liftF(
        IOHandler.readFileIntoMemory(config.regionsPath)
      )
      regions <- EitherT.fromEither(
        CirceJsonParser.parseRegionJson(regionsFile)
      )
      solverResult =
        RayCast2DSolver.matchRegionsToLocations(
          regions,
          locations
        )
      resultJson <- EitherT.fromEither(
        CirceJsonParser.encodeResultsToJson(solverResult)
      )
      output <- EitherT.liftF(
        IOHandler.writeToFile(config.outputPath, resultJson)
      )
    } yield output

  }
}
