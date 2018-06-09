import sbt.Keys.fork

lazy val dockerSettings = Seq(
  maintainer in Docker := "Jarl Andre Hubenthal <jarl.andre@gmail.com>",
  dockerRepository := Some("freeacs"),
  dockerUpdateLatest := true,
  dockerExposedPorts := Seq(8080, 8080),
  dockerExposedVolumes := Seq("/opt/docker/logs", "/opt/docker/conf")
)

lazy val commonSettings = Seq(
  maintainer := "Jarl Andre Hubenthal <jarl.andre@gmail.com>",
  organization := "com.github.freeacs",
  version := "2.0.1-SNAPSHOT",
  scalaVersion := "2.12.6",
  crossPaths := false,
  resolvers += Resolver.mavenLocal,
  autoScalaLibrary := false,
  testOptions += Tests.Argument(TestFrameworks.JUnit),
  fork in Test := true,
  evictionWarningOptions in update := EvictionWarningOptions.default.withWarnTransitiveEvictions(false),
  dependencyOverrides ++= Seq(
    "com.zaxxer" % "HikariCP" % "3.1.0",
    "commons-io" % "commons-io" % "2.4",
    "org.springframework.boot" % "spring-boot-starter-web" % "2.0.2.RELEASE"
  )
)

lazy val common = (project in file("common"))
  .settings(
    commonSettings,
    name := "FreeACS Common",
    normalizedName := "freeacs-common",
    publish := {},
    libraryDependencies ++= Dependencies.database
      ++ Dependencies.testing
      ++ List(
      "org.apache.httpcomponents" % "httpclient" % "4.5.5",
      "ch.qos.logback" % "logback-classic" % "1.2.3"
    )
  )

lazy val dbi = (project in file("dbi"))
  .settings(
    commonSettings,
    name := "FreeACS Dbi",
    normalizedName := "freeacs-dbi",
    publish := {},
    libraryDependencies ++= Dependencies.database
      ++ Dependencies.testing
      ++ List(
      "org.jfree" % "jcommon" % "1.0.17",
      "org.jfree" % "jfreechart" % "1.0.17"
    )
  )
  .dependsOn(common)

lazy val web = (project in file("web"))
  .settings(
    commonSettings,
    dockerSettings,
    name := "Web",
    packageSummary := "FreeACS Web",
    packageDescription := "FreeACS Web",
    scriptClasspath := Seq("*"),
    defaultLinuxInstallLocation := "/opt/freeacs",
    libraryDependencies ++= Dependencies.springBoot
      ++ Dependencies.database
      ++ Dependencies.testing
      ++ Dependencies.jdeb
      ++ Seq(
      "org.springframework.boot" % "spring-boot-starter-security" % "2.0.2.RELEASE",
      "org.springframework" % "spring-context-support" % "5.0.6.RELEASE",
      "commons-fileupload" % "commons-fileupload" % "1.3",
      "commons-lang" % "commons-lang" % "2.4",
      "commons-httpclient" % "commons-httpclient" % "3.1",
      "org.freemarker" % "freemarker" % "2.3.14",
      "org.jfree" % "jcommon" % "1.0.17",
      "org.jfree" % "jfreechart" % "1.0.17"
    )
  )
  .enablePlugins(JavaServerAppPackaging, SystemdPlugin, JDebPackaging)
  .dependsOn(dbi)

lazy val webservice = (project in file("webservice"))
  .settings(
    commonSettings,
    dockerSettings,
    name := "Webservice",
    packageSummary := "FreeACS Webservice",
    packageDescription := "FreeACS Webservice",
    xjcCommandLine += "-verbose",
    scriptClasspath := Seq("*"),
    defaultLinuxInstallLocation := "/opt/freeacs",
    libraryDependencies ++= Dependencies.springBoot
      ++ Dependencies.database
      ++ Dependencies.testing
      ++ Dependencies.jdeb
      ++ Dependencies.springBootWebservices
      ++ Seq("wsdl4j" % "wsdl4j" % "1.6.3")
  )
  .enablePlugins(JavaServerAppPackaging, SystemdPlugin, JDebPackaging)
  .dependsOn(dbi)

lazy val tr069 = (project in file("tr069"))
  .settings(
    commonSettings,
    dockerSettings,
    name := "Tr069",
    packageSummary := "FreeACS Tr069",
    packageDescription := "FreeACS Tr069",
    scriptClasspath := Seq("*"),
    defaultLinuxInstallLocation := "/opt/freeacs",
    libraryDependencies ++= Dependencies.springBoot
      ++ Dependencies.database
      ++ Dependencies.testing
      ++ Dependencies.jdeb
      ++ Seq("org.apache.commons" % "commons-lang3" % "3.7")
  )
  .enablePlugins(JavaServerAppPackaging, SystemdPlugin, JDebPackaging)
  .dependsOn(dbi)

lazy val syslog = (project in file("syslog"))
  .settings(
    commonSettings,
    dockerSettings,
    name := "Syslog",
    packageSummary := "FreeACS Syslog",
    packageDescription := "FreeACS Syslog",
    scriptClasspath := Seq("*"),
    defaultLinuxInstallLocation := "/opt/freeacs",
    libraryDependencies ++= Dependencies.springBoot
      ++ Dependencies.database
      ++ Dependencies.testing
      ++ Dependencies.jdeb
      ++ List("commons-io" % "commons-io" % "1.3.2")
  )
  .enablePlugins(JavaServerAppPackaging, SystemdPlugin, JDebPackaging)
  .dependsOn(dbi)

lazy val stun = (project in file("stun"))
  .settings(
    commonSettings,
    dockerSettings,
    name := "Stun",
    packageSummary := "FreeACS Stun",
    packageDescription := "FreeACS Stun",
    scriptClasspath := Seq("*"),
    defaultLinuxInstallLocation := "/opt/freeacs",
    libraryDependencies ++= Dependencies.springBoot
      ++ Dependencies.database
      ++ Dependencies.testing
      ++ Dependencies.jdeb
      ++ List(
      "org.apache.httpcomponents" % "httpclient" % "4.5.5",
      "commons-io" % "commons-io" % "1.3.2"
    )
  )
  .enablePlugins(JavaServerAppPackaging, SystemdPlugin, JDebPackaging)
  .dependsOn(dbi)

lazy val shell = (project in file("shell"))
  .settings(
    commonSettings,
    dockerSettings,
    name := "Shell",
    packageSummary := "FreeACS Shell",
    packageDescription := "FreeACS Shell",
    publish in Docker := {},
    defaultLinuxInstallLocation := "/opt/freeacs",
    libraryDependencies ++= Dependencies.database
      ++ Dependencies.testing
      ++ Dependencies.jdeb
      ++ List(
      "org.apache.httpcomponents" % "httpclient" % "4.5.5",
      "commons-io" % "commons-io" % "1.3.2",
      "jline" % "jline" % "0.9.5",
      "dom4j" % "dom4j" % "1.6.1"
    )
  )
  .enablePlugins(JavaAppPackaging, JDebPackaging)
  .dependsOn(dbi)

lazy val core = (project in file("core"))
  .settings(
    commonSettings,
    dockerSettings,
    name := "core",
    packageSummary := "FreeACS Core",
    packageDescription := "FreeACS Core",
    scriptClasspath := Seq("*"),
    defaultLinuxInstallLocation := "/opt/freeacs",
    libraryDependencies ++= Dependencies.springBoot
      ++ Dependencies.database
      ++ Dependencies.testing
      ++ Dependencies.jdeb
      ++ List(
      "org.apache.httpcomponents" % "httpclient" % "4.5.5",
      "commons-io" % "commons-io" % "1.3.2"
    )
  )
  .enablePlugins(JavaServerAppPackaging, SystemdPlugin, JDebPackaging)
  .dependsOn(shell)

lazy val root = (project in file(".") settings (publish := {}))
  .aggregate(common, dbi, web, webservice, tr069, syslog, stun, shell, core)
