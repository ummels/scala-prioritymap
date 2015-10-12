resolvers += "jgit-repo" at "http://download.eclipse.org/jgit/maven"

resolvers += "sonatype-snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.5")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.8.0-SNAPSHOT")

addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.5.4")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.3")

addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.0.0")
