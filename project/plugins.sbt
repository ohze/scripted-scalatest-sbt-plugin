addSbtPlugin("com.sandinh" % "sbt-devops-oss" % "6.8.0")

Compile / unmanagedSourceDirectories +=
  baseDirectory.value.getParentFile / "parent/src/main/scala"
Compile / sourceGenerators += Def.task {
  val f = (Compile / managedSourceDirectories).value.head / "V.scala"
  IO.write(
    f,
    s"""object SbtScriptedScalatestVersion {
       |  val version = "???" // don't use
       |}""".stripMargin
  )
  Seq(f)
}
