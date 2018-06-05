name := "FreeACS Core"

normalizedName := "freeacs-core"

packageSummary := "FreeACS Core"

packageDescription := "FreeACS Core"

libraryDependencies ++= List(
  "org.apache.httpcomponents" % "httpclient" % "4.5.5",
  "commons-io" % "commons-io" % "1.3.2",
  "org.springframework.boot" %  "spring-boot-starter-web" % "2.0.2.RELEASE",
  "org.springframework.boot" %  "spring-boot-starter-jdbc" % "2.0.2.RELEASE",
  "org.springframework.boot" %  "spring-boot-starter-actuator" % "2.0.2.RELEASE",
  "com.zaxxer" % "HikariCP" % "3.1.0",
  "org.springframework.boot" %  "spring-boot-starter-test" % "2.0.2.RELEASE" % "test",
  "javax.servlet" % "servlet-api" % "2.5" % "provided",
  "junit" % "junit" % "4.12" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test",
  "org.flywaydb" % "flyway-core" % "5.0.7" % "test",
  "com.h2database" % "h2" % "1.4.197" % "test",
  "mysql" % "mysql-connector-java" % "8.0.11",
  "org.vafer" % "jdeb" % "1.3" artifacts Artifact("jdeb", "jar", "jar")
)

scriptClasspath := Seq("*")
enablePlugins(JavaAppPackaging)
enablePlugins(JDebPackaging)