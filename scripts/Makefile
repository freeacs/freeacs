###########################
#                         #
#     THIS MAKEFILE       #
#           IS            #
#     WORK IN PROGRESS    #
#                         #
###########################

INSTALLATION_PATH := /opt/freeacs
VERSION_NUMBER := latest
DOWNLOAD_ARTIFACTS := rpm|zip

check_env:
ifndef MYSQL_ROOT_PASSWORD
	$(error MYSQL_ROOT_PASSWORD is undefined)
endif
ifndef MYSQL_ACS_PASSWORD
	$(error MYSQL_ACS_PASSWORD is undefined)
endif

cleanup:
	@echo " -> cleaning up";
	rm -rf files.txt .tmp *.rpm *.zip *.deb *.pdf *.sql;
	@echo " -> done cleaning";

download_release: | cleanup
	@echo " -> downloading FreeACS resources"
	@echo "${DOWNLOAD_ARTIFACTS}"
	curl -s https://api.github.com/repos/freeacs/freeacs/releases/${VERSION_NUMBER} | \
		jq -r ".assets[] | select(.name | test(\"${DOWNLOAD_ARTIFACTS}\")) | .browser_download_url" \
		> files.txt;
	awk '{print $0;}' files.txt | xargs -L1 wget;
	unzip "*.zip";
	@echo " -> all necessary FreeACS resources are available.";

install_mysql: | check_env download_release
	@echo ' -> Removing previous mysql server installation'
	systemctl stop mysqld.service && \
		yum remove -y mysql-community-server && \
		rm -rf /var/lib/mysql && \
		rm -rf /var/log/mysqld.log && \
		rm -rf /etc/my.cnf
	@echo ' -> Installing mysql server (community edition)'
	yum localinstall -y https://dev.mysql.com/get/mysql57-community-release-el7-7.noarch.rpm
	yum install -y mysql-community-server
	@echo ' -> Starting mysql server (first run)'
	systemctl enable mysqld.service
	systemctl start mysqld.service
	$(eval TMP_ROOT_PASSWORD := $(`grep 'temporary.*root@localhost' /var/log/mysqld.log | tail -n 1 | sed 's/.*root@localhost: //'`))
	@echo ' -> Setting up new mysql server root password'
	mysqladmin -u root --password="${TMP_ROOT_PASSWORD}" password "${MYSQL_ROOT_PASSWORD}"
	systemctl status mysqld.service
	$(eval FREEACSUSEROK := $(mysql -uroot -p${MYSQL_ROOT_PASSWORD} -e "SELECT count(user) FROM mysql.user where user = 'acs'" 2> /dev/null | tail -n1`))
	if [ "${FREEACSUSEROK}" != '2' ] ; then
		mysql -uroot -p${MYSQL_ROOT_PASSWORD} -e "CREATE DATABASE acs" 2> /dev/nul
		mysql -uroot -p${MYSQL_ROOT_PASSWORD} -e "uninstall plugin validate_password" 2> /dev/null
		mysql -uroot -p${MYSQL_ROOT_PASSWORD} acs -e "CREATE USER 'acs'@'localhost' IDENTIFIED BY '${MYSQL_ACS_PASSWORD}'" 2> /dev/null
		mysql -uroot -p${MYSQL_ROOT_PASSWORD} acs -e "GRANT ALL ON acs.* TO 'acs' IDENTIFIED BY '${MYSQL_ACS_PASSWORD}'"  2> .tmp
		mysql -uroot -p${MYSQL_ROOT_PASSWORD} acs -e "GRANT ALL ON acs.* TO 'acs'@'localhost' IDENTIFIED BY '${MYSQL_ACS_PASSWORD}'" 2>> .tmp
	fi;
	$(eval FREEACSUSEROK := $(mysql -uroot -p${MYSQL_ROOT_PASSWORD} -e "SELECT count(user) FROM mysql.user where user = 'acs'" 2> /dev/null | tail -n1`))
	if [ "${FREEACSUSEROK}" != '2' ] ; then
		@echo "The FreeACS MySQL database users 'acs' and 'acs'@'localhost' is not found"
		@echo "in the mysql.user table. Maybe you stated the wrong MySQL root password??"
		@echo "Please make sure this is corrected, either by running this script again with"
		@echo "the correct root password or by running the equivalent of the following"
		@echo " SQL-statements:"
		@echo ""
		@echo "Running as MySQL Root user:"
		@echo "  CREATE DATABASE acs"
		@echo "  GRANT ALL ON acs.* TO 'acs' IDENTIFIED BY 'A_PASSWORD'"
		@echo "  GRANT ALL ON acs.* TO 'acs'@'localhost' IDENTIFIED BY 'A_PASSWORD'"
		@echo ""
		@echo "Below are stderr output from the commands above - they may indicate"
		@echo "the problem at hand:"
		@echo "------------------------------------------------"
		cat .tmp
		@echo "------------------------------------------------"
		@echo ""
		exit
	else
		@echo ""
		@echo "The FreeACS MySQL database user is OK. "
		@echo ""
	fi

load_tables:
	@echo "Loads all FreeACS table defintions into MySQL"
	mysql -uacs -p${MYSQL_ACS_PASSWORD} acs < install.sql 2> .tmp
	$(eval INSTALLTABLESOK := $(wc -l .tmp | cut -b1-1))
	if [ "${INSTALLTABLESOK}" != '1' ] ; then
		@echo "The output from the installation of the tables indicate some"
		@echo "errors occurred:"
		@echo "------------------------------------------------"
		cat .tmp
		@echo "------------------------------------------------"
		exit
	else
		@echo "Loading of all FreeACS tables was OK"
	fi

