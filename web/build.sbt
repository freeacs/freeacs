autoScalaLibrary := false

resolvers += Resolver.mavenLocal

libraryDependencies ++= List(
  "mysql" % "mysql-connector-java" % "8.0.11",
  "com.github.freeacs" %  "dbi" % "2.0.1-SNAPSHOT",
  "com.typesafe" %  "config" % "1.3.3",
  "javax.mail" %  "javax.mail-api" % "1.6.1",
  "commons-fileupload" %  "commons-fileupload" % "1.3",
  "commons-cli" %  "commons-cli" % "1.1",
  "commons-codec" %  "commons-codec" % "1.4",
  "commons-lang" %  "commons-lang" % "2.4",
  "commons-logging" %  "commons-logging" % "1.0.4",
  "commons-net" %  "commons-net" % "2.2",
  "commons-httpclient" %  "commons-httpclient" % "3.1",
  "dom4j" %  "dom4j" % "1.6.1",
  "net.sf.flexjson" %  "flexjson" % "2.1",
  "org.freemarker" %  "freemarker" % "2.3.14",
  "org.springframework" %  "spring-context-support" % "5.0.6.RELEASE",
  "org.codehaus.jackson" %  "jackson-core-asl" % "1.6.4",
  "org.codehaus.jackson" %  "jackson-mapper-asl" % "1.6.4",
  "jaxen" %  "jaxen" % "1.1.6",
  "javax.xml" %  "jaxrpc" % "1.1",
  "org.jfree" %  "jcommon" % "1.0.17",
  "org.jfree" %  "jfreechart" % "1.0.17",
  "com.metaparadigm" %  "json-rpc" % "1.0",
  "org.json" %  "json" % "20140107",
  "org.springframework.boot" %  "spring-boot-starter-web" % "2.0.2.RELEASE",
  "org.springframework.boot" %  "spring-boot-starter-actuator" % "2.0.2.RELEASE",
  "org.springframework.boot" %  "spring-boot-starter-security" % "2.0.2.RELEASE",
  "com.zaxxer" % "HikariCP" % "3.1.0",
  "org.springframework.boot" %  "spring-boot-starter-test" % "2.0.2.RELEASE" % "test",
  "javax.servlet" % "servlet-api" % "2.5" % "provided",
  "junit" % "junit" % "4.12" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test",
  "org.flywaydb" % "flyway-core" % "5.0.7" % "test",
  "com.h2database" % "h2" % "1.4.197" % "test"
)

scriptClasspath := Seq("*")
enablePlugins(JavaAppPackaging)
testOptions += Tests.Argument(TestFrameworks.JUnit)
fork in Test := true