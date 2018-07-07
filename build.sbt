name := """atomic-ops"""

version := "0.1.0"

lazy val root = project in file(".")

scalaVersion := "2.12.6"

libraryDependencies += "org.scalatest" % "scalatest_2.12" % "3.0.5" % "test"

// https://tpolecat.github.io/2017/04/25/scalac-flags.html
scalacOptions ++= Seq(
    "-Ywarn-unused:imports",
    "-Ywarn-unused:locals",
    "-Ywarn-unused:params",
    "-Ywarn-unused:patvars",
    "-Ywarn-unused:privates")
