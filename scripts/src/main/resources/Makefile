###########################
#                         #
#     FREEACS MAKEFILE    #
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
		load-tables \
		install-binaries \
		update-property-files \
		install-nginx
	@echo "Script complete"

update-property-files: | check-acs-user
	@echo "Updating property files in all modules... "
	@sed -i -e '/main.datasource.password=/ s/=acs/='\""${MYSQL_ACS_PASSWORD}"\"'/' /opt/freeacs-web/config/application-config.conf
	@sed -i -e '/main.datasource.password=/ s/=acs/='"${MYSQL_ACS_PASSWORD}"'/' /opt/freeacs-webservice/config/application-config.properties
	@sed -i -e '/main.datasource.password=/ s/=acs/='"${MYSQL_ACS_PASSWORD}"'/' /opt/freeacs-shell/config/application-config.properties
	@sed -i -e '/main.datasource.password=/ s/=acs/='\""${MYSQL_ACS_PASSWORD}"\"'/' /opt/freeacs-tr069/config/application-config.conf
	@sed -i -e '/main.datasource.password=/ s/=acs/='\""${MYSQL_ACS_PASSWORD}"\"'/' /opt/freeacs-core/config/application-config.conf
	@sed -i -e '/main.datasource.password=/ s/=acs/='\""${MYSQL_ACS_PASSWORD}"\"'/' /opt/freeacs-stun/config/application-config.conf
	@sed -i -e '/main.datasource.password=/ s/=acs/='\""${MYSQL_ACS_PASSWORD}"\"'/' /opt/freeacs-syslog/config/application-config.conf
	@systemctl restart freeacs-*
	@echo "Done"

confirm:
	@echo "WARNING:"
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
endif

check-root-user:
ifndef MYSQL_ROOT_PASSWORD
	$(error MYSQL_ROOT_PASSWORD is undefined)
endif

set-artifacts:
ifeq (${DIST},Ubuntu)
	$(eval DOWNLOAD_ARTIFACTS := deb)
else ifeq (${DIST},CentOS)
	$(eval DOWNLOAD_ARTIFACTS := rpm)
else
	$(error $(DIST))
endif

download-tables:
	@$(MAKE) -f Makefile._Common download-release DOWNLOAD_ARTIFACTS="tables.zip"

download-binaries: | set-artifacts
	@$(MAKE) -f Makefile._Common download-release \
		DOWNLOAD_ARTIFACTS="${DOWNLOAD_ARTIFACTS}" \
		VERSION_NUMBER="${VERSION_NUMBER}"

load-tables: | check-acs-user download-tables
	@unzip "*.zip";
	@$(MAKE) -f Makefile._Common load-tables MYSQL_ACS_PASSWORD="${MYSQL_ACS_PASSWORD}"

install-deps:
ifeq ($(DIST),CentOS)
	@$(MAKE) -f Makefile.CentOS install-deps
else ifeq ($(DIST),Ubuntu)
	@$(MAKE) -f Makefile.Ubuntu install-deps
else
	$(error $(DIST))
endif

install-mysql: | check-root-user check-acs-user
ifeq ($(DIST),CentOS)
	@$(MAKE) -f Makefile.CentOS install-mysql \
		MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD}" \
		MYSQL_ACS_PASSWORD="${MYSQL_ACS_PASSWORD}"
else ifeq ($(DIST),Ubuntu)
	@$(MAKE) -f Makefile.Ubuntu install-mysql \
		MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD}" \
		MYSQL_ACS_PASSWORD="${MYSQL_ACS_PASSWORD}"
else
	$(error $(DIST))
endif

uninstall-binaries:
ifeq ($(DIST),CentOS)
	@$(MAKE) -f Makefile.CentOS uninstall-binaries \
		INSTALLATION_PATH="${INSTALLATION_PATH}"
else ifeq ($(DIST),Ubuntu)
	@$(MAKE) -f Makefile.Ubuntu uninstall-binaries \
		INSTALLATION_PATH="${INSTALLATION_PATH}"
else
	$(error $(DIST))
endif
	@rm -rf /opt/freeacs-*

install-binaries: | uninstall-binaries download-binaries
ifeq ($(DIST),CentOS)
	@$(MAKE) -f Makefile.CentOS install-binaries \
		INSTALLATION_PATH="${INSTALLATION_PATH}"
else ifeq ($(DIST),Ubuntu)
	@$(MAKE) -f Makefile.Ubuntu install-binaries \
		INSTALLATION_PATH="${INSTALLATION_PATH}"
else
	$(error $(DIST))
endif

install-nginx:
ifeq ($(DIST),CentOS)
	@(./common/install_nginx_centos.sh)
else ifeq ($(DIST),Ubuntu)
	@(./common/install_nginx_ubuntu.sh)
else
	$(error $(DIST))
endif

confirm-update:
	@echo "WARNING:"
	@echo "This script will update all modules."
	@echo "You need to run this script as root, or else it will just fail."
	@echo "This script will take backup of the property files of each module."
	@echo "Continue? [y/N] "
	@read line; if [ "$$line" != "y" ]; then echo aborting; exit 1 ; fi
	@echo "Are you sure? [y/N] "
	@read line; if [ "$$line" != "y" ]; then echo aborting; exit 1 ; fi

update-binaries: | confirm-update install-deps download-binaries
	@echo "Dont mind the warnings below, this script is trying to backup two types of property files."
	@echo "Only one type of property file exists in each module"
ifeq ($(DIST),CentOS)
	$(shell ./common/backup_config.sh)
	@$(MAKE) -f Makefile.CentOS install-binaries \
		INSTALLATION_PATH="${INSTALLATION_PATH}"
else ifeq ($(DIST),Ubuntu)
	$(shell ./common/backup_config.sh)
	@$(MAKE) -f Makefile.Ubuntu install-binaries \
		INSTALLATION_PATH="${INSTALLATION_PATH}"
else
	$(error $(DIST))
endif