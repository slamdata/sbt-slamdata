package slamdata

import sbt._, Keys._

import java.io.File
import java.nio.file.attribute.PosixFilePermission, PosixFilePermission.OWNER_EXECUTE
import java.nio.file.Files
import scala.collection.JavaConverters._

import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport.{headerCreate, headerLicense, HeaderLicense}
import sbttravisci.TravisCiPlugin.autoImport._
import wartremover.{wartremoverWarnings, Wart, Warts}

// Inspired by sbt-catalysts

object SbtSlamData extends AutoPlugin {
  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements

  object autoImport extends Base

  class Base extends Publish {
    lazy val transferPublishAndTagResources = taskKey[Unit](
      "Transfers publishAndTag script and associated resources")

    val scalacOptions_2_10 = Seq(
      "-deprecation",
      "-encoding", "UTF-8",
      "-feature",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-unchecked",
      "-Xfuture",
      "-Xlint",
      "-Yno-adapted-args",
      "-Yno-imports",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard")

    val scalacOptions_2_11 = Seq(
      "-Ydelambdafy:method",
      "-Ypartial-unification",
      "-Ywarn-unused-import")

    val scalacOptions_2_12 = Seq("-Xstrict-patmat-analysis")

    val headerLicenseSettings = Seq(
      headerLicense := Some(HeaderLicense.ALv2("2014–2018", "SlamData Inc.")),
      licenses += (("Apache 2", url("http://www.apache.org/licenses/LICENSE-2.0"))),
      checkHeaders := {
        if ((headerCreate in Compile).value.nonEmpty) sys.error("headers not all present")
      })

    lazy val commonBuildSettings = Seq(
      outputStrategy := Some(StdoutOutput),
      autoCompilerPlugins := true,
      autoAPIMappings := true,

      resolvers ++= Seq(
        Resolver.sonatypeRepo("releases"),
        Resolver.sonatypeRepo("snapshots"),
        "JBoss repository" at "https://repository.jboss.org/nexus/content/repositories/",
        Resolver.bintrayRepo("scalaz", "releases"),
        Resolver.bintrayRepo("non", "maven"),
        Resolver.bintrayRepo("slamdata-inc", "maven-public"),
        Resolver.bintrayRepo("slamdata-inc", "maven-private")),

      addCompilerPlugin("org.spire-math"  %% "kind-projector" % "0.9.4"),
      addCompilerPlugin("org.scalamacros" %  "paradise"       % "2.1.0" cross CrossVersion.patch),

      scalacOptions := (CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 12)) => scalacOptions_2_10 ++ scalacOptions_2_11 ++ scalacOptions_2_12
        case Some((2, 11)) => scalacOptions_2_10 ++ scalacOptions_2_11
        case _             => scalacOptions_2_10
      }),

      scalacOptions ++= {
        if (isTravisBuild.value)
          Seq("-Xfatal-warnings")
        else
          Seq()
      },

      scalacOptions in (Test, console) --= Seq(
        "-Yno-imports",
        "-Ywarn-unused-import"),

      scalacOptions in (Compile, doc) -= "-Xfatal-warnings",

      wartremoverWarnings in (Compile, compile) ++= Warts.allBut(
        Wart.Any,                   // - see puffnfresh/wartremover#263
        Wart.ExplicitImplicitTypes, // - see puffnfresh/wartremover#226
        Wart.ImplicitConversion,    // - see mpilquist/simulacrum#35
        Wart.Nothing),              // - see puffnfresh/wartremover#263
      wartremoverWarnings in (Compile, compile) --=
        (CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, 11)) | Some((2, 12)) => Nil
          case _                             => Seq(Wart.Overloading) // Falsely triggers on 2.10
        })
    ) ++ headerLicenseSettings
  }

  import autoImport._

  override def buildSettings = Seq(
    transferPublishAndTagResources := {
      val log = streams.value.log

      val baseDir = (baseDirectory in ThisBuild).value

      def transfer(src: String, dst: File, permissions: Set[PosixFilePermission] = Set()) = {
        val srcʹ = getClass.getClassLoader.getResourceAsStream(src)

        log.info(s"transferring $src to $dst")

        IO.transfer(srcʹ, dst)

        Files.setPosixFilePermissions(
          dst.toPath,
          (Files.getPosixFilePermissions(dst.toPath).asScala ++ permissions).asJava)
      }

      def transferToBaseDir(srcs: String*) = srcs.foreach(src => transfer(src, baseDir / src))
      def transferScripts(srcs: String*) = srcs.foreach(src => transfer(src, baseDir / "scripts" / src, Set(OWNER_EXECUTE)))

      transferScripts("publishAndTag", "bumpDependentProject", "readVersion")
      transferToBaseDir(
        "pubring.pgp.enc",
        "secring.pgp.enc",
        "pgppassphrase.sbt.enc",
        "credentials.bintray.enc",
        "credentials.sonatype.enc"
      )
    })

  override def projectSettings = commonBuildSettings ++ commonPublishSettings
}
