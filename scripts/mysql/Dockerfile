FROM mysql:5.7.15

MAINTAINER freeacs

ENV MYSQL_DATABASE=acs \
    MYSQL_USER=acs \
    MYSQL_PASSWORD=acs \
    MYSQL_RANDOM_ROOT_PASSWORD=true

ADD schema.sql /docker-entrypoint-initdb.d

EXPOSE 3306