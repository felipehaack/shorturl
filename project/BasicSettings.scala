import sbt.Keys._
import sbt._

object BasicSettings extends AutoPlugin {
  override lazy val trigger = allRequirements

  override lazy val projectSettings = Seq(
    organization := "com.payu.shorturl",
    resolvers ++= Dependencies.resolvers,
    scalaVersion := "2.11.9",
    scalacOptions ++= Seq(
      "-deprecation", // Emit warning and location for usages of deprecated APIs.
      "-feature", // Emit warning and location for usages of features that should be imported explicitly.
      "-unchecked", // Enable additional warnings where generated code depends on assumptions.
      "-Xfatal-warnings" // Fail the compilation if there are any warnings.
    ),
    javacOptions ++= Seq(
      "-source", "1.8",
      "-target", "1.8",
      "-Xlint"
    )
  )
}

object VersionGenerator extends AutoPlugin {
  //  override def trigger = noTrigger

  override lazy val projectSettings = Seq(
    sourceGenerators in Compile += generate.taskValue
  )

  lazy val generate = Def.task {
    val file = (sourceManaged in Compile).value / "Version.scala"
    val scalaSource =
      """|package com.payu.shorturl
         |
         |object Version {
         |  val current = "%s"
         |  val scalaVersion = "%s"
         |  val sbtVersion = "%s"
         |}
      """.stripMargin.format(version.value, scalaVersion.value, sbtVersion.value)

    if (!file.exists() || IO.read(file) != scalaSource) {
      IO.write(file, scalaSource)
    }
    Seq(file)
  }
}
