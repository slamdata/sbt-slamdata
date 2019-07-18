import sbtslamdata.BuildInfo
import slamdata.Publish

lazy val root = (project in file("."))
  .settings(
    name         := "sbt-slamdata",
    organization := "com.slamdata",
    description  := "Common build configuration for SBT projects",
    sbtPlugin    := true,
    sbtVersion in Global := {
      scalaBinaryVersion.value match {
        case "2.10" => "0.13.16"
        case "2.12" => "1.0.2"
      }
    })
  .settings(
    addSbtPlugin("com.jsuereth"      % "sbt-pgp"         % BuildInfo.sbtPgpVersion),
    addSbtPlugin("com.github.gseitz" % "sbt-release"     % BuildInfo.sbtReleaseVersion),
    addSbtPlugin("org.foundweekends" % "sbt-bintray"     % BuildInfo.sbtBintrayVersion),
    addSbtPlugin("com.dwijnand"      % "sbt-travisci"    % BuildInfo.sbtTravisCiVersion),
    addSbtPlugin("de.heikoseeberger" % "sbt-header"      % "5.0.2"),
    addSbtPlugin("org.wartremover"   % "sbt-wartremover" % "2.2.1"))
  .settings(publishSettings)

lazy val publishSettings = Publish.commonPublishSettings ++ Seq(
  organizationName := "SlamData Inc.",
  organizationHomepage := Some(url("http://slamdata.com")),
  homepage := Some(url("https://github.com/slamdata/sbt-slamdata")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/slamdata/sbt-slamdata"),
      "scm:git@github.com:slamdata/sbt-slamdata.git")))
