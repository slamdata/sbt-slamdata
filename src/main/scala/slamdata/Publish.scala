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

import com.typesafe.sbt.SbtPgp.autoImportImpl.PgpKeys

import sbtghpackages.GitHubPackagesKeys._
import sbttravisci.TravisCiPlugin, TravisCiPlugin.autoImport._

import scala.{Boolean, Unit}
import scala.collection.immutable.{List, Seq}

class Publish {
  lazy val checkHeaders = taskKey[Unit]("Fail the build if createHeaders is not up-to-date")

  lazy val publishAsOSSProject = settingKey[Boolean](
    "Determines if project should be released publicly both to bintray and maven or only to a private bintray repository")

  lazy val synchronizeWithSonatypeStaging = taskKey[Unit]("Synchronize artifacts published on bintray sonatype staging repository")
  lazy val releaseToMavenCentral = taskKey[Unit]("Close the sonatype staging repository")
  lazy val performMavenCentralSync = settingKey[Boolean]("If true, then project will be sync'd from maven-public to Maven Central")

  lazy val commonPublishSettings = Seq(
    licenses := Seq(("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))),

    publishAsOSSProject := true,
    performMavenCentralSync := false,

    githubOwner := "slamdata",
    githubRepository := { if (publishAsOSSProject.value) "public" else "private" },

    synchronizeWithSonatypeStaging := {},
    releaseToMavenCentral := {},
    autoAPIMappings := true,

    developers := List(
      Developer(
        id = "slamdata",
        name = "SlamData Inc.",
        email = "contact@slamdata.com",
        url = new URL("http://slamdata.com")
      )),

    PgpKeys.pgpPublicRing in Global := {
      if (isTravisBuild.value)
        file("./project/local.pubring.pgp")
      else
        (PgpKeys.pgpPublicRing in Global).value
    },

    PgpKeys.pgpSecretRing in Global := {
      if (isTravisBuild.value)
        file("./project/local.secring.pgp")
      else
        (PgpKeys.pgpSecretRing in Global).value
    })

  lazy val noPublishSettings = Seq(
    publish := {},
    publishLocal := {},
    publishArtifact := false,
    skip in publish := true)
}

object Publish extends Publish
