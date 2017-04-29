name := "payu"

version := "1.0"

scalaVersion in ThisBuild := "2.11.8"

val common = Project("payu-common", file("payu-common"))

val api = Project("payu-api", file("payu-api")).dependsOn(common)

val root = Project("payu", file(".")).aggregate(common, api)
