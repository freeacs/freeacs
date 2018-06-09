import sbt.Keys.fork

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))

lazy val dockerSettings = Seq(
  maintainer in Docker := "Jarl Andre Hubenthal <jarl.andre@gmail.com>",
  dockerRepository := Some("freeacs"),
  dockerUpdateLatest := true,
  dockerExposedPorts := Seq(8080, 8080),
  dockerExposedVolumes := Seq("/opt/docker/logs", "/opt/docker/conf")
)

lazy val packagingSettings = Seq(
  scriptClasspath := Seq("*"),
  defaultLinuxInstallLocation := "/opt",
  daemonUser := "freeacs",
  daemonGroup := "freeacs"
)

lazy val commonSettings = Seq(
  maintainer := "Jarl Andre Hubenthal <jarl.andre@gmail.com>",
  organization := "com.github.freeacs",
  scalaVersion := "2.12.6",
  crossPaths := false,
  resolvers += Resolver.mavenLocal,
  autoScalaLibrary := false,
  testOptions += Tests.Argument(TestFrameworks.JUnit),
  fork in Test := true,
  evictionWarningOptions in update := EvictionWarningOptions.default.withWarnTransitiveEvictions(false),
  releaseUseGlobalVersion := false,
  publishTo := publishTo.value,
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
    packagingSettings,
    name := "FreeACS Web",
    packageSummary := "FreeACS Web",
    packageDescription := "FreeACS Web",
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
    packagingSettings,
    name := "FreeACS Webservice",
    packageSummary := "FreeACS Webservice",
    packageDescription := "FreeACS Webservice",
    xjcCommandLine += "-verbose",
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
    packagingSettings,
    name := "FreeACS Tr069",
    packageSummary := "FreeACS Tr069",
    packageDescription := "FreeACS Tr069",
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
    packagingSettings,
    name := "FreeACS Syslog",
    packageSummary := "FreeACS Syslog",
    packageDescription := "FreeACS Syslog",
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
    packagingSettings,
    name := "FreeACS Stun",
    packageSummary := "FreeACS Stun",
    packageDescription := "FreeACS Stun",
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
    packagingSettings,
    name := "FreeACS Shell",
    packageSummary := "FreeACS Shell",
    packageDescription := "FreeACS Shell",
    publish in Docker := {},
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
    packagingSettings,
    name := "FreeACS Core",
    packageSummary := "FreeACS Core",
    packageDescription := "FreeACS Core",
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
