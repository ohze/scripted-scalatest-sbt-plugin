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
        " ex org.scalatest::scalatest-funsuite:3.2.10 or sbt:com.sandinh:sbt-devops:5.0.12"
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
    val content = pluginsContent.value
    val finder =
      PathFinder(sbtTestDirectory.value) * DirectoryFilter * DirectoryFilter
    for (prjDir <- finder.get()) {
      IO.write(prjDir / "project/plugins.sbt", content)
      IO.write(prjDir / "test", "> scriptedScalatest\n")
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
    val debs = colon2Percent(normalDeps).mkString("\n  ")
    s"$plugins\nlibraryDependencies ++= Seq(\n  $debs\n)"
  }
}
