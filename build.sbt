name := "Coverage"

version := "0.1"

def scalacOptionsVersion(scalaVersion: String): Seq[String] = {
  Seq() ++ {
    // If we're building with Scala > 2.11, enable the compile option
    //  switch to support our anonymous Bundle definitions:
    //  https://github.com/scala/bug/issues/10047
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, scalaMajor: Long)) if scalaMajor < 12 => Seq()
      case _ => Seq("-Xsource:2.11")
    }
  }
}

def javacOptionsVersion(scalaVersion: String): Seq[String] = {
  Seq() ++ {
    // Scala 2.12 requires Java 8. We continue to generate
    //  Java 7 compatible code for Scala 2.11
    //  for compatibility with old clients.
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, scalaMajor: Long)) if scalaMajor < 12 =>
        Seq("-source", "1.7", "-target", "1.7")
      case _ =>
        Seq("-source", "1.8", "-target", "1.8")
    }
  }
}

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.8",
  "org.scalatest" %% "scalatest" % "3.2.0" % "test"
)

scalaVersion := "2.12.10"
scalacOptions := Seq("-Xsource:2.11")

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

lazy val root = (project in file(".")).dependsOn(chisel_tester2)
lazy val chisel_tester2 = RootProject(uri("https://github.com/ryan-lund/chisel-testers2.git"))

libraryDependencies += "edu.berkeley.cs" %% "chisel3" % "3.2.0"

// Provide a managed dependency on X if -DXVersion="" is supplied on the command line.
val defaultVersions = Seq(
  "chisel3" -> "3.4-SNAPSHOT",
  "treadle" -> "1.3-SNAPSHOT"
)

libraryDependencies ++= Seq(
  "com.googlecode.jgenhtml" % "jgenhtml" % "1.5"
)

libraryDependencies ++= defaultVersions.map {
  case (dep, ver) =>
    "edu.berkeley.cs" %% dep % sys.props.getOrElse(dep + "Version", ver)
}

libraryDependencies ++= Seq(
  "org.choco-solver" % "choco-solver" % "4.10.2"
)

libraryDependencies ++= Seq(
  "com.vladsch.flexmark" % "flexmark-all" % "0.35.10"
)
scalacOptions ++= scalacOptionsVersion(scalaVersion.value)
scalacOptions ++= Seq("-deprecation", "-feature", "-language:reflectiveCalls")

javacOptions ++= javacOptionsVersion(scalaVersion.value)

testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-C", "org.scalatest.tools.CoverageHtmlReporter")
