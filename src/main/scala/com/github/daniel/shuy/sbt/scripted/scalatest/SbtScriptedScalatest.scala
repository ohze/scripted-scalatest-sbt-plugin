package com.github.daniel.shuy.sbt.scripted.scalatest

import org.scalatest.{ScriptedScalatestSuite, Suite}
import sbt._
import sbt.Keys.streams

object SbtScriptedScalatest extends AutoPlugin {
  override def trigger: PluginTrigger = allRequirements

  sealed abstract class ScriptedTestStacks(
      val shortstacks: Boolean,
      val fullstacks: Boolean
  )
  case object NoStacks extends ScriptedTestStacks(false, false)
  case object ShortStacks extends ScriptedTestStacks(true, false)
  case object FullStacks extends ScriptedTestStacks(true, true)

  object autoImport {
    lazy val scriptedScalatestDurations = settingKey[Boolean](
      "If false, will not display durations of tests."
    )
    lazy val scriptedScalatestStacks = settingKey[ScriptedTestStacks](
      "Length of stack traces to print."
    )
    lazy val scriptedScalatestStats = settingKey[Boolean](
      "If false, will not display various statistics of tests."
    )
    lazy val scriptedScalatestSpec =
      taskKey[Option[Suite with ScriptedScalatestSuiteMixin]]("The Scalatest Spec.")

    lazy val scriptedScalatest = taskKey[Unit](
    "Executes all Scalatest tests for SBT plugin."
    )
  }
  import autoImport._

  private[this] lazy val logger = Def.task[Logger] {
    streams.value.log
  }

  override def projectSettings: Seq[Setting[_]] = Seq(
    scriptedScalatestDurations := true,
    scriptedScalatestStacks := NoStacks,
    scriptedScalatestStats := true,
    scriptedScalatestSpec := None,
    scriptedScalatest := {
      // do nothing if not configured
      scriptedScalatestSpec.value match {
        case Some(suite) => executeScriptedTestsTask(suite)
        case None =>
          logger.value.warn(
            s"${scriptedScalatestSpec.key.label} not configured, no tests will be run..."
          )
      }
    }
  )

  private[this] def executeScriptedTestsTask(
      suite: ScriptedScalatestSuite
  ): Unit = Def.task {
    println("@@@@@@@")
    val stacks = scriptedScalatestStacks.value
    val status = suite.executeScripted(
      durations = scriptedScalatestDurations.value,
      shortstacks = stacks.shortstacks,
      fullstacks = stacks.fullstacks,
      stats = scriptedScalatestStats.value
    )
    status.waitUntilCompleted()
    if (!status.succeeds()) {
      sys.error("Scripted Scalatest suite failed!")
    }
  }
}
