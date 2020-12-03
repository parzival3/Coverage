ThisBuild / name := "chisel-vframework"
ThisBuild / version := "0.2"
ThisBuild / organization := "parzival3"

scalaVersion := "2.12.10"
scalacOptions := Seq("-Xsource:2.11")


resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases"),
)

libraryDependencies ++= Seq(
  "com.googlecode.jgenhtml" % "jgenhtml" % "1.5",
  "com.vladsch.flexmark" % "flexmark-all" % "0.35.10",
  "edu.berkeley.cs" % "chiseltest_2.12" % "0.3-SNAPSHOT",
  "csp" % "csp_2.12" % "0.2",
  "parzival3" %% "chisel-crv" % "0.2.2"
)

testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-C", "org.scalatest.tools.CoverageHtmlReporter")
coverageEnabled := true

