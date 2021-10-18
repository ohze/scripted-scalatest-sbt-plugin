import sbt._
import sbt.Keys._
import sbt.Def.Initialize
import sbt.ScriptedPlugin.autoImport._

object SbtScriptedSettings {
  val scriptedPrepare = taskKey[Unit]("scriptedPrepare")

  def apply(dependencies: String*): Seq[Setting[_]] = Seq(
    scriptedLaunchOpts += "-Xmx1024M",
    scriptedBufferLog := false,
    scripted := scripted.dependsOn(scriptedPrepare).evaluated,
    scriptedPrepare := scriptedPrepareTask(dependencies: _*).value,
  )

  /** Prepare [project/plugins.sbt, test] files for all
    * scripted test project in src/sbt-test/ * / *
    * @param dependencies ex "org.scalatest::scalatest-wordspec:3.2.10" */
  def scriptedPrepareTask(dependencies: String*): Initialize[Task[Unit]] = Def.task {
    def debs = dependencies.map { _
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
