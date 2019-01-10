package slamdata

import sbt._, Keys._

import java.io.File
import java.nio.file.attribute.PosixFilePermission, PosixFilePermission.OWNER_EXECUTE
import java.nio.file.Files
import scala.collection.JavaConverters._

import bintray.{BintrayKeys, BintrayPlugin}, BintrayKeys._
import com.typesafe.sbt.SbtPgp
import com.typesafe.sbt.pgp.PgpKeys._
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport.{headerCreate, headerLicense, HeaderLicense}
import sbttravisci.TravisCiPlugin, TravisCiPlugin.autoImport._
import wartremover.{wartremoverWarnings, Wart, Warts}

// Inspired by sbt-catalysts

object SbtSlamData extends AutoPlugin {
  private var foundLocalEvictions: Set[(String, String)] = Set()

  override def requires =
    plugins.JvmPlugin &&
    BintrayPlugin &&
    TravisCiPlugin &&
    SbtPgp

  override def trigger = allRequirements

  object autoImport extends Base

  class Base extends Publish {
    lazy val checkLocalEvictions = taskKey[Unit](
      "Checks for the existence of local evictions in the build and fails if they are found")

    lazy val transferPublishAndTagResources = taskKey[Unit](
      "Transfers publishAndTag script and associated resources")

    lazy val transferCommonResources = taskKey[Unit](
      "Transfers common resources not used in publication")

    lazy val scalacStrictMode = settingKey[Boolean](
      "Include stricter warnings and WartRemover settings")

    val BothScopes = "test->test;compile->compile"

    // Exclusive execution settings
    lazy val ExclusiveTests = config("exclusive") extend Test

    val ExclusiveTest = Tags.Tag("exclusive-test")

    def exclusiveTasks(tasks: Scoped*) =
      tasks.flatMap(inTask(_)(tags := Seq((ExclusiveTest, 1))))

    def scalacOptions_2_10(strict: Boolean) = {
      val global = Seq(
        "-encoding", "UTF-8",
        "-deprecation",
        "-language:existentials",
        "-language:higherKinds",
        "-language:implicitConversions",
        "-feature",
        "-Xlint")

      if (strict) {
        global ++ Seq(
          "-unchecked",
          "-Xfuture",
          "-Yno-adapted-args",
          "-Yno-imports",
          "-Ywarn-dead-code",
          "-Ywarn-numeric-widen",
          "-Ywarn-value-discard")
      } else {
        global
      }
    }

    def scalacOptions_2_11(strict: Boolean) = {
      val global = Seq(
        "-Ypartial-unification",
        "-Ywarn-unused-import")

      if (strict)
        global :+ "-Ydelambdafy:method"
      else
        global
    }

    def scalacOptions_2_12(strict: Boolean) = Seq("-target:jvm-1.8")

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

      addCompilerPlugin("org.spire-math"  %% "kind-projector" % "0.9.4"),
      addCompilerPlugin("org.scalamacros" %  "paradise"       % "2.1.0" cross CrossVersion.patch),

      // default to true
      scalacStrictMode := true,

      scalacOptions ++= {
        val strict = scalacStrictMode.value

        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, 12)) => scalacOptions_2_10(strict) ++ scalacOptions_2_11(strict) ++ scalacOptions_2_12(strict)
          case Some((2, 11)) => scalacOptions_2_10(strict) ++ scalacOptions_2_11(strict)
          case _ => scalacOptions_2_10(strict)
        }
      },

      scalacOptions ++= {
        if (isTravisBuild.value && scalacStrictMode.value)
          Seq("-Xfatal-warnings")
        else
          Seq()
      },

      scalacOptions in (Test, console) --= Seq(
        "-Yno-imports",
        "-Ywarn-unused-import"),

      scalacOptions in (Compile, doc) -= "-Xfatal-warnings",

      wartremoverWarnings in (Compile, compile) ++= {
        if (scalacStrictMode.value) {
          Warts.allBut(
            Wart.Any,                   // - see puffnfresh/wartremover#263
            Wart.ExplicitImplicitTypes, // - see puffnfresh/wartremover#226
            Wart.ImplicitConversion,    // - see mpilquist/simulacrum#35
            Wart.Nothing)               // - see puffnfresh/wartremover#263
        } else {
          Seq.empty
        }
      },

      wartremoverWarnings in (Compile, compile) --= {
        if (scalacStrictMode.value) {
          CrossVersion.partialVersion(scalaVersion.value) match {
            case Some((2, 11)) | Some((2, 12)) => Nil
            case _                             => Seq(Wart.Overloading) // Falsely triggers on 2.10
          }
        } else {
          Seq.empty
        }
      }
    ) ++ headerLicenseSettings

    implicit final class ProjectSyntax(val self: Project) {
      def evictToLocal(envar: String, subproject: String, test: Boolean = false): Project = {
        val eviction = Option(System.getenv(envar)).map(file).filter(_.exists()) map { f =>
          foundLocalEvictions += (envar -> subproject)

          val ref = ProjectRef(f, subproject)
          self.dependsOn(if (test) ref % "test->test;compile->compile" else ref)
        }

        eviction.getOrElse(self)
      }
    }
  }

  import autoImport._

  override def buildSettings =
    addCommandAlias("releaseSnapshot", "; project root; reload; checkLocalEvictions; publishSigned; bintrayRelease") ++
    Seq(
      organization := "com.slamdata",

      organizationName := "SlamData Inc.",
      organizationHomepage := Some(url("http://slamdata.com")),

      resolvers := Seq(
        Resolver.sonatypeRepo("releases"),
        Resolver.sonatypeRepo("snapshots"),
        "JBoss repository" at "https://repository.jboss.org/nexus/content/repositories/",
        Resolver.bintrayRepo("scalaz", "releases"),
        Resolver.bintrayRepo("non", "maven"),
        Resolver.bintrayRepo("slamdata-inc", "maven-public"),
        Resolver.bintrayRepo("slamdata-inc", "maven-private")),

      concurrentRestrictions := {
        val maxTasks = 2
        if (isTravisBuild.value)
          // Recreate the default rules with the task limit hard-coded:
          Seq(Tags.limitAll(maxTasks), Tags.limit(Tags.ForkedTestGroup, 1))
        else
          concurrentRestrictions.value
      },

      // Tasks tagged with `ExclusiveTest` should be run exclusively.
      concurrentRestrictions += Tags.exclusive(ExclusiveTest),

      useGpg in Global := {
        val oldValue = (useGpg in Global).value
        !isTravisBuild.value || oldValue
      },

      checkLocalEvictions := {
        if (!foundLocalEvictions.isEmpty) {
          sys.error(s"found active local evictions: ${foundLocalEvictions.mkString("[", ", ", "]")}; publication is disabled")
        }
      },

      transferPublishAndTagResources := {
        val baseDir = (baseDirectory in ThisBuild).value

        transferScripts(
          baseDir,
          "publishAndTag",
          "bumpDependentProject",
          "readVersion",
          "isRevision")

        transferToBaseDir(
          baseDir,
          "pubring.pgp.enc",
          "secring.pgp.enc",
          "pgppassphrase.sbt.enc",
          "credentials.bintray.enc",
          "credentials.sonatype.enc")
      },

      transferCommonResources := {
        val baseDir = (baseDirectory in ThisBuild).value

        transferScripts(
          baseDir,
          "checkAndAutoMerge",
          "commonSetup",
          "discordTravisPost",
          "listLabels")
      })

  private def transfer(src: String, dst: File, permissions: Set[PosixFilePermission] = Set()) = {
    val src2 = getClass.getClassLoader.getResourceAsStream(src)

    IO.transfer(src2, dst)

    Files.setPosixFilePermissions(
      dst.toPath,
      (Files.getPosixFilePermissions(dst.toPath).asScala ++ permissions).asJava)
  }

  private def transferToBaseDir(baseDir: File, srcs: String*) =
    srcs.foreach(src => transfer(src, baseDir / src))

  private def transferScripts(baseDir: File, srcs: String*) =
    srcs.foreach(src => transfer(src, baseDir / "scripts" / src, Set(OWNER_EXECUTE)))

  override def projectSettings = commonBuildSettings ++ commonPublishSettings ++ Seq(
    version := {
      import scala.sys.process._

      val currentVersion = version.value
      if (!isTravisBuild.value)
        currentVersion + "-" + "git rev-parse HEAD".!!.substring(0, 7)
      else
        currentVersion
    })
}
