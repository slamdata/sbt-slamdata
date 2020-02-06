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

import com.typesafe.sbt.pgp.PgpKeys._

import sbttravisci.TravisCiPlugin.autoImport._

import scala.collection.immutable.{List, Seq}

object SbtSlamDataPlugin extends SbtSlamDataBase {

  object autoImport extends autoImport {

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
        )),

      pgpPublicRing in Global := {
        if (isTravisBuild.value)
          file("./project/local.pubring.pgp")
        else
          (pgpPublicRing in Global).value
      },

      pgpSecretRing in Global := {
        if (isTravisBuild.value)
          file("./project/local.secring.pgp")
        else
          (pgpSecretRing in Global).value
      })

    lazy val noPublishSettings = Seq(
      publish := {},
      publishLocal := {},
      publishArtifact := false,
      skip in publish := true)
  }

  protected val autoImporter = autoImport
}
