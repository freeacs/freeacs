#!/usr/bin/env bash
cd "$(dirname "$0")"
cd ..
mvn clean package -DskipTests
declare -a arr=("core" "monitor" "stun" "syslog" "tr069" "web" "webservice")
for i in "${arr[@]}"
do
   docker build "$i" -t freeacs/"$i"
done
cd scripts
docker-compose up -d --build
