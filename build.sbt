lazy val `sbt-scripted-scalatest` = (project in file("parent"))
  .enablePlugins(SbtPlugin)
  .settings(
    pluginCrossBuild / sbtVersion := "1.2.6", // minimum sbt version
  )

lazy val `sbt-scripted-scalatest-impl` = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    libraryDependencies += "org.scalatest" %% "scalatest-core" % "3.2.10",
    pluginCrossBuild / sbtVersion := "1.2.6", // minimum sbt version
    scriptedScalatestDependencies += "org.scalatest::scalatest-wordspec:3.2.10",
  ).aggregate(`sbt-scripted-scalatest`)

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
        "thanhbv",
        "Bui Viet Thanh",
        "thanhbv@sandinh.net",
        url("https://sandinh.com")
      ),
      Developer(
        "daniel-shuy",
        "Daniel Shuy",
        "daniel_shuy@hotmail.com",
        url("https://github.com/daniel-shuy")
      ),
    )
  )
)
