cd "$(dirname "$0")"
java -jar \
-Xms256m \
-Xmx1024m \
-XX:MaxMetaspaceSize=256m \
-XX:CompressedClassSpaceSize=128m \
-Dlogging.config=config/logback.xml \
-Dspring.config.location=config/application.conf \
core.jar