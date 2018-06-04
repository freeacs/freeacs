autoScalaLibrary := false

resolvers += Resolver.mavenLocal

libraryDependencies ++= List(
  "org.apache.commons" % "commons-lang3" % "3.7",
  "mysql" % "mysql-connector-java" % "8.0.11",
  "com.github.freeacs" %  "dbi" % "2.0.1-SNAPSHOT",
  "org.springframework.boot" %  "spring-boot-starter-web" % "2.0.2.RELEASE" exclude("org.springframework.boot", "spring-boot-starter-tomcat"),
  "org.springframework.boot" %  "spring-boot-starter-jetty" % "2.0.2.RELEASE",
  "org.springframework.boot" %  "spring-boot-starter-jdbc" % "2.0.2.RELEASE",
  "org.springframework.boot" %  "spring-boot-starter-actuator" % "2.0.2.RELEASE",
  "com.zaxxer" % "HikariCP" % "3.1.0",
  "org.springframework.boot" %  "spring-boot-starter-test" % "2.0.2.RELEASE" % "test",
  "javax.servlet" % "servlet-api" % "2.5" % "provided",
  "junit" % "junit" % "4.12" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test",
  "org.flywaydb" % "flyway-core" % "5.0.7" % "test",
  "com.h2database" % "h2" % "1.4.197" % "test"
)

scriptClasspath := Seq("*")
enablePlugins(JavaAppPackaging)
testOptions += Tests.Argument(TestFrameworks.JUnit)
fork in Test := true