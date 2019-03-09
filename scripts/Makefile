###########################
#                         #
#     THIS MAKEFILE       #
#           IS            #
#     WORK IN PROGRESS    #
#                         #
###########################

INSTALLATION_PATH := /opt/freeacs
VERSION_NUMBER := latest
DOWNLOAD_ARTIFACTS := rpm|zip
MYSQL_ROOT_PASSWORD :=
MYSQL_ACS_PASSWORD :=
DIST := $(shell ./detect_dist.sh)

check_env:
ifndef MYSQL_ROOT_PASSWORD
	$(error MYSQL_ROOT_PASSWORD is undefined)
endif
ifndef MYSQL_ACS_PASSWORD
	$(error MYSQL_ACS_PASSWORD is undefined)
endif

cleanup:
	@echo " -> cleaning up";
	rm -rf files.txt .tmp *.rpm *.zip *.deb *.pdf *.sql;
	@echo " -> done cleaning";

download_release: | cleanup
	@echo " -> downloading FreeACS resources"
	@echo "${DOWNLOAD_ARTIFACTS}"
	curl -s https://api.github.com/repos/freeacs/freeacs/releases/${VERSION_NUMBER} | \
		jq -r ".assets[] | select(.name | test(\"${DOWNLOAD_ARTIFACTS}\")) | .browser_download_url" \
		> files.txt;
	awk '{print $0;}' files.txt | xargs -L1 wget;
	unzip "*.zip";
	@echo " -> all necessary FreeACS resources are available.";

load_tables:
	@echo "Loads all FreeACS table defintions into MySQL"
	mysql -uacs -p${MYSQL_ACS_PASSWORD} acs < install.sql 2> .tmp
	$(eval INSTALLTABLESOK := $(wc -l .tmp | cut -b1-1))
	if [ "${INSTALLTABLESOK}" != '1' ] ; then
		@echo "The output from the installation of the tables indicate some"
		@echo "errors occurred:"
		@echo "------------------------------------------------"
		cat .tmp
		@echo "------------------------------------------------"
		exit
	else
		@echo "Loading of all FreeACS tables was OK"
	fi

install_mysql: | check_env download_release
ifeq ($(DIST),CentOS)
	make -f Makefile.CentOS install_mysql
else ifeq ($(DIST),Ubuntu)
	make -f Makefile.Ubuntu install_mysql
else
	echo "$(DIST)"
endif
