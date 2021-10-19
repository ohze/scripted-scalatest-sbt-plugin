lazy val `sbt-scripted-scalatest` = (project in file("parent"))
  .enablePlugins(SbtPlugin)
  .settings(
    pluginCrossBuild / sbtVersion := "1.2.6", // minimum sbt version
    Compile / sourceGenerators += Def.task {
      val f = (Compile / managedSourceDirectories).value.head / "V.scala"
      IO.write(
        f,
        s"""object SbtScriptedScalatestVersion {
           |  val version = "${version.value}"
           |}""".stripMargin
      )
      Seq(f)
    },
  )

lazy val `sbt-scripted-scalatest-impl` = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    libraryDependencies += "org.scalatest" %% "scalatest-core" % "3.2.10",
    pluginCrossBuild / sbtVersion := "1.2.6", // minimum sbt version
    scriptedScalatestDependencies := Seq(
      s"sbt:${organization.value}:${moduleName.value}:${version.value}",
      s"sbt:${organization.value}:sbt-scripted-scalatest:${version.value}",
      "org.scalatest::scalatest-wordspec:3.2.10"
    ),
    scripted := scripted
      .dependsOn(`sbt-scripted-scalatest` / publishLocal)
      .evaluated
  ).aggregate(`sbt-scripted-scalatest`)

inThisBuild(
  Seq(
    organization := "com.sandinh",
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
