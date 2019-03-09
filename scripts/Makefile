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
DIST := $(shell ./detect_dist.sh)

.DEFAULT_GOAL := default

default: | check-root-user check-acs-user install-deps install-mysql download-release load-tables
	@echo "Done"

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
	$(eval DOWNLOAD_ARTIFACTS := deb|zip|pdf)
else ifeq (${DIST},Ubuntu)
	$(eval DOWNLOAD_ARTIFACTS := rpm|zip|pdf)
else
	$(error $(DIST))
endif

download-release: | set-artifacts
	make -f Makefile._Common download-release DOWNLOAD_ARTIFACTS=${DOWNLOAD_ARTIFACTS}

load-tables: | check-acs-user
	make -f Makefile._Common load-tables MYSQL_ACS_PASSWORD="${MYSQL_ACS_PASSWORD}"

install-deps:
ifeq ($(DIST),CentOS)
	make -f Makefile.CentOS install-deps
else ifeq ($(DIST),Ubuntu)
	make -f Makefile.Ubuntu install-deps
else
	$(error $(DIST))
endif

install-mysql: | check-root-user check-acs-user
ifeq ($(DIST),CentOS)
	make -f Makefile.CentOS install-mysql \
		MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD}" \
		MYSQL_ACS_PASSWORD="${MYSQL_ACS_PASSWORD}"
else ifeq ($(DIST),Ubuntu)
	make -f Makefile.Ubuntu install-mysql \
		MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD}" \
		MYSQL_ACS_PASSWORD="${MYSQL_ACS_PASSWORD}"
else
	$(error $(DIST))
endif
