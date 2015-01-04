import sbt._
import sbt.Keys._
import com.typesafe.sbt.pgp._
import com.typesafe.sbt.pgp.PgpKeys._

object MyBuild extends Build {
  lazy val Travis =
    config("travis") extend Compile describedAs "Settings for building on Travis CI."

  lazy val configurationSettings: Seq[Setting[_]] = Seq(
    pgpStaticContext <<= (pgpPublicRing, pgpSecretRing) apply SbtPgpStaticContext.apply,
    pgpCmdContext <<= (pgpStaticContext, pgpPassphrase, streams) map SbtPgpCommandContext.apply,
    skip in pgpSigner <<= (skip in pgpSigner) ?? false,
    pgpSigner := (if (useGpg.value)
      new CommandLineGpgSigner(gpgCommand.value, useGpgAgent.value, pgpSigningKey.value)
    else
      new BouncyCastlePgpSigner(pgpCmdContext.value, pgpSigningKey.value)),
    pgpVerifier := (if (useGpg.value)
      new CommandLineGpgVerifier(gpgCommand.value)
    else
      new BouncyCastlePgpVerifier(pgpCmdContext.value))
  )

  override lazy val settings =
    super.settings ++ inConfig(Travis)(configurationSettings)

  lazy val root = (project in file("."))
    .configs(Travis)
    .settings(inConfig(Travis)(PgpSettings.projectSettings): _*)
}
