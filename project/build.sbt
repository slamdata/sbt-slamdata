// Holding at 1.0.0 until sbt/sbt-pgp#97 is addressed
val sbtPgpVersion      = "1.0.0"
val sbtReleaseVersion  = "1.0.4"
val sbtSonatypeVersion = "1.1"
val sbtTravisCiVersion = "1.0.0"

lazy val root = project.in(file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    addSbtPlugin("com.jsuereth"      % "sbt-pgp"      % sbtPgpVersion),
    addSbtPlugin("com.github.gseitz" % "sbt-release"  % sbtReleaseVersion),
    addSbtPlugin("org.xerial.sbt"    % "sbt-sonatype" % sbtSonatypeVersion),
    addSbtPlugin("com.dwijnand"      % "sbt-travisci" % sbtTravisCiVersion),
    buildInfoKeys := Seq[BuildInfoKey](
      "sbtPgpVersion"      -> sbtPgpVersion,
      "sbtReleaseVersion"  -> sbtReleaseVersion,
      "sbtSonatypeVersion" -> sbtSonatypeVersion,
      "sbtTravisCiVersion" -> sbtTravisCiVersion),
    buildInfoPackage := "sbtslamdata")
