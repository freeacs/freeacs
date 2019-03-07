docker login -e $DOCKER_EMAIL -u $DOCKER_USER -p $DOCKER_PASS
declare -a arr=("core" "monitor" "stun" "syslog" "tr069" "web" "webservice")
for i in "${arr[@]}"
do
   docker push freeacs/"$i":latest
done