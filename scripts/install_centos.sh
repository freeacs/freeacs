#!/bin/bash

# Download resources from freeacs.com
download_freeacs() {

  echo ""
  echo "Downloads all necessary resources from freeacs.com:"

  yum install epel-release -y
  yum install curl wget jq unzip -y
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
  freeacsdbuserok=`mysql -uroot -p"${mysqlRootPass}" -e "SELECT count(user) FROM mysql.user where user = 'acs'" 2> /dev/null | tail -n1`
  if [[ "$freeacsdbuserok" != '2' ]] ; then
    mysql -uroot -p"${mysqlRootPass}" -e "CREATE DATABASE acs" 2> /dev/null
    mysql -uroot -p"${mysqlRootPass}" -e "uninstall plugin validate_password" 2> /dev/null
    mysql -uroot -p"${mysqlRootPass}" acs -e "CREATE USER 'acs'@'localhost' IDENTIFIED BY '$acsPass'" 2> /dev/null
    mysql -uroot -p"${mysqlRootPass}" acs -e "GRANT ALL ON acs.* TO 'acs' IDENTIFIED BY '$acsPass'"  2> .tmp
    mysql -uroot -p"${mysqlRootPass}" acs -e "GRANT ALL ON acs.* TO 'acs'@'localhost' IDENTIFIED BY '$acsPass'" 2>> .tmp
    freeacsdbuserok=`mysql -uroot -p"${mysqlRootPass}" -e "SELECT count(user) FROM mysql.user where user = 'acs'" 2> /dev/null | tail -n1`
    if [[ "$freeacsdbuserok" != '2' ]] ; then
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
  mysql -uacs -p"${acsPass}" acs < install.sql 2> .tmp
  installtables=`wc -l .tmp | cut -b1-1`
  if [[ "$installtables" != '1' ]] ; then
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
  rpm -Uvh freeacs-$module-*.rpm
  systemctl restart freeacs-$module
  echo "$module installation complete"
}

cleanup() {
  rm .tmp
  rm -rf freeacs-*.rpm
  rm -rf tables.zip
}

echo ""
echo "              ***************************************** "
echo "              * FreeACS Installation & Upgrade *        "
echo "              ***************************************** "
echo ""
echo "This script does 90% of the installation of Fusion FreeACS. The last 10% "
echo "must be done manually (see Fusion Instalation.pdf) by changing the contents"
echo "of various files. Run the script again to download and install new releases"
echo "or if something didn't work out the first time you installed. If default"
echo "configuration changes in a new release, you will be duly notified."
echo ""
echo "The script is targeted at CentOS 7"
echo ""

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
yum install java-1.8.0-openjdk-headless -y
module_setup core
module_setup stun
module_setup tr069
module_setup syslog
module_setup web
module_setup webservice
module_setup shell
module_setup monitor
if rpm -q nginx; then
echo "Ngninx is already installed. If this is fresh install add the following to http.server configuration in nginx or similar"
echo "
    location /tr069 {
      proxy_set_header        X-Real-IP       \$remote_addr;
      proxy_pass http://localhost:8085/tr069;
    }
    location /web/ {
      proxy_pass http://localhost:8081/web/;
    }
    location /monitor/ {
      proxy_pass http://localhost:8090/monitor/;
    }
    location /webservice/ {
      proxy_pass http://localhost:8088/webservice/;
    }
    location /syslog/ {
      proxy_pass http://localhost:8086/syslog/;
    }
    location /core/ {
      proxy_pass http://localhost:8083/core/;
    }
    location /stun/ {
      proxy_pass http://localhost:8087/stun/;
    }
"
else
echo "nginx is not installed, installing it now"
yum install nginx -y
cat > /etc/nginx/nginx.conf <<- EOM
events {
  worker_connections  19000;
}

http {
  server {
    listen       80;
    server_name  localhost;

    # For the geeks: "A man is not dead while his name is still spoken." -Terry Pratchett
    add_header X-Clacks-Overhead "GNU Terry Pratchett";
    location = / {
      return 301 /web/index;
    }
    location /tr069/ {
      proxy_set_header        X-Real-IP       \$remote_addr;
      proxy_pass http://localhost:8085/tr069/;
    }
    location /web/ {
      proxy_pass http://localhost:8081/web/;
    }
    location /monitor/ {
      proxy_pass http://localhost:8090/monitor/;
    }
    location /webservice/ {
      proxy_pass http://localhost:8088/webservice/;
    }
    location /syslog/ {
      proxy_pass http://localhost:8086/syslog/;
    }
    location /core/ {
      proxy_pass http://localhost:8083/core/;
    }
    location /stun/ {
      proxy_pass http://localhost:8087/stun/;
    }
  }
}
EOM
systemctl enable nginx
systemctl restart nginx
fi
setsebool -P httpd_can_network_connect 1
echo "Generated mysql root pw: $mysqlRootPass"
echo "Generated acs password is $acsPass"
echo "Updating property files in module... "
sed -i -e '/main.datasource.password=/ s/=acs/='\""$acsPass"\"'/' /opt/freeacs-web/config/application-config.conf
sed -i -e '/main.datasource.password=/ s/=acs/='"$acsPass"'/' /opt/freeacs-webservice/config/application-config.properties
sed -i -e '/main.datasource.password=/ s/=acs/='"$acsPass"'/' /opt/freeacs-shell/config/application-config.properties
sed -i -e '/main.datasource.password=/ s/=acs/='\""$acsPass"\"'/' /opt/freeacs-tr069/config/application-config.conf
sed -i -e '/main.datasource.password=/ s/=acs/='\""$acsPass"\"'/' /opt/freeacs-core/config/application-config.conf
sed -i -e '/main.datasource.password=/ s/=acs/='\""$acsPass"\"'/' /opt/freeacs-stun/config/application-config.conf
sed -i -e '/main.datasource.password=/ s/=acs/='\""$acsPass"\"'/' /opt/freeacs-syslog/config/application-config.conf
echo "Done"
rm -rf *.rpm
systemctl restart freeacs-*