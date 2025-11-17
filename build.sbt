//enablePlugins(GitVersioning)

Common.commonSettings

name := "breeze-parent"

lazy val root = project
  .in(file("."))
  .aggregate(math, natives, viz, macros)
  .dependsOn(math, viz)

lazy val macros = project.in(file("macros"))
  .enablePlugins(JacocoPlugin)
  .settings(
    jacocoReportSettings := JacocoReportSettings()
      .withTitle("Macros coverage")
      .withFormats(JacocoReportFormats.HTML, JacocoReportFormats.XML)
  )

lazy val math = project.in(file("math")).dependsOn(macros)
  .enablePlugins(JacocoPlugin)
  .settings(
    jacocoReportSettings := JacocoReportSettings()
      .withTitle("Math coverage")
      .withFormats(JacocoReportFormats.HTML, JacocoReportFormats.XML)
  )

lazy val natives = project.in(file("natives")).dependsOn(math)

lazy val viz = project.in(file("viz")).dependsOn(math)

lazy val benchmark = project.in(file("benchmark")).dependsOn(math, natives)

Global / onChangedBuildSource := ReloadOnSourceChanges

// setup jacoco
import com.github.sbt.jacoco.JacocoPlugin.autoImport._
enablePlugins(JacocoPlugin)

lazy val testWithCoverage = taskKey[Unit]("Run tests and generate coverage report")

Test / testWithCoverage := (Test / jacoco).value

// end setup jacoco

// setup packaging

// JAR di main (senza test né dipendenze)
Compile / packageBin / artifactName := { (_, _, _) =>
  s"${name.value}_${version.value}_main.jar"
}

// JAR di test (solo test)
Test / packageBin / artifactName := { (_, _, _) =>
  s"${name.value}_${version.value}_tests.jar"
}

// JAR assembly (fat JAR senza test)
assembly / mainClass := Some("project.Main")
assembly / assemblyJarName := s"${name.value}_${version.value}_assembly.jar"
assembly / test := {} // evita di lanciare i test durante l’assembly

ThisBuild / commands += Command.command("packageAll") { state =>
  "clean" ::
    "package" ::            // genera main.jar
    "Test/package" ::       // genera tests.jar
    "assembly" ::           // genera assembly.jar
    state
}

Test / skip := false

// end setup packaging
