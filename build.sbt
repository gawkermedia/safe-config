import scalariform.formatter.preferences._

name := "safe-config"
organization := "com.kinja"
version := "1.1.2-SNAPSHOT"

scalaVersion := "2.12.8"
crossScalaVersions := Seq("2.12.8")

scalacOptions ++= Seq(
  "-deprecation",          // Show details of deprecation warnings.
  "-encoding", "UTF-8",    // Set correct encoding for Scaladoc.
  "-feature",              // Show details of feature warnings.
  "-unchecked",            // Show details of unchecked warnings.
  "-Xfatal-warnings",      // All warnings should result in a compiliation failure.
  "-Xfuture",              // Disables view bounds, adapted args, and unsound pattern matching in 2.11.
  "-Xlint",                // Ensure best practices are being followed.
  "-Yno-adapted-args",     // Prevent implicit tupling of arguments.
  "-Ywarn-dead-code",      // Fail when dead code is present. Prevents accidentally unreachable code.
  "-Ywarn-value-discard"   // Prevent accidental discarding of results in unit functions.
)

scalariformAutoformat := true
scalariformPreferences := scalariformPreferences.value
  .setPreference(AlignArguments, true)
  .setPreference(AlignParameters, true)
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(DoubleIndentConstructorArguments, true)
  .setPreference(DanglingCloseParenthesis, Preserve)
  .setPreference(SpaceBeforeColon, true)

wartremoverErrors ++= Warts.allBut(
  Wart.Any,
  Wart.Equals,
  Wart.NonUnitStatements,
  Wart.Nothing,
  Wart.OptionPartial,
  Wart.Overloading,
  Wart.PublicInference,
  Wart.Throw,
  Wart.ToString,
  Wart.TraversableOps,
  Wart.Var
)

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.2",
  "org.scala-lang" % "scala-compiler" % scalaVersion.value,
  compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)

lazy val root = Project("safe-config", file("."))

publishTo := sonatypePublishTo.value
