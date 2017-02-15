import sbtslamdata.BuildInfo

lazy val root = Project("root", file("."))
  .settings(
    name         := "sbt-slamdata",
    organization := "com.slamdata",
    description  := "Common build configuration for SBT projects",
    sbtPlugin    := true)
  .settings(
    addSbtPlugin("com.jsuereth"      % "sbt-pgp"         % BuildInfo.sbtPgpVersion),
    addSbtPlugin("com.github.gseitz" % "sbt-release"     % BuildInfo.sbtReleaseVersion),
    addSbtPlugin("com.dwijnand"      % "sbt-travisci"    % BuildInfo.sbtTravisCiVersion),
    addSbtPlugin("de.heikoseeberger" % "sbt-header"      % "1.7.0"),
    addSbtPlugin("org.wartremover"   % "sbt-wartremover" % "1.2.1"))
  .settings(Publish.commonPublishSettings)
