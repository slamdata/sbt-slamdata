val sbtPgpVersion = "1.1.0"
val sbtReleaseVersion = "1.0.12"
val sbtTravisCiVersion = "1.2.0"
val sbtGitHubPackagesVersion = "0.3.0"
val sbtHeaderVersion  = "5.3.1"

addSbtPlugin("com.jsuereth"      % "sbt-pgp"             % sbtPgpVersion)
addSbtPlugin("com.github.gseitz" % "sbt-release"         % sbtReleaseVersion)
addSbtPlugin("com.codecommit"    % "sbt-github-packages" % sbtGitHubPackagesVersion)
addSbtPlugin("com.dwijnand"      % "sbt-travisci"        % sbtTravisCiVersion)
addSbtPlugin("de.heikoseeberger" % "sbt-header"          % sbtHeaderVersion)

buildInfoKeys := Seq[BuildInfoKey](
  "sbtPgpVersion" -> sbtPgpVersion,
  "sbtReleaseVersion" -> sbtReleaseVersion,
  "sbtGitHubPackagesVersion" -> sbtGitHubPackagesVersion,
  "sbtTravisCiVersion" -> sbtTravisCiVersion,
  "sbtHeaderVersion" -> sbtHeaderVersion)

buildInfoPackage := "sbtslamdata"

enablePlugins(BuildInfoPlugin)
