import sbt._

object Dependencies {
  val springBootActuator = "org.springframework.boot" % "spring-boot-starter-actuator" % "2.0.2.RELEASE"
  val springBootJdbc = "org.springframework.boot" % "spring-boot-starter-jdbc" % "2.0.2.RELEASE"
  val springBootTest = "org.springframework.boot" % "spring-boot-starter-test" % "2.0.2.RELEASE" % "test"
  val springBootWeb = "org.springframework.boot" % "spring-boot-starter-web" % "2.0.2.RELEASE" exclude("org.springframework.boot", "spring-boot-starter-tomcat")
  val springBootJetty = "org.springframework.boot" % "spring-boot-starter-jetty" % "2.0.2.RELEASE"

  val springBoot = Seq(springBootActuator, springBootJdbc, springBootTest, springBootWeb, springBootJetty)

  val springBootWebservices = Seq("org.springframework.boot" % "spring-boot-starter-web-services" % "2.0.1.RELEASE")

  val mysql = "mysql" % "mysql-connector-java" % "8.0.11"
  val hikari = "com.zaxxer" % "HikariCP" % "3.1.0"
  val flyway = "org.flywaydb" % "flyway-core" % "5.0.7" % "test"
  val h2 = "com.h2database" % "h2" % "1.4.197" % "test"

  val database = Seq(mysql, hikari, flyway, h2)

  val junit = "junit" % "junit" % "4.12" % "test"
  val junitInterface = "com.novocode" % "junit-interface" % "0.11" % "test"
  val mockito = "org.mockito" % "mockito-core" % "2.23.0" % Test

  val testing = Seq(junit, junitInterface, mockito)

  val mariadb = Seq(
    "ch.vorburger.mariaDB4j" % "mariaDB4j-core" % "2.3.0" % "test",
    "ch.vorburger.mariaDB4j" % "mariaDB4j-db-linux64" % "10.2.11" % "test",
    "org.mariadb.jdbc" % "mariadb-java-client" % "2.3.0" % "test"
  )

  val jdeb = Seq("org.vafer" % "jdeb" % "1.3" % "provided" artifacts Artifact("jdeb", "jar", "jar"))

}
