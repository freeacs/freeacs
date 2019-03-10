###########################
#                         #
#     THIS MAKEFILE       #
#           IS            #
#     WORK IN PROGRESS    #
#                         #
###########################

INSTALLATION_PATH := /opt/freeacs
VERSION_NUMBER := latest
DOWNLOAD_ARTIFACTS :=
MYSQL_ROOT_PASSWORD :=
MYSQL_ACS_PASSWORD :=
DIST := $(shell ./common/detect_dist.sh)
SHELL := /bin/bash

.DEFAULT_GOAL := default

default: | confirm \
		check-root-user \
		check-acs-user \
		install-deps \
		install-mysql \
		download-release \
		load-tables \
		install-binaries \
		update-property-files
	@echo "Script complete"

update-property-files:
	@echo "MySQL ROOT PW: $MYSQL_ROOT_PASSWORD"
	@echo "MySQL ACS  PW: $MYSQL_ACS_PASSWORD"
	@echo "Updating property files in all modules... "
	sed -i -e '/main.datasource.password=/ s/=acs/='\""$MYSQL_ACS_PASSWORD"\"'/' \
		/opt/freeacs-web/config/application-config.conf
	sed -i -e '/main.datasource.password=/ s/=acs/='\""$MYSQL_ACS_PASSWORD"\"'/' \
		/opt/freeacs-webservice/config/application-config.properties
	sed -i -e '/main.datasource.password=/ s/=acs/='\""$MYSQL_ACS_PASSWORD"\"'/' \
		/opt/freeacs-shell/config/application-config.properties
	sed -i -e '/main.datasource.password=/ s/=acs/='\""$MYSQL_ACS_PASSWORD"\"'/' \
		/opt/freeacs-tr069/config/application-config.conf
	sed -i -e '/main.datasource.password=/ s/=acs/='\""$MYSQL_ACS_PASSWORD"\"'/' \
		/opt/freeacs-core/config/application-config.conf
	sed -i -e '/main.datasource.password=/ s/=acs/='\""$MYSQL_ACS_PASSWORD"\"'/' \
		/opt/freeacs-stun/config/application-config.conf
	sed -i -e '/main.datasource.password=/ s/=acs/='\""$MYSQL_ACS_PASSWORD"\"'/' \
		/opt/freeacs-syslog/config/application-config.conf
	@echo "Done"

confirm:
	@echo "WARNING!!!"
	@echo "This script will completely reinstall MySQL and FreeACS, if already installed."
	@echo "You also need to run this script as root, or else it will just fail."
	@echo "If you need to update FreeACS, then run 'make update-freeacs', and it will only update binaries"
	@echo "Continue? [y/N] "
	@read line; if [ "$$line" != "y" ]; then echo aborting; exit 1 ; fi
	@echo "Are you sure? [y/N] "
	@read line; if [ "$$line" != "y" ]; then echo aborting; exit 1 ; fi

check-acs-user:
ifndef MYSQL_ACS_PASSWORD
	$(error MYSQL_ACS_PASSWORD is undefined)
else
	@echo ' -> Using mysql acs password ${MYSQL_ACS_PASSWORD}'
endif

check-root-user:
ifndef MYSQL_ROOT_PASSWORD
	$(error MYSQL_ROOT_PASSWORD is undefined)
else
	@echo ' -> Using mysql root password ${MYSQL_ROOT_PASSWORD}'
endif

set-artifacts:
ifeq (${DIST},Ubuntu)
	$(eval DOWNLOAD_ARTIFACTS := deb|zip|pdf)
else ifeq (${DIST},CentOS)
	$(eval DOWNLOAD_ARTIFACTS := rpm|zip|pdf)
else
	$(error $(DIST))
endif

download-release: | set-artifacts
	$(MAKE) -f Makefile._Common download-release DOWNLOAD_ARTIFACTS="${DOWNLOAD_ARTIFACTS}"

load-tables: | check-acs-user
	$(MAKE) -f Makefile._Common load-tables MYSQL_ACS_PASSWORD="${MYSQL_ACS_PASSWORD}"

install-deps:
ifeq ($(DIST),CentOS)
	$(MAKE) -f Makefile.CentOS install-deps
else ifeq ($(DIST),Ubuntu)
	$(MAKE) -f Makefile.Ubuntu install-deps
else
	$(error $(DIST))
endif

install-mysql: | check-root-user check-acs-user
ifeq ($(DIST),CentOS)
	$(MAKE) -f Makefile.CentOS install-mysql \
		MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD}" \
		MYSQL_ACS_PASSWORD="${MYSQL_ACS_PASSWORD}"
else ifeq ($(DIST),Ubuntu)
	$(MAKE) -f Makefile.Ubuntu install-mysql \
		MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD}" \
		MYSQL_ACS_PASSWORD="${MYSQL_ACS_PASSWORD}"
else
	$(error $(DIST))
endif

install-binaries:
	@echo "TODO: install binaries"

update-binaries:
	@echo "TODO: update binaries"