import scalariform.formatter.preferences._

name := "safe-config"
organization := "com.kinja"
version := "1.1.2-SNAPSHOT"

scalaVersion := "2.13.0"
crossScalaVersions := Seq("2.13.0", "2.12.8")

scalacOptions ++= Seq(
  "-deprecation",          // Show details of deprecation warnings.
  "-encoding", "UTF-8",    // Set correct encoding for Scaladoc.
  "-feature",              // Show details of feature warnings.
  "-unchecked",            // Show details of unchecked warnings.
  "-Xlint",                // Ensure best practices are being followed.
  "-Ywarn-dead-code",      // Fail when dead code is present. Prevents accidentally unreachable code.
  "-Ywarn-value-discard"   // Prevent accidental discarding of results in unit functions.
)

scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
  case Some((2, scalaMajor)) if scalaMajor >= 13 =>
    Seq(
      "-Ymacro-annotations"
    )
  case _ =>
    Seq(
      "-Xfatal-warnings",  // All warnings should result in a compiliation failure.
      "-Yno-adapted-args", // No longer needed in Scala 2.13
      "-Xfuture"           // Deprecated in Scala 2.13
    )
})

scalariformAutoformat := true
scalariformPreferences := scalariformPreferences.value
  .setPreference(AlignArguments, true)
  .setPreference(AlignParameters, true)
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(DoubleIndentConstructorArguments, true)
  .setPreference(DanglingCloseParenthesis, Preserve)
  .setPreference(SpaceBeforeColon, true)

wartremoverErrors ++= Warts.allBut(Wart.Equals, Wart.Overloading)

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.4",
  "org.scala-lang" % "scala-compiler" % scalaVersion.value,
  "org.scalatest" %% "scalatest" % "3.0.8" % Test
)

libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
  case Some((2, scalaMajor)) if scalaMajor >= 13 =>
    Seq()
  case _ =>
    Seq(compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full))
})

lazy val root = Project("safe-config", file("."))

publishTo := sonatypePublishTo.value
