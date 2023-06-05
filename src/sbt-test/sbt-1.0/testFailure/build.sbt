import org.scalatest.wordspec.AnyWordSpec

lazy val testFailure = project
  .in(file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "test/sbt-0.13/testFailure",

    scriptedScalatestDependencies += "org.scalatest::scalatest-wordspec:3.2.16",

    scriptedScalatestStacks := SbtScriptedScalatest.FullStacks,
    scriptedScalatestSpec := Some(new AnyWordSpec with ScriptedScalatestSuiteMixin {
      override val sbtState: State = state.value

      "scripted" should {
        "fail on Scalatest failure" in {
          assertThrows[sbt.Incomplete](
            Project.extract(sbtState)
              .runInputTask(scripted, "", sbtState))
        }
      }
    }),
  )
