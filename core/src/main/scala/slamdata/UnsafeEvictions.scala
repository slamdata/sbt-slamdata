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
import librarymanagement.{ModuleFilter, ModuleDescriptor, ScalaModuleInfo}

final class UnsafeEvictionsExceptions(val prefix: String, val evicteds: Seq[EvictionPair])
    extends RuntimeException(s"$prefix: ${evicteds.map(e => s"${e.organization}:${e.name}").mkString(", ")}")

object UnsafeEvictions {
  /** Performs logging and exception-throwing given report and configurations */
  def check(module: ModuleDescriptor,
      isFatal: Boolean,
      conf: Seq[(ModuleFilter, VersionNumberCompatibility)],
      evictionWarningOptions: EvictionWarningOptions,
      report: UpdateReport,
      log: Logger): UpdateReport = {
    import sbt.util.ShowLines._

    val ewo = evictionWarningOptions.withGuessCompatible(guessCompatible(conf))
    val ew = EvictionWarning(module, ewo, report)
    ew.lines.foreach(log.error(_))
    if (isFatal && ew.binaryIncompatibleEvictionExists) {
      val evictions = ew.scalaEvictions ++ ew.directEvictions ++ ew.transitiveEvictions
      // FIXME: doesn't work!
      // throw new UnsafeEvictionsException("Unsafe evictions detected", evictions)
      sys.error("Unsafe evictions detected: " +
        evictions.map(e => s"${e.organization}:${e.name}").mkString(", "))
    }
    report
  }

  /** Applies compatibility configuration, and, otherwise, assume it's compatible */
  private def guessCompatible(confs: Seq[(ModuleFilter, VersionNumberCompatibility)])
      : ((ModuleID, Option[ModuleID], Option[ScalaModuleInfo])) => Boolean = {
    case (m1, Some(m2), _) =>
      confs
        .find(conf => conf._1(m1))
        .map {
          case (_, vnc) =>
            val r1 = VersionNumber(m1.revision)
            val r2 = VersionNumber(m2.revision)
            vnc.isCompatible(r1, r2)
        }
        .getOrElse(true)
    case _ => true
  }

  /** Creates a ModuleFilter that does a strict organization matching */
  trait IsOrg {
    import sbt.librarymanagement.DependencyFilter

    /** Creates a ModuleFilter that does a strict organization matching */
    def apply(org: String): ModuleFilter = DependencyFilter.fnToModuleFilter(_.organization == org)
  }

  object IsOrg extends IsOrg
}

