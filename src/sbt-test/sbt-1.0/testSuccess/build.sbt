import org.scalatest.wordspec.AnyWordSpec

lazy val testFailure = project
  .in(file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "test/sbt-0.13/testFailure",

    scriptedScalatestDependencies ++= Seq(
      "org.scalatest::scalatest-funsuite:3.2.10",
      "org.scalatest::scalatest-mustmatchers:3.2.10",
    ),

    scriptedScalatestSpec := Some(new AnyWordSpec with ScriptedScalatestSuiteMixin {
      override val sbtState: State = state.value

      "scripted" should {
        "success on Scalatest success" in {
          Project.extract(sbtState)
            .runInputTask(scripted, "", sbtState)
        }
      }
    }),
  )
