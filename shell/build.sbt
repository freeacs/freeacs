name := "FreeACS Stun"

version := "2.0.1-SNAPSHOT"

maintainer := "Jarl Andre Hubenthal <jarl.andre@gmail.com>"

packageSummary := "FreeACS Stun"

packageDescription := "FreeACS Stun"

autoScalaLibrary := false

resolvers += Resolver.mavenLocal

libraryDependencies ++= List(
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.apache.httpcomponents" % "httpclient" % "4.5.5",
  "commons-io" % "commons-io" % "1.3.2",
  "com.zaxxer" % "HikariCP" % "3.1.0",
  "javax.servlet" % "servlet-api" % "2.5" % "provided",
  "junit" % "junit" % "4.12" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test",
  "org.flywaydb" % "flyway-core" % "5.0.7" % "test",
  "com.h2database" % "h2" % "1.4.197" % "test",
  "mysql" % "mysql-connector-java" % "8.0.11",
  "jline" % "jline" % "0.9.5",
  "dom4j" % "dom4j" % "1.6.1",
  "org.vafer" % "jdeb" % "1.3" artifacts Artifact("jdeb", "jar", "jar")
)

scriptClasspath := Seq("*")
enablePlugins(JavaAppPackaging)
enablePlugins(JDebPackaging)
testOptions += Tests.Argument(TestFrameworks.JUnit)
fork in Test := true