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

import bintray.BintrayKeys._

import sbttravisci.TravisCiPlugin.autoImport._

import scala.Some
import scala.collection.immutable.Seq

object SbtSlamDataPlugin extends SbtSlamDataBase {

  object autoImport extends autoImport {

    lazy val noPublishSettings = Seq(
      publish := {},
      publishLocal := {},
      bintrayRelease := {},
      publishArtifact := false,
      skip in publish := true,
      bintrayEnsureBintrayPackageExists := {})
  }

  import autoImport._

  override def projectSettings =
    super.projectSettings ++
    addCommandAlias("releaseSnapshot", "; project /; reload; checkLocalEvictions; bintrayEnsureBintrayPackageExists; publishSigned; bintrayRelease") ++
    Seq(
      sbtPlugin := true,

      bintrayOrganization := Some("slamdata-inc"),
      bintrayRepository := "sbt-plugins",
      bintrayReleaseOnPublish := false,

      publishMavenStyle := false,

      bintrayCredentialsFile := {
        if (isTravisBuild.value)
          file("./local.credentials.bintray")
        else
          bintrayCredentialsFile.value
      },

      transferPublishAndTagResources := {
        transferToBaseDir((ThisBuild / baseDirectory).value, "credentials.bintray.enc")
        transferPublishAndTagResources.value
      })

  protected val autoImporter = autoImport
}
