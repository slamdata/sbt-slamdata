val sbtPgpVersion      = "1.0.1"
val sbtReleaseVersion  = "1.0.4"
val sbtTravisCiVersion = "1.0.0"

lazy val root = project.in(file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    addSbtPlugin("com.jsuereth"      % "sbt-pgp"      % sbtPgpVersion),
    addSbtPlugin("com.github.gseitz" % "sbt-release"  % sbtReleaseVersion),
    addSbtPlugin("com.dwijnand"      % "sbt-travisci" % sbtTravisCiVersion),
    buildInfoKeys := Seq[BuildInfoKey](
      "sbtPgpVersion"      -> sbtPgpVersion,
      "sbtReleaseVersion"  -> sbtReleaseVersion,
      "sbtTravisCiVersion" -> sbtTravisCiVersion),
    buildInfoPackage := "sbtslamdata")
