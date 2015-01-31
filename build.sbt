name := "scala-prioritymap"

version := "0.4.0-SNAPSHOT"

organization := "de.ummels"

description := "Immutable priority maps for Scala"

licenses := Seq("ISC License" -> url("http://opensource.org/licenses/ISC"))

homepage := Some(url("https://github.com/ummels/scala-prioritymap"))

scalaVersion := "2.11.5"

crossScalaVersions := Seq("2.10.4", "2.11.5")

scalacOptions ++= Seq("-Xlint", "-unchecked", "-deprecation", "-feature")

scalacOptions in (Compile, doc) ++=
  Seq("-doc-root-content", baseDirectory.value + "/root-doc.txt")

scalacOptions in (Compile, doc) ++= Seq("-doc-title", name.value)

scalacOptions in (Compile, doc) ++= {
  val copy = "© 2014-2015 Michael Ummels"
  val footer = name.value + " " + version.value + " API documentation. " + copy
  Seq("-doc-footer", footer)
}

scalacOptions in (Compile, doc) ++= {
  val prefix = "https://www.github.com/ummels/scala-prioritymap/blob/"
  val path = "/src/main/scala/de/ummels/prioritymap/€{TPL_NAME}.scala"
  if (isSnapshot.value)
    Seq("-doc-source-url", prefix + "master" + path)
  else
    Seq("-doc-source-url", prefix + "v" + version.value + path)
}

autoAPIMappings := true

apiURL := {
  if (isSnapshot.value)
    None
  else
    Some(url("https://ummels.github.io/scala-prioritymap/api/" + version.value))
}

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.3" % "test"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.12.1" % "test"
