name := "scala-prioritymap"

version := "0.2.0-SNAPSHOT"

organization := "de.ummels"

description := "Immutable priority maps for Scala"

licenses := Seq("BSD-style" -> url("http://opensource.org/licenses/ISC"))

homepage := Some(url("https://github.com/ummels/scala-prioritymap"))

scalaVersion := "2.11.4"

crossScalaVersions := Seq("2.10.4", "2.11.4")

scalacOptions ++= Seq("-Xlint", "-unchecked", "-deprecation", "-feature")

autoAPIMappings := true

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.2" % "test"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.12.1" % "test"
