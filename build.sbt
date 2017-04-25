import sbtslamdata.BuildInfo
import slamdata.Publish

lazy val root = Project("root", file("."))
  .settings(
    name         := "sbt-slamdata",
    organization := "com.slamdata",
    description  := "Common build configuration for SBT projects",
    sbtPlugin    := true)
  .settings(
    addSbtPlugin("com.jsuereth"      % "sbt-pgp"         % BuildInfo.sbtPgpVersion),
    addSbtPlugin("com.github.gseitz" % "sbt-release"     % BuildInfo.sbtReleaseVersion),
    addSbtPlugin("org.xerial.sbt"    % "sbt-sonatype"    % BuildInfo.sbtSonatypeVersion),
    addSbtPlugin("com.dwijnand"      % "sbt-travisci"    % BuildInfo.sbtTravisCiVersion),
    addSbtPlugin("de.heikoseeberger" % "sbt-header"      % "1.7.0"),
    addSbtPlugin("org.wartremover"   % "sbt-wartremover" % "2.0.3"))
  .settings(publishSettings)

lazy val publishSettings = Publish.commonPublishSettings ++ Seq(
  organizationName := "SlamData Inc.",
  organizationHomepage := Some(url("http://slamdata.com")),
  homepage := Some(url("https://github.com/slamdata/sbt-slamdata")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/slamdata/sbt-slamdata"),
      "scm:git@github.com:slamdata/sbt-slamdata.git")))
