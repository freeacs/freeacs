#!/bin/bash

# Download resources from freeacs.com
download_freeacs() {

  echo ""
  echo "Downloads all necessary resources from freeacs.com:"

  yum install epel-release -y
  yum install jq -y
  jq --version

  cleanup

  whattodownload="rpm|tables|pdf"
  curl -s https://api.github.com/repos/freeacs/freeacs/releases/latest | jq -r ".assets[] | select(.name | test(\"${whattodownload}\")) | .browser_download_url" > files.txt
  awk '{print $0;}' files.txt | xargs -l1 wget
  unzip "*.zip"
  rm -rf *.zip

  echo ""
  echo "All necessary FreeACS resources are available."
  echo ""
}

create_freeacsdbuser() {
  echo "Using mysql root pw: $mysqlRootPass"
  acsPass="$(pwmake 128)"
  echo "Generated acs password is $acsPass"
  freeacsdbuserok=`mysql -uroot -p$mysqlRootPass -e "SELECT count(user) FROM mysql.user where user = 'acs'" 2> /dev/null | tail -n1`
  if [ "$freeacsdbuserok" != '2' ] ; then
    mysql -uroot -p$mysqlRootPass -e "CREATE DATABASE acs" 2> /dev/null
    mysql -uroot -p$mysqlRootPass acs -e "CREATE USER 'acs'@'localhost' IDENTIFIED BY '$acsPass'" 2> /dev/null
    mysql -uroot -p$mysqlRootPass acs -e "GRANT ALL ON acs.* TO 'acs' IDENTIFIED BY '$acsPass'"  2> .tmp
    mysql -uroot -p$mysqlRootPass acs -e "GRANT ALL ON acs.* TO 'acs'@'localhost' IDENTIFIED BY '$acsPass'" 2>> .tmp
    freeacsdbuserok=`mysql -uroot -p$mysqlRootPass -e "SELECT count(user) FROM mysql.user where user = 'acs'" 2> /dev/null | tail -n1`
    if [ "$freeacsdbuserok" != '2' ] ; then
      echo "The FreeACS MySQL database users 'acs' and 'acs'@'localhost' is not found"
      echo "in the mysql.user table. Maybe you stated the wrong MySQL root password??"
      echo "Please make sure this is corrected, either by running this script again with"
      echo "the correct root password or by running the equivalent of the following"
      echo " SQL-statements:"
      echo ""
      echo "Running as MySQL Root user:"
      echo "  CREATE DATABASE acs"
      echo "  GRANT ALL ON acs.* TO 'acs' IDENTIFIED BY 'A_PASSWORD'"
      echo "  GRANT ALL ON acs.* TO 'acs'@'localhost' IDENTIFIED BY 'A_PASSWORD'"
      echo ""
      echo "Below are stderr output from the commands above - they may indicate"
      echo "the problem at hand:"
      echo "------------------------------------------------"
      cat .tmp
      echo "------------------------------------------------"
      echo ""
      exit
    else
      echo ""
      echo "The FreeACS MySQL database user is OK. "
  	  echo ""
    fi
  else
    echo ""
    echo "The FreeACS MySQL database user is OK. "
    echo ""
  fi
}

load_database_tables() {
  echo ""
  echo "Loads all FreeACS table defintions into MySQL"
  mysql -uacs -p$acsPass acs < install.sql 2> .tmp
  installtables=`wc -l .tmp | cut -b1-1`
  if [ "$installtables" != '1' ] ; then
    echo "The output from the installation of the tables indicate some"
    echo "errors occurred:"
	echo "------------------------------------------------"
	cat .tmp
	echo "------------------------------------------------"
	exit
  else
    echo "Loading of all FreeACS tables was OK"
  fi
}

install_mysql() {
    mysqlRootPass="$(pwmake 128)"

    echo ' -> Removing previous mysql server installation'
    systemctl stop mysqld.service && yum remove -y mysql-community-server && rm -rf /var/lib/mysql && rm -rf /var/log/mysqld.log && rm -rf /etc/my.cnf

    echo ' -> Installing mysql server (community edition)'
    yum localinstall -y https://dev.mysql.com/get/mysql57-community-release-el7-7.noarch.rpm
    yum install -y mysql-community-server

    echo ' -> Starting mysql server (first run)'
    systemctl enable mysqld.service
    systemctl start mysqld.service
    tempRootDBPass="`grep 'temporary.*root@localhost' /var/log/mysqld.log | tail -n 1 | sed 's/.*root@localhost: //'`"

    echo ' -> Setting up new mysql server root password'
    systemctl stop mysqld.service
    rm -rf /var/lib/mysql/*logfile*
    wget -O /etc/my.cnf "https://my-site.com/downloads/mysql/512MB.cnf"
    systemctl start mysqld.service
    mysqladmin -u root --password="$tempRootDBPass" password "$mysqlRootPass"
    mysql -u root --password="$mysqlRootPass" -e <<-EOSQL
        DELETE FROM mysql.user WHERE User='';
        DROP DATABASE IF EXISTS test;
        DELETE FROM mysql.db WHERE Db='test' OR Db='test\\_%';
        DELETE FROM mysql.user where user != 'mysql.sys';
        CREATE USER 'root'@'%' IDENTIFIED BY '${mysqlRootPass}';
        GRANT ALL ON *.* TO 'root'@'%' WITH GRANT OPTION;
        FLUSH PRIVILEGES;
EOSQL
    systemctl status mysqld.service
    echo " -> MySQL server installation completed, root password: $mysqlRootPass";
}

module_setup() {
  module="$1"
  echo "$module installation start"
  systemctl disable freeacs-$module
  rpm -Uvh freeacs-$module*.rpm
  echo "$module installation complete"
}

cleanup() {
  rm .tmp
  rm -rf freeacs-*.rpm
  rm -rf tables.zip
}

read -p "Do you run this script with (root) permission? (y/n) " yn
case $yn in
  [Yy]* ) echo "" ;;
  *     ) echo "Installation must be run with root permission."
          exit;;
esac

read -p "This script will remove any existing mysql server and reinstall it. Continue? (y/n) " yn
case $yn in
  [Yy]* ) echo "" ;;
  *     ) echo "The script cannot continue."
          exit;;
esac

download_freeacs
install_mysql
create_freeacsdbuser
load_database_tables
module_setup core
module_setup stun
module_setup tr069
module_setup syslog
module_setup web
module_setup webservice
module_setup shell
echo "Generated mysql root pw: $mysqlRootPass"
echo "Generated acs password is $acsPass"