name := "FreeACS Common"

normalizedName := "freeacs-common"

libraryDependencies ++= List(
  "org.apache.httpcomponents" % "httpclient" % "4.5.5",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "junit" % "junit" % "4.12" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test",
  "mysql" % "mysql-connector-java" % "8.0.11"
)