ThisBuild / organization := "com.slamdata"

description := "Common build configuration for SBT projects"

ThisBuild / sbtVersion := "1.3.8"
ThisBuild / scalaVersion := "2.12.10"

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
ThisBuild / homepage := Some(url("https://github.com/slamdata/sbt-slamdata"))
ThisBuild / scmInfo := Some(ScmInfo(homepage.value.get, "scm:git@github.com:slamdata/sbt-slamdata.git"))

