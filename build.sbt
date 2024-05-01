name := "safe-config"
organization := "com.kinja"
version := "2.0.0"

crossScalaVersions := Seq("2.13.13")
scalaVersion := crossScalaVersions.value.head

scalacOptions ++= Seq(
  "-unchecked",                        // Show details of unchecked warnings.
  "-deprecation",                      // Show details of deprecation warnings.
  "-encoding", "UTF-8",                // Set correct encoding for Scaladoc.
  "-feature",                          // Show details of feature warnings.
  "-explaintypes",                     // Explain type errors in more detail.
  "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
  "-Xlint",                            // Ensure best practices are being followed.
  "-Xlint:adapted-args",               // Warn if an argument list is modified to match the receiver.
  "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
  "-Xlint:doc-detached",               // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-unit",               // Warn when nullary methods return Unit.
  "-Xlint:option-implicit",            // Option.apply used implicit view.
  "-Xlint:package-object-classes",     // Class or object defined in package object.
  "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow",      // A local type parameter shadows a type already in scope.
  "-Ywarn-dead-code",                  // Fail when dead code is present. Prevents accidentally unreachable code.
  "-Ywarn-dead-code",                  // Fail when dead code is present. Prevents accidentally unreachable code.
  "-Ywarn-numeric-widen",              // Warn when numerics are widened.
  // "-Ywarn-unused:params",              // Warn if a value parameter is unused.
  // "-Ywarn-unused:patvars",             // Warn if a variable bound in a pattern is unused.
  "-Ywarn-value-discard",              // Prevent accidental discarding of results in unit functions.
  "-Ymacro-annotations",
  "-Xlint:constant",                  // Evaluation of a constant arithmetic expression results in an error.
  "-Ywarn-unused:locals",             // Warn if a local definition is unused.
  "-Ywarn-unused:implicits",          // Warn if an implicit parameter is unused.
  "-Ywarn-unused:privates",           // Warn if a private member is unused.
  "-Ywarn-extra-implicit"             // Warn when more than one implicit parameter section is defined.
)

scalafmtOnCompile := true

wartremoverErrors ++= Warts.allBut(Wart.Equals, Wart.Overloading, Wart.ListAppend, Wart.IterableOps)

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.4.2",
  "org.scala-lang" % "scala-compiler" % scalaVersion.value,
  "org.scalatest" %% "scalatest" % "3.2.17" % Test
)

lazy val root = Project("safe-config", file("."))

publishTo := sonatypePublishTo.value
