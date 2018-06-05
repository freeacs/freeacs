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
    commonSettings,
    name := "FreeACS Web",
    normalizedName := "freeacs-web",
    packageSummary := "FreeACS Web",
    packageDescription := "FreeACS Web",
    scriptClasspath := Seq("*"),
    libraryDependencies ++= Dependencies.springBoot
      ++ Seq(Dependencies.springBootWebservices)
      ++ Dependencies.database
      ++ Dependencies.testing
      ++ Seq(Dependencies.jdeb)
      ++ Seq(
        "org.springframework.boot" % "spring-boot-starter-security" % "2.0.2.RELEASE",
        "javax.mail" % "javax.mail-api" % "1.6.1",
        "commons-fileupload" % "commons-fileupload" % "1.3",
        "commons-cli" % "commons-cli" % "1.1",
        "commons-codec" % "commons-codec" % "1.4",
        "commons-lang" % "commons-lang" % "2.4",
        "commons-logging" % "commons-logging" % "1.0.4",
        "commons-net" % "commons-net" % "2.2",
        "commons-httpclient" % "commons-httpclient" % "3.1",
        "dom4j" % "dom4j" % "1.6.1",
        "net.sf.flexjson" % "flexjson" % "2.1",
        "org.freemarker" % "freemarker" % "2.3.14",
        "org.springframework" % "spring-context-support" % "5.0.6.RELEASE",
        "org.codehaus.jackson" % "jackson-core-asl" % "1.6.4",
        "org.codehaus.jackson" % "jackson-mapper-asl" % "1.6.4",
        "jaxen" % "jaxen" % "1.1.6",
        "javax.xml" % "jaxrpc" % "1.1",
        "org.jfree" % "jcommon" % "1.0.17",
        "org.jfree" % "jfreechart" % "1.0.17",
        "com.metaparadigm" % "json-rpc" % "1.0",
        "org.json" % "json" % "20140107"
      )
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
