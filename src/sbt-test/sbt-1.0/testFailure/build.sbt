import com.github.daniel.shuy.sbt.scripted.scalatest.ScriptedScalatestSuiteMixin
import org.scalatest.wordspec.AnyWordSpec

lazy val testFailure = project
  .in(file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "test/sbt-0.13/testFailure",

    scriptedBufferLog := false,

    scriptedScalatestStacks := SbtScriptedScalatest.FullStacks,
    scriptedScalatestSpec := Some(new AnyWordSpec with ScriptedScalatestSuiteMixin {
      override val sbtState: State = state.value

      "scripted" should {
        "fail on Scalatest failure" in {
          assert(1 == 2)
          assertThrows[sbt.Incomplete](
            Project.extract(sbtState)
              .runInputTask(scripted, "", sbtState))
        }
      }
    })
  )
