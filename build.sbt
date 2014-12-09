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

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.12.0" % "test"

// Configure publication of API doc on GitHub

site.settings

site.includeScaladoc("")

ghpages.settings

git.remoteRepo := "git@github.com:ummels/scala-prioritymap.git"

// Configure publication of artifacts on Sonatype

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := {
  <scm>
    <url>https://github.com/ummels/scala-prioritymap</url>
    <connection>scm:git:git@github.com:ummels/scala-prioritymap.git</connection>
  </scm>
  <developers>
    <developer>
      <id>ummels</id>
      <name>Michael Ummels</name>
    </developer>
  </developers>
}
