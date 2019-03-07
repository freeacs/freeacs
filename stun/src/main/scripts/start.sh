#!/usr/bin/env bash
java -jar \
-Xms256m \
-Xmx1024m \
-XX:MaxMetaspaceSize=256m \
-XX:CompressedClassSpaceSize=128m \
-Dlogging.config=config/logback.xml \
-Dspring.config.location=config/application.conf \
stun.jar