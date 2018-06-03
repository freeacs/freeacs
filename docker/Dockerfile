FROM java:8-jre
MAINTAINER jarl.andre@gmail.com
ENV MAIN_DATASOURCE_HOST=db.main
ENV MAIN_DATASOURCE_PORT=3306
ENV MAIN_DATASOURCE_DB=acs
ENV MAIN_DATASOURCE_USER=acs
ENV MAIN_DATASOURCE_PASS=acs
ENV SYSLOG_DATASOURCE_HOST=db.syslog
ENV SYSLOG_DATASOURCE_PORT=3306
ENV SYSLOG_DATASOURCE_DB=acs
ENV SYSLOG_DATASOURCE_USER=acs
ENV SYSLOG_DATASOURCE_PASS=acs
ENV SPRING_CONFIG_LOCATION=config
ENV LOG_PATH=logs
WORKDIR /usr/share/backend
ENTRYPOINT /usr/bin/java \
    -Dmain.datasource.jdbcUrl=jdbc:mysql://$MAIN_DATASOURCE_HOST:$MAIN_DATASOURCE_PORT/$MAIN_DATASOURCE_DB \
    -Dmain.datasource.username=$MAIN_DATASOURCE_USER \
    -Dmain.datasource.password=$MAIN_DATASOURCE_PASS \
    -Dsyslog.datasource.jdbcUrl=jdbc:mysql://$SYSLOG_DATASOURCE_HOST:$SYSLOG_DATASOURCE_PORT/$SYSLOG_DATASOURCE_DB \
    -Dsyslog.datasource.username=$SYSLOG_DATASOURCE_USER \
    -Dsyslog.datasource.password=$SYSLOG_DATASOURCE_PASS \
    -DLOG_PATH=$LOG_PATH \
    -jar /usr/share/backend/backend.jar --spring.config.location=$SPRING_CONFIG_LOCATION
COPY *.jar /usr/share/backend/backend.jar