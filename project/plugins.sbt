addSbtPlugin("com.sandinh" % "sbt-devops" % "5.0.12")

Compile / sources += baseDirectory.value.getParentFile /
  "src/main/scala/SbtScriptedSettings.scala"
