import sbt.Keys.fork

lazy val commonSettings = Seq(
  maintainer := "Jarl Andre Hubenthal <jarl.andre@gmail.com>",
  organization := "com.github.freeacs",
  version := "2.0.1-SNAPSHOT",
  scalaVersion := "2.12.6",
  resolvers += Resolver.mavenLocal,
  autoScalaLibrary := false,
  testOptions += Tests.Argument(TestFrameworks.JUnit),
  fork in Test := true
)

lazy val common = (project in file("common"))
  .settings(
    commonSettings
  )
lazy val dbi = (project in file("dbi"))
  .settings(
    commonSettings
  )
  .dependsOn(common)
lazy val web = (project in file("web"))
  .settings(
    commonSettings
  )
  .dependsOn(dbi)
lazy val webservice = (project in file("webservice"))
  .settings(
    commonSettings,
    name := "FreeACS Webservice",
    normalizedName := "freeacs-webservice",
    packageSummary := "FreeACS Webservice",
    packageDescription := "FreeACS Webservice",
    xjcCommandLine += "-verbose",
    scriptClasspath := Seq("*"),
    libraryDependencies ++= Dependencies.springBoot
      ++ Seq(Dependencies.springBootWebservices)
      ++ Dependencies.database
      ++ Dependencies.testing
      ++ Seq(Dependencies.jdeb)
      ++ Seq("wsdl4j" % "wsdl4j" % "1.6.3")
  )
  .enablePlugins(JavaAppPackaging, JDebPackaging)
  .dependsOn(dbi)
lazy val tr069 = (project in file("tr069"))
  .settings(
    commonSettings
  )
  .dependsOn(dbi)
lazy val syslog = (project in file("syslog"))
  .settings(
    commonSettings
  )
  .dependsOn(dbi)
lazy val stun = (project in file("stun"))
  .settings(
    commonSettings
  )
  .dependsOn(dbi)
lazy val shell = (project in file("shell"))
  .settings(
    commonSettings
  )
  .dependsOn(dbi)
lazy val core = (project in file("core"))
  .settings(
    commonSettings
  )
  .dependsOn(shell)

lazy val root = (project in file("."))
  .aggregate(common, dbi, web, webservice, tr069, syslog, stun, shell, core)
