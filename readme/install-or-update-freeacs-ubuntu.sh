#!/bin/bash



###########################
# FUNCTION DEFINTIONS BEGIN
###########################

# Install the applications/programs need to run FreeACS
function install_basic {
  aptitude update && sudo aptitude safe-upgrade
  aptitude install unzip
  aptitude install zip
  aptitude install mysql-server-5.6
  aptitude install openjdk-7-jre-headless
  aptitude install tomcat7
}

# Checks to see if installation is ok
function check_java_installation {
  javaok='n'
  java -version 2> .tmp
  if grep -q "OpenJDK" .tmp -o grep -q "1.7" .tmp ; then
    javaok='y'
  fi
  if [ $javaok = 'n' ] ; then
    echo "The command 'java -version' doesn't seem to return the expected 'OpenJDK' and"
    echo "'1.7' strings. One possible explanation is that OpenJDK is not the default"
    echo "java of your system. Make sure this is corrected before you continue"
    exit
  else 
    echo "Java 1.7 OK"
  fi
}

function check_mysql_installation {
  mysqlok=`mysql --version | grep 5.6 | wc -l`
  if [ $mysqlok != '1' ] ; then
    echo "The command 'mysql --version' doesn't seem to return the expected '5.6' string."
	echo "One explanation is that you've installed the wrong version of MySQL. Please"
	echo "correct this before you continue"
	exit
  else 
    echo "MySQL 5.6 OK"
  fi
}

function check_tomcat_installation {
  tomcatok=`grep tomcat7 /etc/passwd | wc -l`
  if [ $tomcatok != '1' ] ; then
    echo "The command 'grep tomcat7 /etc/passwd' doesn't seem to return the expected"
	echo "'tomcat7' string, indicating that this user is missing from the system. This"
	echo "in turns indicates that Tomcat7 is not installed. Please correct this before"
	echo "you continue"
	exit
  else 
    echo "Tomcat 7 OK"
  fi
}

# Preparation to allow tomcat to use port 80 and port 443
function prepare_tomcat {
  touch /etc/authbind/byport/80
  chmod 500 /etc/authbind/byport/80
  chown tomcat7 /etc/authbind/byport/80
  touch /etc/authbind/byport/443
  chmod 500 /etc/authbind/byport/443
  chown tomcat7 /etc/authbind/byport/443
  # Tomcat must own the cacerts file
  chown tomcat7:tomcat7 /etc/ssl/certs/java/cacerts
}

# Download resources from freeacs.com
function download_freeacs {

  echo ""
  echo "Downloads all necessary resources from freeacs.com:"
  files=( "Fusion Installation.pdf" core.war install2013R1.sql monitor.war shell.jar spp.war stun.war syslog.war tr069.war web.war ws.war tables.zip )

  for i in "${files[@]}" 
  do
    if [ ! -f "$i" ] ; then
      echo "  downloading freeacs.com/download/$i"
      wget -q "freeacs.com/download/$i"
	else 
      rm "$i"*
	  if [ $? == "1" ] ; then
	    echo ""
	    echo "ERROR: The old installation files cannot be removed and overwritten - exits the "
		echo "installation"
		exit
	  fi 
	  echo "  downloading freeacs.com/download/$i (overwriting previous download)"
      wget -q "freeacs.com/download/$i"
	fi 
    if [ ! -f "$i" ] ; then
	  echo ""
	  echo "ERROR: The download failed OR the file could not be written to disk - exits the "
	  echo "installation"
	  exit
	fi
  done
  
  echo ""
  echo "All necessary FreeACS resources are available."
  echo ""
}

function create_freeacsdbuser {
  freeacsdbuserok=`mysql -uroot -p$mysqlrootpw -e "SELECT count(user) FROM mysql.user where user = 'xaps'" 2> /dev/null | tail -n1`
  if [ "$freeacsdbuserok" != '2' ] ; then
    mysql -uroot -p$mysqlrootpw -e "CREATE DATABASE xaps" 2> /dev/null
    mysql -uroot -p$mysqlrootpw xaps -e "GRANT ALL ON xaps.* TO 'xaps' IDENTIFIED BY '$acsdbpw'"  2> .tmp
    mysql -uroot -p$mysqlrootpw xaps -e "GRANT ALL ON xaps.* TO 'xaps'@'localhost' IDENTIFIED BY '$acsdbpw'" 2>> .tmp
    freeacsdbuserok=`mysql -uroot -p$mysqlrootpw -e "SELECT count(user) FROM mysql.user where user = 'xaps'" 2> /dev/null | tail -n1`
    if [ "$freeacsdbuserok" != '2' ] ; then
      echo "The FreeACS MySQL database users 'xaps' and 'xaps'@'localhost' is not found"
      echo "in the mysql.user table. Maybe you stated the wrong MySQL root password??"
      echo "Please make sure this is corrected, either by running this script again with"
      echo "the correct root password or by running the equivalent of the following"
      echo " SQL-statements:"
      echo ""
      echo "Running as MySQL Root user:"
      echo "  CREATE DATABASE xaps"
      echo "  GRANT ALL ON xaps.* TO 'xaps' IDENTIFIED BY 'A_PASSWORD'"
      echo "  GRANT ALL ON xaps.* TO 'xaps'@'localhost' IDENTIFIED BY 'A_PASSWORD'"
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

function load_database_tables {
  echo ""
  echo "Loads all FreeACS table defintions into MySQL"
  mysql -uxaps -p$acsdbpw xaps < install2013R1.sql 2> .tmp
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

function database_setup {
  mkdir tables 2> /dev/null
  unzip -o -q -d tables/ tables.zip
  
  verified='n'
  until [ $verified == 'y' ] || [ $verified == 'Y' ]; do
    read -p "State the root password for the MySQL database: " mysqlrootpw
    read -p "Is [$mysqlrootpw] correct? (y/n) " verified
  done
  echo ""
  echo "Specify/create the password for the FreeACS MySQL user."
  echo "NB! The FreeACS MySQL user name defaults to 'xaps'"
  echo "NB! If the user has been created before: Do not try "
  echo "to change the password - this script will not handle "
  echo "the change of password into MySQL, but the configuration"
  echo "files will be changed - causing a password mismatch!!"

  verified='n'
  until [ $verified == 'y' ] || [ $verified == 'Y' ]; do
    read -p "Specify/create the password for the FreeACS MySQL user: " acsdbpw
    read -p "Is [$acsdbpw] correct? (y/n) " verified
  done
  echo ""
  create_freeacsdbuser
  
  tablepresent=`mysql -uxaps -p$acsdbpw xaps -e "SHOW TABLES LIKE 'unit_type'" 2> /dev/null  | wc -l`
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

function tomcat_setup {
  mkdir /var/lib/tomcat7/shell 2> /dev/null
  echo ""
  # Extracts and removes all xaps-*.properties files from the jar/war archives 
  archives=( core.war monitor.war spp.war stun.war syslog.war tr069.war web.war ws.war )
  
  for i in "${archives[@]}"
  do
    unzip -j -q -o $i WEB-INF/classes/xaps*.properties > /dev/null 2>&1
    zip -d -q $i WEB-INF/classes/xaps*.properties > /dev/null 2>&1
  done

  unzip -j -q -o shell.jar xaps-shell*.properties > /dev/null 2>&1
  zip -d -q shell.jar xaps-shell*.properties > /dev/null 2>&1

  # Changes the default FreeACS MySQL password in the property files
  oldacsdbpw=`grep -e "^db.xaps.url" xaps-tr069.properties | cut -d" " -f3 | cut -b6-40 | cut -d"@" -f1`
  sed -i 's/xaps\/'$oldacsdbpw'/xaps\/'$acsdbpw'/g' *.properties
  
  echo "NB! Important! Checks to see whether you have some existing" 
  echo "configuration of FreeACS. In that case, a diff between the" 
  echo "existing config and the new default config is shown. The new"
  echo "default config is NOT applied, but you should inspect the"
  echo "diff to understand if new properties are added or old ones"
  echo "removed. If so, please update your property files accordingly." 
  echo "The diff is printed to file config-diff.txt.	"
  
  modules=( core monitor spp stun syslog tr069 web ws )
  echo "" > config-diff.txt
  for j in "${modules[@]}"
  do
	propertyfiles=( xaps-$j.properties xaps-$j-logs.properties )
    for propertyfile in "${propertyfiles[@]}"
	do
      overwrite=''
      if [ -f /var/lib/tomcat7/common/$propertyfile ] ; then
        diff /var/lib/tomcat7/common/$propertyfile $propertyfile > tmp
        if [ -s tmp ] ; then
          echo "  $propertyfile diff found, added diff to config-diff.txt - please inspect!"
          echo "$propertyfile diff:" >> config-diff.txt
          echo "------------------------------------------------" >> config-diff.txt
          cat tmp >> config-diff.txt
          echo "" >> config-diff.txt
		else 
		  mv -f $propertyfile /var/lib/tomcat7/common
        fi
	  else
	    mv -f $propertyfile /var/lib/tomcat7/common
      fi      
    done    
  done
  
  propertyfiles=( xaps-shell.properties xaps-shell-logs.properties )
  for propertyfile in "${propertyfiles[@]}"
  do
    if [ -f /var/lib/tomcat7/shell/$propertyfile ] ; then
      diff /var/lib/tomcat7/shell/$propertyfile $propertyfile > tmp
      if [ -s tmp ] ; then
        echo "  $propertyfile diff found, added diff to config-diff.txt - please inspect!"
        echo "$propertyfile diff:" >> config-diff.txt
        echo "------------------------------------------------" >> config-diff.txt
        cat tmp >> config-diff.txt
        echo "" >> config-diff.txt
	  else
	    mv -f $propertyfile /var/lib/tomcat7/shell
      fi
	else
	  mv -f $propertyfile /var/lib/tomcat7/shell
    fi      
  done
  echo "All property files have been checked. Those which weren't found"
  echo "in the system have been installed."
  echo ""

  # Copies all war, jar and property files into their correct location
  # This actually deploys the application into Tomcat  
  mv *.war /var/lib/tomcat7/webapps
  mv *.jar /var/lib/tomcat7/shell
  echo "All WAR/JAR/property files have been moved to Tomcat - servers have been deployed!"
  
  # Makes requests to http://hostname/ redirect to http://hostname/web
  rm -rf /var/lib/tomcat7/webapps/ROOT
  ln -s /var/lib/tomcat7/webapps/web /var/lib/tomcat7/webapps/ROOT
  
  # Changes all ownership and permissions - tomcat7 user owns everything
  chown -R tomcat7:tomcat7 /var/lib/tomcat7
  chmod g+w /var/lib/tomcat7/common /var/lib/tomcat7/webapps /var/lib/tomcat7/shell
  chmod g+s /var/lib/tomcat7/common /var/lib/tomcat7/webapps /var/lib/tomcat7/shell
  echo "All file ownership and permissions have been transferred to the tomcat7 user"
  echo ""
  
}

function shell_setup {

  echo "cd /var/lib/tomcat7/shell" > /usr/bin/fusionshell
  echo "java -jar shell.jar" >> /usr/bin/fusionshell
  chmod 755 /usr/bin/fusionshell
 
  verified='n'
  until [ $verified == 'y' ] || [ $verified == 'Y' ]; do
    read -p "What is your Ubuntu username (will add tomcat7 group to this user): " ubuntuuser
    read -p "Is [$ubuntuuser] correct? (y/n) " verified
  done
  usermod -a -G tomcat7 $ubuntuuser

  echo ""
  echo "Shell is set up and can be accessed using the 'fusionshell' command."
  echo "NB! The group change will not take effect before next login with "
  echo "your ubuntu user. Running the shell before that can cause some error"
  echo "messages."
  echo ""
}

function cleanup {
  rm -rf tables.zip
  rm .tmp
  rm -rf tables/
  rm install2013R1.sql
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

read -p "Do you run this script with sudo (root) permission? (y/n) " yn
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
check_tomcat_installation
prepare_tomcat
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







