name := "priority-map"

version := "0.1-SNAPSHOT"

organization := "de.ummels"

scalaVersion := "2.11.4"

scalacOptions ++= Seq("-Xlint", "-unchecked", "-deprecation", "-feature")

autoAPIMappings := true

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.2" % "test"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.12.0" % "test"
