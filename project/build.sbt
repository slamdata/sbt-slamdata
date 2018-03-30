val sbtPgpVersion      = "1.1.0"
val sbtReleaseVersion  = "1.0.6"
val sbtTravisCiVersion = "1.1.1"
val sbtBintrayVersion  = "0.5.4"

lazy val root = project.in(file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    addSbtPlugin("com.jsuereth"      % "sbt-pgp"      % sbtPgpVersion),
    addSbtPlugin("com.github.gseitz" % "sbt-release"  % sbtReleaseVersion),
    addSbtPlugin("org.foundweekends" % "sbt-bintray"  % sbtBintrayVersion),
    addSbtPlugin("com.dwijnand"      % "sbt-travisci" % sbtTravisCiVersion),
    buildInfoKeys := Seq[BuildInfoKey](
      "sbtPgpVersion"      -> sbtPgpVersion,
      "sbtReleaseVersion"  -> sbtReleaseVersion,
      "sbtBintrayVersion"  -> sbtBintrayVersion,
      "sbtTravisCiVersion" -> sbtTravisCiVersion),
    buildInfoPackage := "sbtslamdata")
