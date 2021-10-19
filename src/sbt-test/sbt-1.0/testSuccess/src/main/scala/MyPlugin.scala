import sbt._
import sbt.Keys._

object MyPlugin extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {
    val fooKey = settingKey[String]("")
  }
  import autoImport._
  override def projectSettings: Seq[Setting[_]] = Seq(
    fooKey := "bar",
    test := {
      IO.write(target.value / fooKey.value, "foo content")
    },
  )
}
