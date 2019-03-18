#!/bin/bash
# Detects distribution of linux

OS=`uname -s`
DIST="Unknown"

if [[ "${OS}" = "Linux" ]] ; then
	if [[ -f /etc/centos-release ]] ; then
		DIST='CentOS'
	elif [[ -f /etc/debian_version ]] ; then
		DIST=`lsb_release -is`
		if [[ ${DIST} != "Ubuntu" ]] && [[ ${DIST} != "LinuxMint" ]]; then
			DIST="Unsupported debian distro: $DIST"
		else
			DIST='Ubuntu'
			CURRENT_VERSION=`lsb_release -rs`
			REQUIRED_VERSION='16.04'
			if [[ "$(printf '%s\n' "$REQUIRED_VERSION" "$CURRENT_VERSION" | sort -V | head -n1)" != "$REQUIRED_VERSION" ]];
			then
				DIST="Your Ubuntu version is not supported, minimum is ${REQUIRED_VERSION}"
 			fi
		fi
	fi
fi

echo ${DIST}
