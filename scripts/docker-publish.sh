#!/usr/bin/env bash
declare -a arr=("core" "monitor" "stun" "syslog" "tr069" "web" "webservice")
for i in "${arr[@]}"
do
   docker push freeacs/"$i":latest
done