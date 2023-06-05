import sbt._
import sbt.Keys._
import sbt.Def.Initialize
import sbt.ScriptedPlugin.autoImport._
import sbt.plugins.SbtPlugin

object SbtScriptedScalatestPlugin extends AutoPlugin {
  override def requires = SbtPlugin
  override def trigger = allRequirements

  val scriptedPrepare = taskKey[Unit](
    "Generate src/sbt-test/*/*/{project/plugins.sbt, test}"
  )

  object autoImport {
    val scriptedScalatestDependencies = settingKey[Seq[String]](
      "[sbt:]organization(:: or :)name:(version)." +
        " ex org.scalatest::scalatest-funsuite:3.2.16 or sbt:com.sandinh:sbt-devops:6.8.0"
    )
  }
  import autoImport._

  override def projectSettings: Seq[Setting[_]] = Seq(
    scriptedLaunchOpts += "-Xmx1024M",
    scriptedBufferLog := false,
    scripted := scripted.dependsOn(scriptedPrepare).evaluated,
    scriptedPrepare := scriptedPrepareTask.value,
    scriptedScalatestDependencies := Seq(
      s"sbt:${organization.value}:${moduleName.value}:${version.value}",
      s"sbt:com.sandinh:sbt-scripted-scalatest-impl:${SbtScriptedScalatestVersion.version}",
    ),
  )

  private def scriptedPrepareTask: Initialize[Task[Unit]] = Def.task {
    val log = streams.value.log
    val content = pluginsContent.value
    val finder =
      PathFinder(sbtTestDirectory.value) * DirectoryFilter * DirectoryFilter
    val sbtV = (pluginCrossBuild / sbtVersion).?.value
    for (prjDir <- finder.get()) {
      // delete target, project/project/target, */target directories
      IO.delete(
        Seq("target", "project/project/target").map(prjDir / _) ++
          (PathFinder(prjDir) * DirectoryFilter * "target").get()
      )
      sbtV.foreach { v =>
        IO.write(prjDir / "project/build.properties", s"sbt.version=$v\n")
      }

      IO.write(prjDir / "project/plugins.sbt", content)

      val testFile = prjDir / "test"
      if (!testFile.exists()) IO.write(testFile, "> scriptedScalatest\n")

      log.info(
        s"Prepared $prjDir\nYou can open that as a project in Intellij or vscode"
      )
    }
  }

  /** org(:: | :)name:version -> "org" (%% | %) "name" % "version" */
  private def colon2Percent(debs: Seq[String]): Seq[String] = debs
    .map {
      _.replace("::", "\" %% \"")
        .replace(":", "\" % \"")
    }
    .map("\"" + _ + "\"")

  private def pluginsContent: Initialize[String] = Def.setting {
    val (pluginDeps, normalDeps) = scriptedScalatestDependencies.value
      .partition(_.startsWith("sbt:"))
    val plugins = colon2Percent(pluginDeps.map(_.stripPrefix("sbt:")))
      .map(d => s"addSbtPlugin($d)")
      .mkString("\n")
    val debs = colon2Percent(normalDeps).mkString(",\n  ")
    s"$plugins\nlibraryDependencies ++= Seq(\n  $debs\n)"
  }
}
