val publishSnapshot = taskKey[Unit]("Publishes snapshot artifacts to a repository.")

lazy val buildSettings = Seq(
  organization := "de.ummels",
  description := "Immutable priority maps for Scala",
  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.10.6", "2.11.8"),
  scalacOptions ++= Seq("-Xlint", "-unchecked", "-deprecation", "-feature")
)

lazy val docSettings = Seq(
  scalacOptions in(Compile, doc) ++=
      Seq("-doc-root-content", baseDirectory.value + "/root-doc.txt"),
  scalacOptions in(Compile, doc) ++= Seq("-doc-title", name.value),
  scalacOptions in(Compile, doc) ++= {
    val copy = "© 2014-2016 Michael Ummels"
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
  },
  autoAPIMappings := true
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
  apiURL := {
    val baseUrl = "https://oss.sonatype.org/service/local/repositories/releases/archive/de/ummels/"
    val artifact = normalizedName.value + "_" + scalaBinaryVersion.value
    val dir =  artifact + "/" + version.value + "/"
    val jar = artifact + "-" + version.value + "-javadoc.jar"
    if (isSnapshot.value) Some(url("https://ummels.github.io/scala-prioritymap/api/"))
    else Some(url(baseUrl + dir + jar + "/!/"))
  },
  pomExtra := {
    <developers>
      <developer>
        <id>ummels</id>
        <name>Michael Ummels</name>
      </developer>
    </developers>
  },
  pgpSecretRing := file(Path.userHome.absolutePath + "/.sbt/gpg/secring.gpg"),
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
  PgpKeys.publishSigned := (),
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
) ++ site.includeScaladoc("api")

lazy val commonSettings = Seq(
  parallelExecution in Test := false,
  coverageHighlighting := scalaBinaryVersion.value == "2.11",
  scalastyleSources in Compile ++= (sourceDirectories in Compile).value // See https://github.com/scalastyle/scalastyle-sbt-plugin/issues/47
)

lazy val commonJsSettings = Seq(
  scalaJSStage in Global := FastOptStage,
  parallelExecution := false
)

lazy val commonJvmSettings = Seq(
  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDF")
)

lazy val root = project.in(file(".")).
    settings(name := "scala-prioritymap-root").
    settings(buildSettings:_*).
    settings(noPublishSettings:_*).
    aggregate(libraryJVM, libraryJS, testsJVM, testsJS)

lazy val library = crossProject.crossType(CrossType.Pure).in(file("library")).
    settings(name := "scala-prioritymap").
    settings(buildSettings:_*).
    settings(docSettings:_*).
    settings(publishSettings:_*).
    settings(commonSettings:_*).
    jvmSettings(commonJvmSettings:_*).
    jvmSettings(siteSettings:_*).
    jsSettings(commonJsSettings:_*).
    jsConfigure(_.disablePlugins(scoverage.ScoverageSbtPlugin))

lazy val libraryJVM = library.jvm

lazy val libraryJS = library.js

lazy val tests = crossProject.in(file("tests")).
    dependsOn(library).
    settings(name := "scala-prioritymap-tests").
    settings(buildSettings:_*).
    settings(noPublishSettings:_*).
    settings(commonSettings:_*).
    settings(
      libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.0" % "test",
      libraryDependencies += "org.scalacheck" %%% "scalacheck" % "1.13.4" % "test"
    ).
    jvmSettings(commonJvmSettings:_*).
    jsSettings(commonJsSettings:_*).
    jsConfigure(_.disablePlugins(scoverage.ScoverageSbtPlugin))

lazy val testsJVM = tests.jvm

lazy val testsJS = tests.js

addCommandAlias("validateJVM", ";coverage;testsJVM/test;coverageReport")
addCommandAlias("validateJS", ";testsJS/test")
addCommandAlias("validate", ";validateJVM;validateJS")
