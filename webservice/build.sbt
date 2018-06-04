xjcCommandLine += "-verbose"

autoScalaLibrary := false

resolvers += Resolver.mavenLocal

libraryDependencies ++= List(
  "mysql" % "mysql-connector-java" % "8.0.11",
  "org.springframework.boot" % "spring-boot-starter-web-services" % "2.0.1.RELEASE",
  "org.springframework.boot" % "spring-boot-starter-actuator" % "2.0.2.RELEASE",
  "org.springframework.boot" % "spring-boot-starter-jdbc" % "2.0.2.RELEASE",
  "org.springframework.boot" % "spring-boot-starter-test" % "2.0.2.RELEASE" % "test",
  "com.zaxxer" % "HikariCP" % "3.1.0",
  "javax.servlet" % "servlet-api" % "2.5",
  "wsdl4j" % "wsdl4j" % "1.6.3",
  "junit" % "junit" % "4.12" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test",
  "org.flywaydb" % "flyway-core" % "5.0.7" % "test",
  "com.h2database" % "h2" % "1.4.197" % "test"
)

scriptClasspath := Seq("*")
enablePlugins(JavaAppPackaging)
testOptions += Tests.Argument(TestFrameworks.JUnit)
fork in Test := true