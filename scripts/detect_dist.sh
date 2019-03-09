#!/bin/sh
# Detects distribution of linux

OS=`uname -s`
DIST="Unknown"

if [ "${OS}" = "Linux" ] ; then
	KERNEL=`uname -r`
	if [ -f /etc/centos-release ] ; then
		DIST='CentOS'
	elif [ -f /etc/redhat-release ] ; then
		DIST='RedHat'
	elif [ -f /etc/SuSE-release ] ; then
		DIST=`cat /etc/SuSE-release | tr "\n" ' '| sed s/VERSION.*//`
	elif [ -f /etc/debian_version ] ; then
		DIST="Debian `cat /etc/debian_version`"
	fi
	if [ -f /etc/UnitedLinux-release ] ; then
		DIST="${DIST}[`cat /etc/UnitedLinux-release | tr "\n" ' ' | sed s/VERSION.*//`]"
	fi
fi

echo ${DIST}
