package slamdata

import sbt._, Keys._

import java.io.File

import com.typesafe.sbt.SbtPgp.autoImportImpl.PgpKeys
import de.heikoseeberger.sbtheader.HeaderKey.{createHeaders, headers}
import de.heikoseeberger.sbtheader.license.Apache2_0
import sbtrelease.ReleasePlugin.autoImport.{
  releaseCrossBuild, releasePublishArtifactsAction}
import wartremover.{wartremoverWarnings, Wart, Warts}

// Inspired by sbt-catalysts

object SbtSlamData extends AutoPlugin {
  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements

  object autoImport extends Base

  class Base extends Publish {
    val commonScalacOptions_2_10 = Seq(
      "-deprecation",
      "-encoding", "UTF-8",
      "-feature",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-unchecked",
      "-Xfatal-warnings",
      "-Xfuture",
      "-Xlint",
      "-Yno-adapted-args",
      "-Yno-imports",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard")

    lazy val commonBuildSettings = Seq(
      scalaOrganization := (
        scalaPartialVersion.value collect {
          case (2, 12)  => "org.typelevel"
        } getOrElse (scalaVersion.value match {
          case "2.11.7" => "org.typelevel"
          case "2.11.8" => "org.typelevel"
          case _        => "org.scala-lang"
        })),
      headers := Map(
        ("scala", Apache2_0("2014–2017", "SlamData Inc.")),
        ("java",  Apache2_0("2014–2017", "SlamData Inc."))),
      licenses += (("Apache 2", url("http://www.apache.org/licenses/LICENSE-2.0"))),
      checkHeaders := {
        if ((createHeaders in Compile).value.nonEmpty) sys.error("headers not all present")
      },
      outputStrategy := Some(StdoutOutput),
      autoCompilerPlugins := true,
      autoAPIMappings := true,
      resolvers ++= Seq(
        Resolver.sonatypeRepo("releases"),
        Resolver.sonatypeRepo("snapshots"),
        "JBoss repository" at "https://repository.jboss.org/nexus/content/repositories/",
        "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
        "bintray/non" at "http://dl.bintray.com/non/maven"),
      addCompilerPlugin("org.spire-math"  %% "kind-projector" % "0.9.3"),
      addCompilerPlugin("org.scalamacros" %  "paradise"       % "2.1.0" cross CrossVersion.full),
      scalacOptions ++= commonScalacOptions_2_10,
      scalacOptions ++= post210(scalaVersion.value, Seq(
        "-Ydelambdafy:method",
        "-Yliteral-types",
        "-Ypartial-unification",
        "-Ywarn-unused-import")),
      scalacOptions in (Test, console) --= Seq(
        "-Yno-imports",
        "-Ywarn-unused-import"),
      scalacOptions in (Compile, doc) -= "-Xfatal-warnings",
      wartremoverWarnings in (Compile, compile) ++= Warts.allBut(
        Wart.Any,                   // - see puffnfresh/wartremover#263
        Wart.ExplicitImplicitTypes, // - see puffnfresh/wartremover#226
        Wart.ImplicitConversion,    // - see mpilquist/simulacrum#35
        Wart.Nothing,               // - see puffnfresh/wartremover#263
        Wart.Overloading),           // Falsely triggers on 2.10
      wartremoverWarnings in (Compile, compile) ++=
        post210(scalaVersion.value, Seq(Wart.Overloading))
    )

    lazy val scalaPartialVersion = Def setting (CrossVersion partialVersion scalaVersion.value)

    def post210[A](version: String, settings: Seq[A]): Seq[A] =
      CrossVersion.partialVersion(version) match {
        case Some((2, 11)) | Some((2, 12)) => settings
        case _                             => Nil
      }
  }

  lazy val transferPublishAndTagResources = {
    lazy val transferPublishAndTagResources = taskKey[Unit](
      "Transfers publishAndTag script and associated resources")

    transferPublishAndTagResources := {
      val log = streams.value.log

      val baseDir = (baseDirectory in ThisBuild).value

      def transfer(src: String, dst: File) = {
        val srcʹ = getClass.getClassLoader.getResourceAsStream(src)

        log.info(s"transferring $src to $dst")

        IO.transfer(srcʹ, dst)
      }

      transfer("publishAndTag",       baseDir / "scripts" / "publishAndTag")
      transfer("credentials.sbt.enc", baseDir / "credentials.sbt.enc")
      transfer("pubring.pgp.enc",     baseDir / "pubring.pgp.enc")
      transfer("secring.pgp.enc",     baseDir / "secring.pgp.enc")
    }
  }
}
