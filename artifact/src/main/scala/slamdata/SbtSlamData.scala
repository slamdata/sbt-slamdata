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

import sbtghpackages.{GitHubPackagesKeys, GitHubPackagesPlugin}, GitHubPackagesKeys._

import scala.collection.immutable.{List, Seq}

object SbtSlamData extends SbtSlamDataBase {

  override def requires = super.requires && GitHubPackagesPlugin

  object autoImport extends autoImport {

    lazy val noPublishSettings = Seq(
      publish := {},
      publishLocal := {},
      publishArtifact := false,
      skip in publish := true)
  }

  import autoImport._

  override def projectSettings =
    super.projectSettings ++
    addCommandAlias("releaseSnapshot", "; project /; reload; checkLocalEvictions; +publishSigned") ++
    Seq(
      githubOwner := "slamdata",
      githubRepository := { if (publishAsOSSProject.value) "public" else "private" })

  protected val autoImporter = autoImport
}
