val scala3Version = "3.7.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala-main-task",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    assembly / mainClass := Some("com.regionmatcher.Main"),
    assembly / test := (Test / test).value,
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "1.0.0" % Test,
      "org.scala-lang" %% "toolkit" % "0.7.0",
      "io.circe" %% "circe-core" % "0.14.7",
      "io.circe" %% "circe-generic" % "0.14.7",
      "io.circe" %% "circe-parser" % "0.14.7"
    )
  )
