name := "scala-prioritymap"

version := "0.3.0-SNAPSHOT"

organization := "de.ummels"

description := "Immutable priority maps for Scala"

licenses := Seq("ISC License" -> url("http://opensource.org/licenses/ISC"))

homepage := Some(url("https://github.com/ummels/scala-prioritymap"))

apiURL := Some(url("http://ummels.github.io/scala-prioritymap/"))

scalaVersion := "2.11.5"

crossScalaVersions := Seq("2.10.4", "2.11.5")

scalacOptions ++= Seq("-Xlint", "-unchecked", "-deprecation", "-feature")

autoAPIMappings := true

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.3" % "test"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.12.1" % "test"
