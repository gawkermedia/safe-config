import sbt._
import sbt.Keys._
import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform._
import ScalariformKeys._

import wartremover._

object Build extends Build {

  lazy val pomStuff = {
    <url>https://github.com/gawkermedia/safe-config</url>
    <licenses>
      <license>
        <name>BSD 3-Clause</name>
        <url>https://github.com/gawkermedia/safe-config/blob/master/LICENSE</url>
      </license>
    </licenses>
    <scm>
      <connection>git@github.com:gawkermedia/safe-config.git</connection>
      <developerConnection>scm:git:git@github.com:gawkermedia/safe-config.git</developerConnection>
      <url>git@github.com:gawkermedia/safe-config</url>
    </scm>
    <developers>
      <developer>
        <name>Chris Neveu</name>
      </developer>
      <developer>
        <name>Pedro Rodriguez</name>
      </developer>
    </developers>
  }

  lazy val base: Project = Project(
    "safe-config",
    file("."),
    settings = Defaults.defaultSettings ++ scalariformSettings ++ wartremoverSettings ++ Seq(
      organization := "com.kinja",
      version      := "1.1.1",
	  scalaVersion := "2.11.8",
      pomExtra := pomStuff,
      scalacOptions ++= Seq(
        "-deprecation",          // Show details of deprecation warnings.
        "-encoding", "UTF-8",    // Set correct encoding for Scaladoc.
        "-feature",              // Show details of feature warnings.
        "-language:higherKinds", // Enable higher-kinded types.
        "-language:postfixOps",  // We use this frequently for defining durations.
        "-language:reflectiveCalls",
        "-unchecked",            // Show details of unchecked warnings.
        "-Xfatal-warnings",      // All warnings should result in a compiliation failure.
        "-Xfuture",              // Disables view bounds, adapted args, and unsound pattern matching in 2.11.
        "-Xlint",                // Ensure best practices are being followed.
        "-Yno-adapted-args",     // Prevent implicit tupling of arguments.
        "-Ywarn-dead-code",      // Fail when dead code is present. Prevents accidentally unreachable code.
        "-Ywarn-value-discard"   // Prevent accidental discarding of results in unit functions.
      ),
      ScalariformKeys.preferences := ScalariformKeys.preferences.value
        .setPreference(AlignArguments, true)
        .setPreference(AlignParameters, true)
        .setPreference(AlignSingleLineCaseStatements, true)
        .setPreference(DoubleIndentClassDeclaration, true)
        .setPreference(DoubleIndentClassDeclaration, true)
        .setPreference(PreserveDanglingCloseParenthesis, true)
        .setPreference(RewriteArrowSymbols, true)
        .setPreference(SpaceBeforeColon, true),
      wartremoverErrors ++= Seq(
        Wart.Any2StringAdd,  // Prevent accidental stringification.
        Wart.FinalCaseClass, // Case classes should always be final.
        Wart.IsInstanceOf,   // Prevent type-casing.
        Wart.Null,           // Null is bad, bad, bad.
        Wart.Return          // Prevent use of `return` keyword.
      ),
      libraryDependencies ++= Seq(
        "com.typesafe" % "config" % "1.2.1",
        "org.scala-lang" % "scala-compiler" % scalaVersion.value,
        compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
		  "org.scalatest" %% "scalatest" % "2.2.1" % "test"
      )
    )
  )
}
