import org.scalatest.wordspec.AnyWordSpec

lazy val testBasicSimple = project
  .in(file("."))
  .settings(
    name := "test/basic/simple",

    scriptedScalatestStacks := SbtScriptedScalatest.FullStacks,
    scriptedScalatestSpec := Some(new AnyWordSpec with ScriptedScalatestSuiteMixin {
      override val sbtState: State = state.value

      "scripted test" should {
        "fail" in {
          fail()
        }
      }
    })
  )
