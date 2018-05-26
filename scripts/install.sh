#!/bin/bash

###########################
# FUNCTION DEFINTIONS BEGIN
###########################

# Install the applications/programs need to run FreeACS
install_basic() {
  apt-get update && apt-get upgrade
  apt-get install unzip zip curl wget jq gawk
  apt-get install mysql-server-5.7
  apt-get install default-jre
}

# Checks to see if installation is ok
check_java_installation() {
  javaok='n'
  java -version 2> .tmp
  if grep -q "OpenJDK" .tmp -o grep -q "1.8" .tmp ; then
    javaok='y'
  fi
  if [ $javaok = 'n' ] ; then
    echo "The command 'java -version' doesn't seem to return the expected 'OpenJDK' and"
    echo "'1.7' strings. One possible explanation is that OpenJDK is not the default"
    echo "java of your system. Make sure this is corrected before you continue"
    exit
  else
    echo "Java OK"
  fi
}

check_mysql_installation() {
  mysqlok=`mysql --version | grep 5.7 | wc -l`
  if [ $mysqlok != '1' ] ; then
    echo "The command 'mysql --version' doesn't seem to return the expected '5.6' string."
	echo "One explanation is that you've installed the wrong version of MySQL. Please"
	echo "correct this before you continue"
	exit
  else
    echo "MySQL OK"
  fi
}

# Download resources from freeacs.com
download_freeacs() {

  echo ""
  echo "Downloads all necessary resources from freeacs.com:"

  ./download.sh

  echo ""
  echo "All necessary FreeACS resources are available."
  echo ""
}

create_freeacsdbuser() {
  echo "Using mysql root pw: $mysqlrootpw"
  freeacsdbuserok=`mysql -uroot -p$mysqlrootpw -e "SELECT count(user) FROM mysql.user where user = 'acs'" 2> /dev/null | tail -n1`
  if [ "$freeacsdbuserok" != '2' ] ; then
    mysql -uroot -p$mysqlrootpw -e "CREATE DATABASE acs" 2> /dev/null
    mysql -uroot -p$mysqlrootpw acs -e "CREATE USER 'acs'@'localhost' IDENTIFIED BY '$acsdbpw'" 2> /dev/null
    mysql -uroot -p$mysqlrootpw acs -e "GRANT ALL ON acs.* TO 'acs' IDENTIFIED BY '$acsdbpw'"  2> .tmp
    mysql -uroot -p$mysqlrootpw acs -e "GRANT ALL ON acs.* TO 'acs'@'localhost' IDENTIFIED BY '$acsdbpw'" 2>> .tmp
    freeacsdbuserok=`mysql -uroot -p$mysqlrootpw -e "SELECT count(user) FROM mysql.user where user = 'acs'" 2> /dev/null | tail -n1`
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
  mysql -uacs -p$acsdbpw acs < install.sql 2> .tmp
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

database_setup() {
  verified=""
  while [[ $verified != 'y' ]] && [[ $verified != 'Y' ]]
  do
    read -p "State the root password for the MySQL database: " mysqlrootpw
    read -p "Is [$mysqlrootpw] correct? (y/n) " verified
  done
  echo ""
  echo "Specify/create the password for the FreeACS MySQL user."
  echo "NB! The FreeACS MySQL user name defaults to 'acs'"
  echo "NB! If the user has been created before: Do not try "
  echo "to change the password - this script will not handle "
  echo "the change of password into MySQL, but the configuration"
  echo "files will be changed - causing a password mismatch!!"

  verified=""
  while [[ $verified != 'y' ]] && [[ $verified != 'Y' ]]
  do
    read -p "Specify/create the password for the FreeACS MySQL user: " acsdbpw
    read -p "Is [$acsdbpw] correct? (y/n) " verified
  done
  echo ""
  create_freeacsdbuser

  tablepresent=`mysql -uacs -p$acsdbpw acs -e "SHOW TABLES LIKE 'unit_type'" 2> /dev/null  | wc -l`
  if [ "$tablepresent" == "2" ] ; then
    echo "WARNING! An important FreeACS table is found in the database,"
	echo "indicating that the database tables have already been loaded. "
	echo "If you decide to load all table definitions, you will delete "
	echo "ALL FreeACS data in the database, and start over."
    verified='n'
    until [ $verified == 'y' ] || [ $verified == 'Y' ]; do
      read -p "Load all table defintions (and overwriting all data)? (y/n): " yn
      read -p "Is [$yn] correct? (y/n) " verified
    done
	echo
	if [ "$yn" == 'y' ] ; then
	  load_database_tables
	fi
  else
    load_database_tables
  fi
  echo ""

}

tomcat_setup() {
  echo "Tomcat setup"
}

shell_setup() {

  echo "Fusion shell (TODO)"
}

cleanup() {
  rm .tmp
}

#########################
# FUNCTION DEFINTIONS END
#########################





####################
# MAIN PROGRAM START
####################

echo ""
echo "              ***************************************** "
echo "              * Fusion FreeACS Installation & Upgrade * "
echo "              ***************************************** "
echo ""
echo "This script does 90% of the installation of Fusion FreeACS. The last 10% "
echo "must be done manually (see Fusion Instalation.pdf) by changing the contents"
echo "of various files. Run the script again to download and install new releases"
echo "or if something didn't work out the first time you installed. If default"
echo "configuration changes in a new release, you will be duly notified."
echo ""
echo "The script is targeted at Ubuntu 14.04 64-bit, but may also work with other"
echo "Ubuntu versions, like Ubuntu 12.04 LTS."
echo ""


#################################
# Prepare system for installation
#################################

read -p "Do you run this script with (root) permission? (y/n) " yn
case $yn in
  [Yy]* ) echo "" ;;
  *     ) echo "Installation must be run with root permission."
          exit;;
esac

read -p "Upgrade Ubuntu, install mysql-5.6, openjdk-1.7 and tomcat-7? (y/n) " answer
echo ""
case $answer in
  [Yy]* ) install_basic ;;
esac
check_java_installation
check_mysql_installation
echo ""

############################
# Installs FreeACS resources
############################

download_freeacs
database_setup
tomcat_setup
shell_setup
cleanup

echo "NB! Installation is now 90% complete. Continue with the modifications"
echo "described in the 'Fusion Installation.pdf' document, chapter 4 (the"
echo "document should be present in this folder)"
echo ""
echo "If this is an update, no need to do anything else"