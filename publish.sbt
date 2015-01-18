site.settings

site.includeScaladoc("api/current")

ghpages.settings

git.remoteRepo := {
  val repo = "ummels/scala-prioritymap.git"
  sys.env.get("GITHUB_TOKEN") match {
    case None => "git@github.com:" + repo
    case Some(token) => "https://" + token + "@github.com/" + repo
  }
}

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

credentials ++= (for (pw <- sys.env.get("SONATYPE_PASSWORD").toSeq) yield
  Credentials("Sonatype Nexus Repository Manager",
              "oss.sonatype.org",
              "ummels",
              pw))

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

val publishSnapshot = taskKey[Unit]("Publishes snapshot artifacts to a repository.")

publishSnapshot := Def.taskDyn {
  if (isSnapshot.value) Def.task { publish.value }
  else Def.task {
    println("Version " + version.value + " is not a snapshot version.")
  }
}.value
