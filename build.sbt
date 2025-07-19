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
      // "-Werror",
      "-Xlint",
      "-Wunused:imports"
    ),
    semanticdbEnabled := true,
    scalafixOnCompile := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    assembly / mainClass := Some("Main"),
    assembly / test := (Test / test).value,
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "1.0.0" % Test,
      "com.lihaoyi" %% "os-lib" % "0.11.3",
      // "org.scala-lang" %% "toolkit" % "0.7.0",
      // "io.circe" %% "circe-core" % "0.14.7",
      // "io.circe" %% "circe-generic" % "0.14.7",
      "io.circe" %% "circe-parser" % "0.14.7"
    )
  )
