#!/usr/bin/env bash
cd "$(dirname "$0")"
cd ..
mvn package -DskipTests
declare -a arr=("core" "monitor" "stun" "syslog" "tr069" "web" "webservice")
for i in "${arr[@]}"
do
   docker build "$i" -t freeacs/"$i"
done
