/*
 * Copyright 2014–2020 SlamData Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package slamdata

import sbt._, Keys._
import sbt.complete.DefaultParsers.fileParser

import de.heikoseeberger.sbtheader.AutomateHeaderPlugin
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport._

import _root_.io.crashbox.gpg.SbtGpg

import sbttravisci.TravisCiPlugin, TravisCiPlugin.autoImport._

import org.yaml.snakeyaml.Yaml

import scala.{sys, Boolean, None, Some, StringContext}
import scala.collection.immutable.{Set, Seq}
import scala.collection.JavaConverters._
import scala.sys.process._

import java.io.File
import java.lang.{String, System}
import java.nio.file.attribute.PosixFilePermission, PosixFilePermission.OWNER_EXECUTE
import java.nio.file.Files

abstract class SbtSlamDataBase extends AutoPlugin {
  private var foundLocalEvictions: Set[(String, String)] = Set()

  override def requires =
    plugins.JvmPlugin &&
    TravisCiPlugin &&
    SbtGpg

  override def trigger = allRequirements

  class autoImport extends SbtSlamDataKeys {
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

    def scalacOptions_2_13(strict: Boolean) = {
      val numCPUs = java.lang.Runtime.getRuntime.availableProcessors()
      Seq(
        s"-Ybackend-parallelism", numCPUs.toString,
        "-Wunused:imports",
        "-Wdead-code",
        "-Wnumeric-widen",
        "-Wvalue-discard")
    }

    val scalacOptionsRemoved_2_13 =
      Seq(
        "-Yno-adapted-args",
        "-Ywarn-unused-import",
        "-Ywarn-value-discard",
        "-Ywarn-numeric-widen",
        "-Ywarn-dead-code",
        "-Xfuture")

    val headerLicenseSettings = Seq(
      headerLicense := Some(HeaderLicense.ALv2("2014–2020", "SlamData Inc.")),
      licenses += (("Apache 2", url("http://www.apache.org/licenses/LICENSE-2.0"))),
      checkHeaders := {
        if ((headerCreate in Compile).value.nonEmpty) sys.error("headers not all present")
      })

    lazy val commonBuildSettings = Seq(
      outputStrategy := Some(StdoutOutput),
      autoCompilerPlugins := true,
      autoAPIMappings := true,

      addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.11.0" cross CrossVersion.full),
      addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),

      // default to true
      scalacStrictMode := true,

      scalacOptions ++= {
        val strict = scalacStrictMode.value

        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, 13)) =>
            val mainline = scalacOptions_2_10(strict) ++
              scalacOptions_2_11(strict) ++
              scalacOptions_2_12(strict) ++
              scalacOptions_2_13(strict)

            mainline.filterNot(scalacOptionsRemoved_2_13.contains)

          case Some((2, 12)) => scalacOptions_2_10(strict) ++ scalacOptions_2_11(strict) ++ scalacOptions_2_12(strict)

          case Some((2, 11)) => scalacOptions_2_10(strict) ++ scalacOptions_2_11(strict)

          case _ => scalacOptions_2_10(strict)
        }
      },

      scalacOptions --= {
        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, n)) if n >= 13 => Some("-Ypartial-unification")
          case _ => None
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
    ) ++ headerLicenseSettings

    lazy val commonPublishSettings = Seq(
      licenses := Seq(("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))),

      publishAsOSSProject := true,
      performMavenCentralSync := false,

      synchronizeWithSonatypeStaging := {},
      releaseToMavenCentral := {},
      autoAPIMappings := true,

      developers := List(
        Developer(
          id = "slamdata",
          name = "SlamData Inc.",
          email = "contact@slamdata.com",
          url = new URL("http://slamdata.com")
        )))

    implicit final class ProjectSyntax(val self: Project) {
      def evictToLocal(envar: String, subproject: String, test: Boolean = false): Project = {
        val eviction = sys.env.get(envar).map(file).filter(_.exists()) map { f =>
          foundLocalEvictions += ((envar, subproject))

          val ref = ProjectRef(f, subproject)
          self.dependsOn(if (test) ref % "test->test;compile->compile" else ref)
        }

        eviction.getOrElse(self)
      }
    }
  }

  protected val autoImporter: autoImport
  import autoImporter._

  override def globalSettings = Seq(
    concurrentRestrictions in Global := {
      val oldValue = (concurrentRestrictions in Global).value
      val maxTasks = 2
      if (isTravisBuild.value)
      // Recreate the default rules with the task limit hard-coded:
        Seq(Tags.limitAll(maxTasks), Tags.limit(Tags.ForkedTestGroup, 1))
      else
        oldValue
    },

    // Tasks tagged with `ExclusiveTest` should be run exclusively.
    concurrentRestrictions in Global += Tags.exclusive(ExclusiveTest))

  override def buildSettings =
    addCommandAlias("ci", "; checkHeaders; test") ++
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

      checkLocalEvictions := {
        if (!foundLocalEvictions.isEmpty) {
          sys.error(s"found active local evictions: ${foundLocalEvictions.mkString("[", ", ", "]")}; publication is disabled")
        }
      },

      transferPublishAndTagResources := {
        val baseDir = (ThisBuild / baseDirectory).value

        transferScripts(
          baseDir,
          "core/publishAndTag",
          "core/bumpDependentProject",
          "core/readVersion",
          "core/isRevision")

        transferToBaseDir(
          baseDir,
          "core/signing-secret.pgp.enc")
      },

      transferCommonResources := {
        val baseDir = (baseDirectory in ThisBuild).value

        transferScripts(
          baseDir,
          "core/checkAndAutoMerge",
          "core/commonSetup",
          "core/discordTravisPost",
          "core/listLabels",
          "core/closePR")
      },

      secrets := Seq(baseDirectory.value / ".secrets.yml.enc"),

      exportSecretsForActions := {
        val pwd = sys.env("ENCRYPTION_PASSWORD")
        if (pwd == null) {
          sys.error("$ENCRYPTION_PASSWORD not set")
        }

        val yaml = new Yaml

        secrets.value foreach { file =>
          val decrypted = s"""openssl aes-256-cbc -pass env:ENCRYPTION_PASSWORD -in ${file.getAbsolutePath()} -d""".!!
          val parsed = yaml.load[Any](decrypted)
            .asInstanceOf[java.util.Map[String, String]]
            .asScala
            .toMap   // yolo

          parsed foreach {
            case (key, value) =>
              println(s"::add-mask::$value")
              println(s"::set-env name=$key::$value")
          }
        }
      },

      decryptSecret / aggregate := false,
      decryptSecret := {
        val file = fileParser(baseDirectory.value).parsed
        val log = streams.value.log
        val plogger = ProcessLogger(log.info(_), log.error(_))
        val ecode =
          s"""openssl aes-256-cbc -pass env:ENCRYPTION_PASSWORD -in ${file} -out ${file.getPath().replaceAll("\\.enc$", "")} -d""".!(plogger)

        if (ecode != 0) {
          sys.error(s"openssl exited with status $ecode")
        } else {
          file.delete()
        }
      },

      encryptSecret / aggregate := false,
      encryptSecret := {
        val file = fileParser(baseDirectory.value).parsed
        val log = streams.value.log
        val plogger = ProcessLogger(log.info(_), log.error(_))

        val ecode =
          s"""openssl aes-256-cbc -pass env:ENCRYPTION_PASSWORD -in ${file} -out ${file}.enc""".!(plogger)

        if (ecode != 0) {
          sys.error(s"openssl exited with status $ecode")
        } else {
          file.delete()
        }
      })

  private def isWindows(): Boolean = System.getProperty("os.name").startsWith("Windows")

  private def transfer(src: String, dst: File, permissions: Set[PosixFilePermission] = Set()) = {
    val src2 = getClass.getClassLoader.getResourceAsStream(src)

    IO.transfer(src2, dst)

    if (!isWindows()) {
      Files.setPosixFilePermissions(
        dst.toPath,
        (Files.getPosixFilePermissions(dst.toPath).asScala ++ permissions).asJava)
    }
  }

  protected def transferToBaseDir(baseDir: File, srcs: String*) =
    srcs.foreach(src => transfer(src, baseDir / src))

  protected def transferScripts(baseDir: File, srcs: String*) =
    srcs.foreach(src => transfer(src, baseDir / "scripts" / src, Set(OWNER_EXECUTE)))

  override def projectSettings =
    AutomateHeaderPlugin.projectSettings ++
    commonBuildSettings ++
    commonPublishSettings ++
    Seq(
      version := {
        import scala.sys.process._

        val currentVersion = version.value
        if (!isTravisBuild.value)
          currentVersion + "-" + "git rev-parse HEAD".!!.substring(0, 7)
        else
          currentVersion
      },
      conflictManager := {
        val currentManager = conflictManager.value
        if (isTravisBuild.value)
          ConflictManager.strict.withOrganization("com.slamdata")
        else
          currentManager
      })
}
