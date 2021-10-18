import com.github.daniel.shuy.sbt.scripted.scalatest.ScriptedScalaTestSuiteMixin
import org.scalatest.wordspec.AnyWordSpec

lazy val testFailure = project
  .in(file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "test/sbt-0.13/testFailure",

    scriptedBufferLog := false,

    scriptedScalaTestStacks := SbtScriptedScalaTest.FullStacks,
    scriptedScalaTestSpec := Some(new AnyWordSpec with ScriptedScalaTestSuiteMixin {
      override val sbtState: State = state.value

      "scripted" should {
        "fail on ScalaTest failure" in {
          assert(1 == 2)
          assertThrows[sbt.Incomplete](
            Project.extract(sbtState)
              .runInputTask(scripted, "", sbtState))
        }
      }
    })
  )
