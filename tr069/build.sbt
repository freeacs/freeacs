name := "FreeACS Tr069"

normalizedName := "freeacs-tr069"

packageSummary := "FreeACS Tr069"

packageDescription := "FreeACS Tr069"

libraryDependencies ++= List(
  "org.apache.commons" % "commons-lang3" % "3.7",
  "mysql" % "mysql-connector-java" % "8.0.11",
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
  "com.h2database" % "h2" % "1.4.197" % "test",
  "org.vafer" % "jdeb" % "1.3" artifacts Artifact("jdeb", "jar", "jar")
)

scriptClasspath := Seq("*")
enablePlugins(JavaAppPackaging)
enablePlugins(JDebPackaging)