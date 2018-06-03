FROM java
MAINTAINER jarl.andre@gmail.com
ENV MAIN_DATASOURCE_HOST=db.main
ENV MAIN_DATASOURCE_PORT=3306
ENV MAIN_DATASOURCE_DB=acs
ENV SYSLOG_DATASOURCE_HOST=db.syslog
ENV SYSLOG_DATASOURCE_PORT=3306
ENV SYSLOG_DATASOURCE_DB=acs
ENTRYPOINT /usr/bin/java \
    -Dmain.datasource.jdbcUrl=jdbc:mysql://$MAIN_DATASOURCE_HOST:$MAIN_DATASOURCE_PORT/$MAIN_DATASOURCE_DB \
    -Dsyslog.datasource.jdbcUrl=jdbc:mysql://$SYSLOG_DATASOURCE_HOST:$SYSLOG_DATASOURCE_PORT/$SYSLOG_DATASOURCE_DB \
    -jar /usr/share/backend/backend.jar
COPY *.jar /usr/share/backend/backend.jar