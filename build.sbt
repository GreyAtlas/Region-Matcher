val scala3Version = "3.7.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "region-matcher",
    version := "1.0",
    scalaVersion := scala3Version,
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-Xlint",
      "-Wnonunit-statement",
      "-Wunused:imports"
    ),
    semanticdbEnabled := true,
    scalafixOnCompile := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    Compile / run / fork := true,
    assembly / mainClass := Some("Main"),
    assembly / test := (Test / test).value,
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "1.0.0" % Test,
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.6" % Test,
      "org.typelevel" %% "cats-core" % "2.13.0",
      "org.typelevel" %% "cats-effect" % "3.6.2",
      "com.monovore" %% "decline" % "2.5.0",
      "com.monovore" %% "decline-effect" % "2.5.0",
      "org.scala-lang" %% "toolkit" % "0.7.0",
      "io.circe" %% "circe-core" % "0.14.7",
      "io.circe" %% "circe-generic" % "0.14.7",
      "io.circe" %% "circe-parser" % "0.14.7"
    )
  )
