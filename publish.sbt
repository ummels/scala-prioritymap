site.settings

site.includeScaladoc("")

ghpages.settings

git.remoteRepo := "git@github.com:ummels/scala-prioritymap.git"

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

pgpSecretRing in Travis := baseDirectory.value / "secring.gpg"

pgpPublicRing in Travis := baseDirectory.value / "pubring.gpg"

pgpSigningKey in Travis := Some(0x94C0654F15395FC5L)

pgpPassphrase in Travis := sys.env.get("GPG_PASSPHRASE") map (_.toCharArray)

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
