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
	elif [ -f /etc/debian_version ] ; then
		DIST="Debian"
	fi
fi

echo ${DIST}
