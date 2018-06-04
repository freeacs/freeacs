lazy val commonSettings = Seq(
  organization := "com.github.freeacs",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.6"
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
    commonSettings
  )
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
