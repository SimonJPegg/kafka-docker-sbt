lazy val projectVersion = "1.0-SNAPSHOT"

organization in ThisBuild := "com.kainos"
version in ThisBuild := projectVersion

lazy val scalaProjectVersion = "2.11.11"

scalaVersion in ThisBuild := scalaProjectVersion
scalafmtConfig in ThisBuild := file("scalafmt.conf")
scalafmtVersion in ThisBuild := "1.1.0"
scalafmtOnCompile := true

lazy val producerDebugPort = 5002
lazy val converterDebugPort = 5001
lazy val consumerDebugPort = 5003

lazy val dockerAppPath = "/app/"
lazy val dockerResourcesDir = "src/main/resources/docker-scripts"
lazy val dockerResourcesTargetPath = "/app/"

enablePlugins(DockerComposePlugin)

docker <<= (
  docker in `string_producer`,
  docker in `string_avro_converter`,
  docker in `avro_consumer`
) map { (image, _, _) =>
  image
}

dockerImageCreationTask := docker.value

lazy val `kafka-docker-sbt` = (project in file("."))
  .aggregate(`string_producer`, `string_avro_converter`, `avro_consumer`)

lazy val `schema` = (project in file("schema"))
  .enablePlugins(SbtAvro)
  .settings(
    scalaVersion := scalaProjectVersion,
    name := "schema",
    version := projectVersion,
    stringType := "CharSequence",
    resolvers += "Confluent" at "http://packages.confluent.io/maven/",
    libraryDependencies ++= Seq(
      "com.sksamuel.avro4s" %% "avro4s-core" % "1.8.0",
      "org.apache.kafka" % "kafka-clients" % "0.10.2.0-cp1"
    )
  )

lazy val `string_producer` = (project in file("stringProducer"))
  .enablePlugins(sbtdocker.DockerPlugin, DockerComposePlugin, JavaAppPackaging)
  .settings(
    scalaVersion := scalaProjectVersion,
    name := "string_producer",
    version := projectVersion,
    resolvers += "Confluent" at "http://packages.confluent.io/maven/",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.1" % "test",
      "org.apache.avro" % "avro" % "1.8.1",
      "io.confluent" % "kafka-avro-serializer" % "3.2.1",
      "org.apache.kafka" % "kafka-clients" % "0.10.2.0-cp1"
    ),
    wartremoverWarnings ++= Warts.all,
    scalafmtOnCompile := true,
    dockerImageCreationTask := (publishLocal in Docker).value,
    mainClass in Compile := Some("com.kainos.producer.StringProducer"),
    dockerfile in docker := {
      new Dockerfile {
        val mainClassString = (mainClass in Compile).value.get
        val classpath = (fullClasspath in Compile).value
        from("java")
        add(classpath.files, dockerAppPath)
        copy(baseDirectory.value / dockerResourcesDir,
             dockerResourcesTargetPath)
        copy(baseDirectory.value / "src/main/resources/data/shakespeare",
             "/data/shakespeare")
        entryPoint(s"/app/entrypoint.sh")
        cmd(s"${name.value}", s"${version.value}", s"$producerDebugPort")
      }
    },
    imageNames in docker := Seq(
      ImageName(s"${name.value}:latest"),
      ImageName(namespace = Some(organization.value),
                repository = name.value,
                tag = Some("v" + version.value))
    )
  )
  .dependsOn(`schema`)

lazy val `string_avro_converter` = (project in file("stringAvroConverter"))
  .enablePlugins(sbtdocker.DockerPlugin, DockerComposePlugin, JavaAppPackaging)
  .settings(
    scalaVersion := scalaProjectVersion,
    name := "string_avro_converter",
    version := projectVersion,
    resolvers += "Confluent" at "http://packages.confluent.io/maven/",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.1" % "test",
      "org.apache.avro" % "avro" % "1.8.1",
      "io.confluent" % "kafka-avro-serializer" % "3.2.1",
      "org.apache.kafka" % "kafka-clients" % "0.10.2.0-cp1"
    ),
    wartremoverWarnings ++= Warts.all,
    scalafmtOnCompile := true,
    dockerImageCreationTask := (publishLocal in Docker).value,
    mainClass in Compile := Some(
      "com.kainos.converter.StringConsumerAvroProducer"),
    dockerfile in docker := {
      new Dockerfile {
        val mainClassString = (mainClass in Compile).value.get
        val classpath = (fullClasspath in Compile).value
        from("java")
        add(classpath.files, dockerAppPath)
        copy(baseDirectory.value / dockerResourcesDir,
             dockerResourcesTargetPath)
        entryPoint(s"/app/entrypoint.sh")
        cmd(s"${name.value}", s"${version.value}", s"$converterDebugPort")
      }
    },
    imageNames in docker := Seq(
      ImageName(s"${name.value}:latest"),
      ImageName(namespace = Some(organization.value),
                repository = name.value,
                tag = Some("v" + version.value))
    )
  )
  .dependsOn(`schema`)

lazy val `avro_consumer` = (project in file("avroConsumer"))
  .enablePlugins(sbtdocker.DockerPlugin, DockerComposePlugin, JavaAppPackaging)
  .settings(
    scalaVersion := scalaProjectVersion,
    name := "avro_consumer",
    version := projectVersion,
    resolvers += "Confluent" at "http://packages.confluent.io/maven/",
    libraryDependencies ++= Seq(
      "org.apache.avro" % "avro" % "1.8.1",
      "io.confluent" % "kafka-avro-serializer" % "3.2.1",
      "org.scalatest" %% "scalatest" % "3.0.1" % "test",
      "org.apache.kafka" % "kafka-clients" % "0.10.2.0-cp1"
    ),
    wartremoverWarnings ++= Warts.all,
    scalafmtOnCompile := true,
    dockerImageCreationTask := (publishLocal in Docker).value,
    mainClass in Compile := Some("com.kainos.consumer.AvroConsumer"),
    dockerfile in docker := {
      new Dockerfile {
        val mainClassString = (mainClass in Compile).value.get
        val classpath = (fullClasspath in Compile).value
        from("java")
        add(classpath.files, dockerAppPath)
        copy(baseDirectory.value / dockerResourcesDir,
             dockerResourcesTargetPath)
        entryPoint(s"/app/entrypoint.sh")
        cmd(s"${name.value}", s"${version.value}", s"$consumerDebugPort")
      }
    },
    imageNames in docker := Seq(
      ImageName(s"${name.value}:latest"),
      ImageName(namespace = Some(organization.value),
                repository = name.value,
                tag = Some("v" + version.value))
    )
  )
  .dependsOn(`schema`)
