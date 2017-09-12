organization in ThisBuild := "com.kainos"
version in ThisBuild := "1.0-SNAPSHOT"

lazy val scalaProjectVersion = "2.11.11"

scalaVersion in ThisBuild := scalaProjectVersion
scalafmtConfig in ThisBuild := file("scalafmt.conf")
scalafmtVersion in ThisBuild := "1.1.0"
scalafmtOnCompile := true

def dockerSettings(exposePort: Option[Int] = None,
                   debugPort: Option[Int] = None) = Seq(
  assemblyMergeStrategy in assembly := {
    case r if r.startsWith("reference.conf") => MergeStrategy.concat
    case PathList("META-INF", m) if m.equalsIgnoreCase("MANIFEST.MF") =>
      MergeStrategy.discard
    case x => MergeStrategy.first
  },
  dockerfile in docker := {
    // The assembly task generates a fat JAR file
    val baseDir = baseDirectory.value
    val artifact: File = assembly.value
    val artifactTargetPath = s"/app/${artifact.name}"
    val dockerResourcesDir = baseDir / "docker-scripts"
    val dockerResourcesTargetPath = "/app/"

    new Dockerfile {
      from("java")
      add(artifact, artifactTargetPath)
      copy(dockerResourcesDir, dockerResourcesTargetPath)
      entryPoint(s"/app/entrypoint.sh")
      debugPort match {
        case Some(port) => cmd(s"${name.value}", s"${version.value}", s"$port")
        case None       => cmd(s"${name.value}", s"${version.value}")
      }
      exposePort match {
        case Some(port) => expose(port)
        case None       =>
      }
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

lazy val `tokenserver` = (project in file("tokenserver"))
  .enablePlugins(sbtdocker.DockerPlugin)
  .settings(
    scalaVersion := scalaProjectVersion,
    name := "tokenserver",
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.0.9",
      "org.scalatest" %% "scalatest" % "3.0.1" % "test"
    ),
    wartremoverWarnings ++= Warts.all,
    scalafmtOnCompile := true,
    dockerSettings(exposePort = Some(8888)),
    mainClass in assembly := Some("com.kainos.token.TokenServer")
  )

lazy val `producer` = (project in file("producer"))
  .enablePlugins(sbtdocker.DockerPlugin)
  .settings(
    scalaVersion := scalaProjectVersion,
    name := "producer",
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.1" % "test",
      "org.apache.kafka" %% "kafka" % "0.10.0.0"
    ),
    wartremoverWarnings ++= Warts.all,
    scalafmtOnCompile := true,
    dockerSettings(),
    mainClass in assembly := Some("com.kainos.client.Producer")
  )
  .dependsOn(`tokenserver`)

lazy val `consumer` = (project in file("consumer"))
  .enablePlugins(sbtdocker.DockerPlugin)
  .settings(
    scalaVersion := scalaProjectVersion,
    name := "consumer",
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.1" % "test",
      "org.apache.kafka" %% "kafka" % "0.10.0.0"
    ),
    wartremoverWarnings ++= Warts.all,
    scalafmtOnCompile := true,
    dockerSettings(),
    mainClass in assembly := Some("com.kainos.client.Consumer")
  )
