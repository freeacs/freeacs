import sbt.Keys.fork
import DebianConstants._
import Dependencies.{h2, hikari, mysql}

publishTo in ThisBuild := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))

javacOptions in ThisBuild ++= Seq("-encoding", "UTF-8")

lazy val copyAppProps = mappings in Universal ++= {
  ((sourceDirectory in Compile).value / "resources" * "application.properties").get.map { f =>
    f -> s"config/application-config.properties"
  }
}

lazy val copyAppConfig = mappings in Universal ++= {
  ((sourceDirectory in Compile).value / "resources" * "application.conf").get.map { f =>
    f -> s"config/application-config.conf"
  }
}

lazy val copyLogProps = mappings in Universal ++= {
  ((sourceDirectory in Compile).value / "resources" * "logback.xml").get.map { f =>
    f -> "config/logback.xml"
  }
}

lazy val copyAppIni = mappings in Universal ++= {
  ((sourceDirectory in Compile).value / "resources" * "application.ini").get.map { f =>
    f -> "conf/application.ini"
  }
}

lazy val dockerSettings = Seq(
  maintainer in Docker := "Jarl Andre Hubenthal <jarl.andre@gmail.com>",
  dockerBaseImage := "openjdk:8-jdk",
  dockerRepository := Some("freeacs"),
  dockerUpdateLatest := true,
  dockerExposedPorts := Seq(8080, 8080),
  dockerExposedVolumes := Seq("/opt/docker/logs", "/opt/docker/config")
)

lazy val packagingSettings = Seq(
  defaultLinuxInstallLocation := "/opt",
  daemonUser := "freeacs",
  daemonGroup := "freeacs",
  maintainerScripts in Debian := maintainerScriptsAppend((maintainerScripts in Debian).value)(
    Conffiles ->
      s"""/opt/${normalizedName.value}/config/application-config.properties
         |/opt/${normalizedName.value}/config/application-config.conf
         |/etc/default/${normalizedName.value}
         |/lib/systemd/system/${normalizedName.value}.service
      """.stripMargin,
  ),
  rpmGroup := Some("Other"),
  rpmRelease := "1",
  rpmUrl := Some("https://github.com/freeacs/freeacs"),
  rpmLicense := Some("The MIT License (MIT)"),
  rpmVendor := "freeacs",
  rpmAutoreq := "false",
  serviceAutostart := false
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
  dependencyOverrides ++= Seq(
    "com.zaxxer" % "HikariCP" % "3.1.0",
    "commons-io" % "commons-io" % "2.4"
  )
)

lazy val common = (project in file("common"))
  .settings(
    commonSettings,
    name := "FreeACS Common",
    normalizedName := "freeacs-common",
    publish := {},
    libraryDependencies ++= Dependencies.testing
      ++ List(
      "com.zaxxer" % "HikariCP" % "3.1.0",
      "org.apache.httpcomponents" % "httpclient" % "4.5.5",
      "commons-httpclient" % "commons-httpclient" % "3.1",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.sparkjava" % "spark-core" % "2.8.0",
      "com.typesafe" % "config" % "1.3.3",
      "org.apache.commons" % "commons-lang3" % "3.7"
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
      "org.jfree" % "jcommon" % "1.0.17" % "provided",
      "org.jfree" % "jfreechart" % "1.0.17" % "provided"
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
    libraryDependencies ++= Seq(mysql, hikari, h2)
      ++ Dependencies.testing
      ++ Seq(
      "commons-fileupload" % "commons-fileupload" % "1.3",
      "commons-httpclient" % "commons-httpclient" % "3.1",
      "org.freemarker" % "freemarker" % "2.3.14",
      "org.jfree" % "jcommon" % "1.0.17",
      "org.jfree" % "jfreechart" % "1.0.17",
      "com.sparkjava" % "spark-template-freemarker" % "2.7.1",
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.7",
      "org.seleniumhq.selenium" % "selenium-java" % "3.141.59" % "test"
    ),
    copyAppConfig,
    copyLogProps,
    copyAppIni
  )
  .enablePlugins(JavaServerAppPackaging, SystemdPlugin, JDebPackaging, RpmPlugin, DockerPlugin)
  .dependsOn(dbi)

lazy val monitor = (project in file("monitor"))
  .settings(
    commonSettings,
    dockerSettings,
    packagingSettings,
    name := "FreeACS Monitor",
    packageSummary := "FreeACS Monitor",
    packageDescription := "FreeACS Monitor",
    libraryDependencies ++= Seq(
      "org.freemarker" % "freemarker" % "2.3.14",
      "commons-httpclient" % "commons-httpclient" % "3.1"
    ),
    copyAppConfig,
    copyLogProps,
    copyAppIni
  )
  .enablePlugins(JavaServerAppPackaging, SystemdPlugin, JDebPackaging, RpmPlugin, DockerPlugin)
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
      ++ Dependencies.springBootWebservices
      ++ Seq("wsdl4j" % "wsdl4j" % "1.6.3"),
    copyAppProps,
    copyLogProps,
    copyAppIni
  )
  .enablePlugins(JavaServerAppPackaging, SystemdPlugin, JDebPackaging, RpmPlugin, DockerPlugin)
  .dependsOn(dbi)

lazy val tr069 = (project in file("tr069"))
  .settings(
    commonSettings,
    dockerSettings,
    packagingSettings,
    name := "FreeACS Tr069",
    packageSummary := "FreeACS Tr069",
    packageDescription := "FreeACS Tr069",
    libraryDependencies ++= Dependencies.database
      ++ Dependencies.testing
      ++ Seq(
        "com.mashape.unirest" % "unirest-java" % "1.4.9" % "test"
      ),
    copyAppConfig,
    copyLogProps,
    copyAppIni
  )
  .enablePlugins(JavaServerAppPackaging, SystemdPlugin, JDebPackaging, RpmPlugin, DockerPlugin)
  .dependsOn(dbi)

lazy val syslog = (project in file("syslog"))
  .settings(
    commonSettings,
    dockerSettings,
    packagingSettings,
    name := "FreeACS Syslog",
    packageSummary := "FreeACS Syslog",
    packageDescription := "FreeACS Syslog",
    libraryDependencies ++= Dependencies.database
      ++ Dependencies.testing
      ++ List("commons-io" % "commons-io" % "1.3.2"),
    copyAppConfig,
    copyLogProps,
    copyAppIni
  )
  .enablePlugins(JavaServerAppPackaging, SystemdPlugin, JDebPackaging, RpmPlugin, DockerPlugin)
  .dependsOn(dbi)

lazy val stun = (project in file("stun"))
  .settings(
    commonSettings,
    dockerSettings,
    packagingSettings,
    name := "FreeACS Stun",
    packageSummary := "FreeACS Stun",
    packageDescription := "FreeACS Stun",
    libraryDependencies ++= Dependencies.database
      ++ Dependencies.testing
      ++ List(
      "org.apache.httpcomponents" % "httpclient" % "4.5.5",
      "commons-io" % "commons-io" % "1.3.2",
      "org.mockito" % "mockito-core" % "2.21.0" % Test
    ),
    copyAppConfig,
    copyLogProps,
    copyAppIni
  )
  .enablePlugins(JavaServerAppPackaging, SystemdPlugin, JDebPackaging, RpmPlugin, DockerPlugin)
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
      ++ List(
      "org.apache.httpcomponents" % "httpclient" % "4.5.5",
      "commons-io" % "commons-io" % "1.3.2",
      "jline" % "jline" % "0.9.5",
      "dom4j" % "dom4j" % "1.6.1"
    ),
    copyAppProps,
    copyLogProps,
    copyAppIni
  )
  .enablePlugins(JavaAppPackaging, JDebPackaging, RpmPlugin)
  .dependsOn(dbi)

lazy val core = (project in file("core"))
  .settings(
    commonSettings,
    dockerSettings,
    packagingSettings,
    name := "FreeACS Core",
    packageSummary := "FreeACS Core",
    packageDescription := "FreeACS Core",
    libraryDependencies ++= Dependencies.database
      ++ Dependencies.testing
      ++ List(
      "org.apache.httpcomponents" % "httpclient" % "4.5.5",
      "commons-io" % "commons-io" % "1.3.2"
    ),
    copyAppConfig,
    copyLogProps,
    copyAppIni
  )
  .enablePlugins(JavaServerAppPackaging, SystemdPlugin, JDebPackaging, RpmPlugin, DockerPlugin)
  .dependsOn(shell)

lazy val root = (project in file(".") settings (publish := {}))
  .aggregate(common, dbi, web, monitor, webservice, tr069, syslog, stun, shell, core)
