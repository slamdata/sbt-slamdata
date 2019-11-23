import sbtslamdata.BuildInfo
import slamdata.Publish

lazy val root = (project in file("."))
  .settings(
    name         := "sbt-slamdata",
    organization := "com.slamdata",
    description  := "Common build configuration for SBT projects",
    sbtPlugin    := true,
    sbtVersion in Global := "1.3.0")
  .settings(
    addSbtPlugin("com.jsuereth"      % "sbt-pgp"         % BuildInfo.sbtPgpVersion),
    addSbtPlugin("com.github.gseitz" % "sbt-release"     % BuildInfo.sbtReleaseVersion),
    addSbtPlugin("org.foundweekends" % "sbt-bintray"     % BuildInfo.sbtBintrayVersion),
    addSbtPlugin("com.dwijnand"      % "sbt-travisci"    % BuildInfo.sbtTravisCiVersion),
    addSbtPlugin("de.heikoseeberger" % "sbt-header"      % "5.3.0"))
  .settings(publishSettings)

lazy val publishSettings = Publish.commonPublishSettings ++ Seq(
  organizationName := "SlamData Inc.",
  organizationHomepage := Some(url("http://slamdata.com")),
  homepage := Some(url("https://github.com/slamdata/sbt-slamdata")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/slamdata/sbt-slamdata"),
      "scm:git@github.com:slamdata/sbt-slamdata.git")))
