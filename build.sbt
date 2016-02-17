import com.typesafe.sbt.packager.docker._

name := """play-pass"""

version := "1.0.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, JavaAppPackaging, DockerPlugin)

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

scalaVersion := "2.11.7"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
resolvers += Resolver.jcenterRepo
resolvers += Resolver.mavenLocal

libraryDependencies ++= Seq(
  cache,
  javaWs,
  filters,
  // WebJars pull in client-side web libraries,
  //"org.webjars" % "bootstrap" % "4.0.0-alpha.2",
  "org.webjars" % "bootstrap" % "3.3.6",
  "org.webjars" % "webjars-play_2.11" % "2.4.0-2",
  //"org.webjars" % "jquery" % "3.0.0-alpha1",
  "org.webjars" % "jquery" % "2.2.0",
  "org.webjars" % "font-awesome" % "4.5.0",
  //"org.webjars" % "requirejs" % "2.1.20",
  //"org.webjars" % "backbonejs" % "1.2.1",
  //"org.webjars" % "angularjs" % "2.0.0-alpha.22",
  //"org.webjars" % "underscorejs" % "1.8.1",
  //"org.webjars" % "react" % "0.13.3",

  // Security
  "be.objectify" %% "deadbolt-java" % "2.4.4",
  "de.qaware" % "heimdall" % "1.3",

  // Commons
  "org.apache.commons" % "commons-lang3" % "3.4",
  "com.google.guava" % "guava" % "19.0",
  "org.apache.commons" % "commons-collections4" % "4.1",
  "commons-io" % "commons-io" % "2.4",


  // ARANGO
  "com.arangodb" % "arangodb-java-driver" % "2.7.2",

  // Json
  "com.jayway.jsonpath" % "json-path" % "2.1.0",
  "io.mola.galimatias" % "galimatias" % "0.2.1",

  // CSV
  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-csv" % "2.7.1",

  // Testing
  "org.assertj" % "assertj-core" % "3.1.0" % "test",
  "net.sourceforge.htmlcleaner" % "htmlcleaner" % "2.16" % "test",
  "com.jayway.restassured" % "xml-path" % "2.8.0" % "test",
  "net.sourceforge.htmlunit" % "htmlunit" % "2.19" % "test",
  "junit" % "junit" % "4.12" % "test"
  // Select Play modules
  //anorm,     // Scala RDBMS Library
  //javaJpa,   // Java JPA plugin
  //"org.hibernate" % "hibernate-entitymanager" % "4.3.10.Final",
  //"mysql" % "mysql-connector-java" % "5.1.36",
  //filters,   // A set of built-in filters
)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")

initialize := {
  val _ = initialize.value
  if (sys.props("java.specification.version") != "1.8")
    sys.error("Java 8 is required for this project.")
}


// --------------------
// ------ DOCKER ------
// --------------------
// build with activator docker:publishLocal

dockerBaseImage := "frolvlad/alpine-oraclejdk8:latest"
dockerCommands := dockerCommands.value.flatMap {
  case cmd@Cmd("FROM", _) => List(cmd, Cmd("RUN", "apk update && apk add bash"))
  case other => List(other)
}

// setting a maintainer which is used for findAll packaging types</pre>
maintainer := "Leonard Daume"

// exposing the play ports
dockerExposedPorts in Docker := Seq(9000, 9443)

// publish to repo
//dockerRepository := Some("quay.io/")
//dockerUpdateLatest := true

// run this with: docker run -p 9000:9000 <name>:<version>
