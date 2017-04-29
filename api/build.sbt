enablePlugins(PlayScala, DockerPlugin, VersionGenerator)

addCompilerPlugin(paradise)

libraryDependencies ++= Seq(
  logging,
  logstash,
  postgres,
  jdbc,
  filters,
  evolutions,
  relate,
  Accord.core,
  scalatest % Test,
  scalatestPlay % Test,
  mockito % Test,
  guiceTestkit % Test
)

libraryDependencies += filters

sourceGenerators in Compile += VersionGenerator.generate.taskValue