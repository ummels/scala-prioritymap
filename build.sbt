import scoverage.ScoverageSbtPlugin

val publishSnapshot = taskKey[Unit]("Publishes snapshot artifacts to a repository.")

lazy val buildSettings = Seq(
  name := "scala-prioritymap",
  version := "0.4.0-SNAPSHOT",
  organization := "de.ummels",
  description := "Immutable priority maps for Scala",
  scalaVersion := "2.11.7",
  crossScalaVersions := Seq("2.10.5", "2.11.7"),
  scalacOptions ++= Seq("-Xlint", "-unchecked", "-deprecation", "-feature"),
  libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.0-M9" % "test",
  libraryDependencies += "org.scalacheck" %%% "scalacheck" % "1.12.5" % "test"
)

lazy val docSettings = Seq(
  scalacOptions in(Compile, doc) ++=
      Seq("-doc-root-content", baseDirectory.value + "/root-doc.txt"),
  scalacOptions in(Compile, doc) ++= Seq("-doc-title", name.value),
  scalacOptions in(Compile, doc) ++= {
    val copy = "© 2014-2015 Michael Ummels"
    val footer = name.value + " " + version.value + " API documentation. " + copy
    Seq("-doc-footer", footer)
  },
  scalacOptions in(Compile, doc) ++= {
    val prefix = "https://www.github.com/ummels/scala-prioritymap/blob/"
    val path = "/src/main/scala/de/ummels/prioritymap/€{TPL_NAME}.scala"
    if (isSnapshot.value)
      Seq("-doc-source-url", prefix + "master" + path)
    else
      Seq("-doc-source-url", prefix + "v" + version.value + path)
  }
)

lazy val publishSettings = Seq(
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  credentials ++= (for (pw <- sys.env.get("SONATYPE_PASSWORD").toSeq) yield
    Credentials("Sonatype Nexus Repository Manager",
      "oss.sonatype.org",
      "ummels",
      pw)),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  homepage := Some(url("https://github.com/ummels/scala-prioritymap")),
  licenses := Seq("ISC License" -> url("http://opensource.org/licenses/ISC")),
  scmInfo := Some(ScmInfo(url("https://github.com/ummels/scala-prioritymap"),
    "scm:git:git@github.com:ummels/scala-prioritymap.git")),
  autoAPIMappings := true,
  apiURL := {
    if (isSnapshot.value) None
    else Some(url("https://ummels.github.io/scala-prioritymap/api/" + version.value))
  },
  pomExtra := {
    <developers>
      <developer>
        <id>ummels</id>
        <name>Michael Ummels</name>
      </developer>
    </developers>
  },
  publishSnapshot := Def.taskDyn {
    if (isSnapshot.value) Def.task { publish.value }
    else Def.task {
      println("Version " + version.value + " is not a snapshot version.")
    }
  }.value
)

lazy val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)

lazy val siteSettings = site.settings ++ ghpages.settings ++ Seq(
  git.remoteRepo := {
    val repo = "ummels/scala-prioritymap.git"
    sys.env.get("GITHUB_TOKEN") match {
      case None => "git@github.com:" + repo
      case Some(token) => "https://" + token + "@github.com/" + repo
    }
  }
) ++ site.includeScaladoc("api/current")

lazy val commonSettings = Seq(
  coverageHighlighting := scalaBinaryVersion.value != "2.10"
)

lazy val commonJsSettings = Seq(
  scalaJSStage in Global := FastOptStage,
  parallelExecution := false
)

lazy val commonJvmSettings = Seq()

lazy val root = project.in(file(".")).
    aggregate(libraryJVM, libraryJS).
    settings(name := "scala-prioritymap-root").
    settings(noPublishSettings: _*)

lazy val library = crossProject.in(file(".")).
    settings(buildSettings: _*).
    settings(docSettings: _*).
    settings(publishSettings: _*).
    settings(commonSettings: _*).
    jvmSettings(commonJvmSettings: _*).
    jvmSettings(siteSettings: _*).
    jsSettings(commonJsSettings: _*).
    jsConfigure(_.disablePlugins(ScoverageSbtPlugin))

lazy val libraryJVM = library.jvm

lazy val libraryJS = library.js

addCommandAlias("validateJVM", ";coverage;libraryJVM/test")
addCommandAlias("validateJS", ";libraryJS/test")
addCommandAlias("validate", ";validateJVM;validateJS")
