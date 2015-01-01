(for {
  password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
} yield
  credentials += Credentials("Sonatype Nexus Repository Manager",
                             "oss.sonatype.org",
                             "ummels",
                             password))
