lazy val projectVersion = "1.0-SNAPSHOT"

organization in ThisBuild := "com.kainos"
version in ThisBuild := projectVersion

lazy val scalaProjectVersion = "2.11.11"

scalaVersion in ThisBuild := scalaProjectVersion
scalafmtConfig in ThisBuild := file("scalafmt.conf")
scalafmtVersion in ThisBuild := "1.1.0"
scalafmtOnCompile := true

lazy val tokenServerDebugPort = 5001
lazy val producerDebugPort = 5002
lazy val consumerDebugPort = 5003

def dockerSettings(exposePort: List[Int] = Nil, debugPort: Option[Int] = None) =
  Seq(
    assemblyMergeStrategy in assembly := {
      case r if r.startsWith("reference.conf") => MergeStrategy.concat
      case PathList("META-INF", m) if m.equalsIgnoreCase("MANIFEST.MF") =>
        MergeStrategy.discard
      case _ => MergeStrategy.first
    },
    dockerfile in docker := {
      // The assembly task generates a fat JAR file
      val baseDir = baseDirectory.value
      val artifact: File = assembly.value
      val artifactTargetPath = s"/app/${artifact.name}"
      val dockerResourcesDir = baseDir / "src/main/resources/docker-scripts"
      val dockerResourcesTargetPath = "/app/"

      new Dockerfile {
        from("java")
        add(artifact, artifactTargetPath)
        copy(dockerResourcesDir, dockerResourcesTargetPath)
        entryPoint(s"/app/entrypoint.sh")
        debugPort match {
          case Some(port) =>
            cmd(s"${name.value}", s"${version.value}", s"$port")
          case None => cmd(s"${name.value}", s"${version.value}")
        }
        exposePort map (expose(_))
      }
    },
    imageNames in docker := Seq(
      // Sets the latest tag
      ImageName(s"${name.value}:latest"),
      // Sets a name with a tag that contains the project version
      ImageName(namespace = Some(organization.value),
                repository = name.value,
                tag = Some("v" + version.value))
    )
  )

lazy val `kafka-docker-sbt` = (project in file("."))
  .aggregate(`producer`, `tokenserver`, `consumer`)

lazy val `schema` = (project in file("schema"))
  .enablePlugins(SbtAvro)
  .settings(
    scalaVersion := scalaProjectVersion,
    name := "tokenserver",
    version := projectVersion,
    stringType := "CharSequence",
    libraryDependencies ++= Seq(
      "com.sksamuel.avro4s" %% "avro4s-core" % "1.8.0",
      "com.squareup.okhttp3" % "okhttp" % "3.9.0"
    )
  )

lazy val `tokenserver` = (project in file("tokenserver"))
  .enablePlugins(sbtdocker.DockerPlugin)
  .settings(
    scalaVersion := scalaProjectVersion,
    name := "tokenserver",
    version := projectVersion,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.0.9",
      "org.scalatest" %% "scalatest" % "3.0.1" % "test"
    ),
    wartremoverWarnings ++= Warts.all,
    scalafmtOnCompile := true,
    dockerSettings(exposePort = List(8888),
                   debugPort = Some(tokenServerDebugPort)),
    mainClass in assembly := Some("com.kainos.token.TokenServer")
  )

lazy val `producer` = (project in file("producer"))
  .enablePlugins(sbtdocker.DockerPlugin)
  .settings(
    scalaVersion := scalaProjectVersion,
    name := "producer",
    version := projectVersion,
    resolvers += "Confluent" at "http://packages.confluent.io/maven/",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.1" % "test",
      "org.apache.avro" % "avro" % "1.8.1",
      "io.confluent" % "kafka-avro-serializer" % "3.2.1",
      "org.apache.kafka" % "kafka-clients" % "0.10.2.0"
    ),
    wartremoverWarnings ++= Warts.all,
    scalafmtOnCompile := true,
    dockerSettings(debugPort = Some(producerDebugPort)),
    mainClass in assembly := Some("com.kainos.client.Producer")
  )
  .dependsOn(`tokenserver`, `schema`)

lazy val `consumer` = (project in file("consumer"))
  .enablePlugins(sbtdocker.DockerPlugin)
  .settings(
    scalaVersion := scalaProjectVersion,
    name := "consumer",
    version := projectVersion,
    libraryDependencies ++= Seq(
      "org.apache.avro" % "avro" % "1.8.1",
      "io.confluent" % "kafka-avro-serializer" % "3.2.1",
      "org.scalatest" %% "scalatest" % "3.0.1" % "test",
      "org.apache.kafka" % "kafka-clients" % "0.10.2.0"
    ),
    wartremoverWarnings ++= Warts.all,
    scalafmtOnCompile := true,
    dockerSettings(debugPort = Some(consumerDebugPort)),
    mainClass in assembly := Some("com.kainos.client.Consumer")
  )
  .dependsOn(`schema`)
