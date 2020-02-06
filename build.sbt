ThisBuild / organization := "com.slamdata"

description := "Common build configuration for SBT projects"

ThisBuild / sbtPlugin := true
ThisBuild / sbtVersion := "1.3.8"

lazy val root = project
  .in(file("."))
  .aggregate(core, artifact, plugin)
  .settings(noPublishSettings)

lazy val core = project.in(file("core"))
  .settings(
    name := "sbt-slamdata-core",
    scalacStrictMode := false)

lazy val artifact = project.in(file("artifact"))
  .dependsOn(core)
  .settings(name := "sbt-slamdata")

lazy val plugin = project.in(file("plugin"))
  .dependsOn(core)
  .settings(name := "sbt-slamdata-plugin")

ThisBuild / organizationName := "SlamData Inc."
ThisBuild / organizationHomepage := Some(url("https://slamdata.com"))
