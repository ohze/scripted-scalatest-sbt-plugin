import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

lazy val testBasicSimple = project
  .in(file("."))
  .settings(
    name := "test/basic/simple",

    scriptedScalatestSpec := Some(new AnyFunSuite with Matchers with ScriptedScalatestSuiteMixin {
      override val sbtState: State = state.value

      test("scripted test") {
        val extracted = Project.extract(sbtState)
        extracted.get(fooKey) mustBe "bar"

        extracted.runTask(Keys.test, sbtState)
        val f = extracted.get(target) / "bar"
        IO.read(f) mustBe "foo content"
      }
    })
  )
