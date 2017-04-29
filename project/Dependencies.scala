import sbt._

object Dependencies extends AutoPlugin {
  val autoImport = this

  val resolvers = Seq(
    Resolver.jcenterRepo
  )

  val paradise = "org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full

  val scalaGuice = "net.codingwell" %% "scala-guice" % "4.1.0"
  val logging = "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
  val logstash = "net.logstash.logback" % "logstash-logback-encoder" % "4.9"

  // Persistence
  val postgres = "org.postgresql" % "postgresql" % "42.0.0"
  val relate = "com.lucidchart" %% "relate" % "2.0.1"

  // Validation
  object Accord {
    private val version = "0.6.1"
    val core = "com.wix" %% "accord-core" % version excludeAll ExclusionRule(organization = "org.scalatest")
    val scalatest = "com.wix" %% "accord-scalatest" % version
  }

  // Test
  val scalatest = "org.scalatest" %% "scalatest" % "3.0.1"
  val scalatestPlay = "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0"
  val mockito = "org.mockito" % "mockito-core" % "2.7.22"
  val guiceTestkit = "com.google.inject.extensions" % "guice-testlib" % "4.1.0"
}
