autoScalaLibrary := false

resolvers += Resolver.mavenLocal

libraryDependencies ++= List(
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "junit" % "junit" % "4.12" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test",
  "mysql" % "mysql-connector-java" % "8.0.11",
  "org.jfree" %  "jcommon" % "1.0.17",
  "org.jfree" %  "jfreechart" % "1.0.17"
)

testOptions += Tests.Argument(TestFrameworks.JUnit)
fork in Test := true