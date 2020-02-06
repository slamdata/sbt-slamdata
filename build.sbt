import sbtslamdata.BuildInfo

import slamdata.SbtSlamData

name := "sbt-slamdata"
organization := "com.slamdata"

description := "Common build configuration for SBT projects"

sbtPlugin := true
Global / sbtVersion in Global := "1.3.0"

scalacStrictMode := false

addSbtPlugin("com.jsuereth"      % "sbt-pgp"             % BuildInfo.sbtPgpVersion)
addSbtPlugin("com.github.gseitz" % "sbt-release"         % BuildInfo.sbtReleaseVersion)
addSbtPlugin("com.codecommit"    % "sbt-github-packages" % BuildInfo.sbtGitHubPackagesVersion)
addSbtPlugin("com.dwijnand"      % "sbt-travisci"        % BuildInfo.sbtTravisCiVersion)
addSbtPlugin("de.heikoseeberger" % "sbt-header"          % BuildInfo.sbtHeaderVersion)

organizationName := "SlamData Inc."
organizationHomepage := Some(url("http://slamdata.com"))

homepage := Some(url("https://github.com/slamdata/sbt-slamdata"))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/slamdata/sbt-slamdata"),
    "scm:git@github.com:slamdata/sbt-slamdata.git"))
