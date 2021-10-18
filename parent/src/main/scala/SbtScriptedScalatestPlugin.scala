import sbt._
import sbt.Keys._
import sbt.Def.Initialize
import sbt.ScriptedPlugin.autoImport._
import sbt.plugins.SbtPlugin

object SbtScriptedScalatestPlugin extends AutoPlugin {
  override def requires = SbtPlugin
  override def trigger = allRequirements

  val scriptedPrepare = taskKey[Unit]("scriptedPrepare")

  object autoImport {
    val scriptedScalatestDependencies = settingKey[Seq[String]](
      "ex \"org.scalatest::scalatest-funsuite:3.2.10\""
    )
  }
  import autoImport._

  override def projectSettings: Seq[Setting[_]] = Seq(
    scriptedLaunchOpts += "-Xmx1024M",
    scriptedBufferLog := false,
    scripted := scripted.dependsOn(scriptedPrepare).evaluated,
    scriptedPrepare := scriptedPrepareTask.value,
    scriptedScalatestDependencies := Nil,
  )

  /** Prepare [project/plugins.sbt, test] files for all
    * scripted test project in src/sbt-test/ * / *
    */
  def scriptedPrepareTask: Initialize[Task[Unit]] = Def.task {
    def debs = scriptedScalatestDependencies.value.map { _
      .replace("::", "\" %% \"")
      .replace(":", "\" % \"")
    }.map(d => s"""  "$d"""")
      .mkString("\n")

    for {
      prjDir <- (
        PathFinder(sbtTestDirectory.value) * DirectoryFilter * DirectoryFilter
        ).get()
    } {
      IO.write(
        prjDir / "project/plugins.sbt",
        s"""addSbtPlugin("${organization.value}" % "${moduleName.value}" % "${version.value}")
           |libraryDependencies ++= Seq(\n$debs\n)
           |""".stripMargin
      )
      IO.write(prjDir / "test", "> scriptedScalatest\n")
    }
  }
}
