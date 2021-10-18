addSbtPlugin("com.sandinh" % "sbt-devops-oss" % "5.0.12")

Compile / unmanagedSourceDirectories +=
  baseDirectory.value.getParentFile / "parent/src/main/scala"
