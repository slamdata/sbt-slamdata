package slamdata

import sbt._, Keys._

object CommonDependencies {
  val argonautVersion       = "6.2-RC2"
  val monocleVersion        = "1.4.0"
  val scalacheckVersion     = "1.13.4"
  val scalazVersion         = "7.2.8"
  val scalazSpecs2Version   = "0.5.0"
  val simulacrumVersion     = "0.10.0"
  val slamdataPredefVersion = "0.0.2"
  val specs2Version         = "3.8.7"

  object argonaut {
    val argonaut     = "io.argonaut"                %% "argonaut"          % argonautVersion
  }

  object scalacheck {
    val scalacheck   = "org.scalacheck"             %% "scalacheck"        % scalacheckVersion
  }

  object scalaz {
    val core         = "org.scalaz"                 %% "scalaz-core"       % scalazVersion
  }

  object simulacrum {
    val simulacrum   = "com.github.mpilquist"       %% "simulacrum"        % simulacrumVersion
  }

  object slamdata {
    val predef       = "com.slamdata"               %% "slamdata-predef"   % slamdataPredefVersion
  }

  object specs2 {
    val core         = "org.specs2"                 %% "specs2-core"       % specs2Version
    val scalacheck   = "org.specs2"                 %% "specs2-scalacheck" % specs2Version
  }

  object monocle {
    val core         = "com.github.julien-truffaut" %% "monocle-core"      % monocleVersion
    val law          = "com.github.julien-truffaut" %% "monocle-law"       % monocleVersion
  }

  object typelevel {
    val scalazSpecs2 = "org.typelevel"              %% "scalaz-specs2"     % scalazSpecs2Version
  }

}
