package slamdata

import sbt._, Keys._
import bintray.BintrayKeys._
import com.typesafe.sbt.SbtPgp.autoImportImpl.PgpKeys
import sbtrelease.ReleasePlugin.autoImport.{ releaseCrossBuild, releasePublishArtifactsAction }
import scala.concurrent.duration._

class Publish {
  lazy val checkHeaders = taskKey[Unit]("Fail the build if createHeaders is not up-to-date")

  lazy val publishAsOSSProject = settingKey[Boolean](
    "Determines if project should be released publicly both to bintray and maven or only to a private bintray repository")

  lazy val synchronizeWithSonatypeStaging = taskKey[Unit]("Synchronize artifacts published on bintray sonatype staging repository")
  lazy val releaseToMavenCentral = taskKey[Unit]("Close the sonatype staging repository")
  lazy val performMavenCentralSync = settingKey[Boolean]("If true, then project will be sync'd from maven-public to Maven Central")

  lazy val commonPublishSettings = Seq(
    licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    publishAsOSSProject := true,
    performMavenCentralSync := publishAsOSSProject.value && !sbtPlugin.value,
    bintrayRepository := { if (publishAsOSSProject.value) "maven-public" else "maven-private" },
    bintrayReleaseOnPublish := false,
    synchronizeWithSonatypeStaging := mavenCentralRelatedTask(bintraySyncSonatypeStaging).value,
    releaseToMavenCentral := mavenCentralRelatedTask(bintraySyncMavenCentral).value,
    bintraySyncMavenCentralRetries := Seq(5.seconds, 1.minute, 5.minutes),
    publishMavenStyle := true,
    bintrayOrganization := Some("slamdata-inc"),
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    releaseCrossBuild := true,
    autoAPIMappings := true,
    developers := List(
      Developer(
        id = "slamdata",
        name = "SlamData Inc.",
        email = "contact@slamdata.com",
        url = new URL("http://slamdata.com")
      )
    ),
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    PgpKeys.pgpPublicRing in Global := file("./project/local.pubring.pgp"),
    PgpKeys.pgpSecretRing in Global := file("./project/local.secring.pgp"),
    bintrayCredentialsFile := file("./local.credentials.bintray"),
    credentials ++= Seq(file("./local.credentials.sonatype")).filter(_.exists).map(Credentials(_))
  )

  lazy val noPublishSettings = Seq(
    publish := {},
    publishLocal := {},
    publishArtifact := false,
    skip in publish := true
  )

  private def mavenCentralRelatedTask(task: TaskKey[Unit]): Def.Initialize[Task[Unit]] = Def.taskDyn {
    if (performMavenCentralSync.value) Def.task(task.value) else Def.task(())
  }

}

object Publish extends Publish
