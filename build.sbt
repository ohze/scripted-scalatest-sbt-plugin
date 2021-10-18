lazy val `sbt-scripted-scalatest` = project
  .in(file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    organization := "com.github.daniel-shuy",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest-core" % "3.2.10"
    ),
    pluginCrossBuild / sbtVersion := "1.2.6", // minimum sbt version
    SbtScriptedSettings("org.scalatest::scalatest-wordspec:3.2.10"),
  )

inThisBuild(
  Seq(
    versionScheme := Some("semver-spec"),
    licenses := Seq(
      "Apache License, Version 2.0" -> url(
        "http://www.apache.org/licenses/LICENSE-2.0.txt"
      )
    ),
    developers := List(
      Developer(
        "daniel-shuy",
        "Daniel Shuy",
        "daniel_shuy@hotmail.com",
        url("https://github.com/daniel-shuy")
      )
    )
  )
)
