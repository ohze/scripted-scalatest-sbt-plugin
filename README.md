# scripted-scalatest-sbt-plugin

[![CI](https://github.com/ohze/scripted-scalatest-sbt-plugin/actions/workflows/sbt-devops.yml/badge.svg)](https://github.com/ohze/scripted-scalatest-sbt-plugin/actions/workflows/sbt-devops.yml)

A SBT plugin to use [ScalaTest](http://www.scalatest.org/) with scripted-plugin to test your SBT plugins

Traditionally, to test a SBT plugin, you had to create subprojects in `/sbt-test`, then in the subprojects, create SBT tasks to perform the testing, then specify the tasks to execute in a `test` file (see <https://www.scala-sbt.org/1.x/docs/Testing-sbt-plugins.html>).

This is fine when performing simple tests, but for complicated tests (see <https://www.scala-sbt.org/1.x/docs/Testing-sbt-plugins.html#step+6%3A+custom+assertion>), this can get messy really quickly:

-   It sucks to not be able to write tests in a BDD style (except by using comments, which feels clunky).
-   Manually writing code to print the test results to the console for each subproject is a pain.

This plugin leverages ScalaTest's powerful assertion system (to automatically print useful messages on assertion failure) and its expressive DSLs.

This plugin allows you to use any of ScalaTest's test [Suites](http://www.scalatest.org/user_guide/selecting_a_style), including [AsyncTestSuites](http://www.scalatest.org/user_guide/async_testing).

## Notes

-   Do not use ScalaTest's [ParallelTestExecution](https://www.scalatest.org/scaladoc/3.2.10/org/scalatest/ParallelTestExecution.html) mixin with this plugin. `ScriptedScalaTestSuiteMixin` runs `sbt clean` before each test, which may cause weird side effects when run in parallel.
-   When executing SBT tasks in tests, use `Project.runTask(<task>, state.value)` instead of `<task>.value`. Calling `<task>.value` declares it as a dependency, which executes before the tests, not when the line is called.
-   When implementing [BeforeAndAfterEach](https://www.scalatest.org/scaladoc/3.2.10/org/scalatest/BeforeAndAfterEach.html)'s `beforeEach`, make sure to invoke `super.beforeEach` afterwards:

```scala
override protected def beforeEach(): Unit = {
  // ...
  super.beforeEach() // To be stackable, must call super.beforeEach
}
```

- This SBT plugin is now tested using itself!

## Usage
Require sbt 1.2.x+

### Step 1. Add `sbt-scripted-scalatest`
`project/plugins.sbt`
```scala
addSbtPlugin("com.sandinh" % "sbt-scripted-scalatest" % "3.0.0")
```

### Step 2. Add dependencies for your sbt-test's projects
See [step 3: src/sbt-test](https://www.scala-sbt.org/1.x/docs/Testing-sbt-plugins.html#step+3%3A+src%2Fsbt-test)

`build.sbt`
```diff
 lazy val `my-plugin` = project
   .enablePlugins(SbtPlugin)
   .settings(
-    scriptedLaunchOpts := { scriptedLaunchOpts.value ++
-      Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
-    },
-    scriptedBufferLog := false
+    scriptedScalatestDependencies += "org.scalatest::scalatest-wordspec:3.2.10",
   )
```
`scriptedScalatestDependencies` will be used to the auto-generated `plugins.sbt` in all sbt-test's project:

`scr/sbt-test/<test-group>/<test-name>/project/plugins.sbt`

Ex, for [The FunSuite style](https://www.scalatest.org/user_guide/selecting_a_style), set:
```scala
scriptedScalatestDependencies += "org.scalatest::scalatest-funsuite:3.2.10"
```

### Step 3. Remove `src/sbt-test/*/*/{test, project/plugins.sbt}`
Those files are auto-generated by `sbt-scripted-scalatest`  
You can also add this to `.gitignore`:
```.gitignore
**/sbt-test/*/*/project/build.properties
**/sbt-test/*/*/project/plugins.sbt
**/sbt-test/*/*/test
```

### Step 4. Write your test

In `sbt-test/<test-group>/<test-name>/build.sbt`, create a new ScalaTest Suite/Spec, mixin `ScriptedScalaTestSuiteMixin` and pass it into `scriptedScalaTestSpec`. When mixing in `ScriptedScalaTestSuiteMixin`, implement `sbtState` as `state.value`.

Using SBT's Example in <https://www.scala-sbt.org/1.x/docs/Testing-sbt-plugins.html#step+6%3A+custom+assertion>:

```scala
import org.scalatest.wordspec.AnyWordSpec

lazy val root = (project in file("."))
  .settings(
    version := "0.1",
    scalaVersion := "3.0.2",
    assembly/ assemblyJarName := "foo.jar",
    scriptedScalaTestSpec := Some(new AnyWordSpec with ScriptedScalaTestSuiteMixin {
      override val sbtState: State = state.value
      "assembly" should "create a JAR that prints out 'bye'" in {
        Project.runTask(assembly, sbtState)
        import scala.sys.process._
        val process = s"java -jar ${crossTarget.value / "foo.jar"}"
        val out = process.!!
        assert(out.trim == "bye")
      }
    },
  )
```

It is possible move the ScalaTest Suite/Spec into a separate `.scala` file in the `project` folder, however that may cause issues when trying to access SBT `SettingKey`s or declaring custom `TaskKey`s, therefore is currently not recommended except for extremely simple tests. A better approach would be to move all configurations related to this plugin to a new `.sbt` file, eg. `test.sbt`.

See [Settings](#settings) for other configurable settings.

### Step 5: Use the scripted-plugin as usual

Eg. Run `sbt scripted` on the main project to execute all tests.

## Settings

| Setting                    | Type                                           | Description                                                                                                                                                                                                                     |
| -------------------------- | ---------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| scriptedScalaTestSpec      | Option[Suite with ScriptedScalaTestSuiteMixin] | **Required**. The ScalaTest Suite/Spec. If not configured (defaults to `None`), no tests will be executed.                                                                                                                      |
| scriptedScalaTestDurations | Boolean                                        | **Optional**. If `true`, displays durations of tests. Defaults to `true`.                                                                                                                                                       |
| scriptedScalaTestStacks    | NoStacks / ShortStacks / FullStacks            | **Optional**. The length of stack traces to display for failed tests. `NoStacks` will not display any stack traces. `ShortStacks` displays short stack traces. `FullStacks` displays full stack traces. Defaults to `NoStacks`. |
| scriptedScalaTestStats     | Boolean                                        | **Optional**. If `true`, displays various statistics of tests. Defaults to `true`.                                                                                                                                              |

## Tasks

| Task              | Description                                                                                                                                                                  |
| ----------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| scriptedScalatest | Executes all test configured in `scriptedScalaTestSpec`. This task must be [configured for scripted-plugin to run in the `test` script file](#step-4--write-your-test). |
