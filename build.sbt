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
lazy val web = (project in file("web"))
  .settings(
    commonSettings
  )
lazy val webservice = (project in file("webservice"))
  .settings(
    commonSettings
  )
lazy val tr069 = (project in file("tr069"))
  .settings(
    commonSettings
  )
lazy val syslog = (project in file("syslog"))
  .settings(
    commonSettings
  )
lazy val stun = (project in file("stun"))
  .settings(
    commonSettings
  )
lazy val shell = (project in file("shell"))
  .settings(
    commonSettings
  )
lazy val core = (project in file("core"))
  .settings(
    commonSettings
  )

lazy val root = (project in file("."))
  .aggregate(web, webservice, tr069, syslog, stun, dbi, common, shell, core)
