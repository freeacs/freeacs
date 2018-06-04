lazy val commonSettings = Seq(
  organization := "com.github.freeacs",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.6"
)

lazy val web = (project in file("web"))
  .settings(
    commonSettings
  )

lazy val webservice = (project in file("webservice"))
  .settings(
    commonSettings
  )

lazy val root = (project in file("."))
  .aggregate(web, webservice)
