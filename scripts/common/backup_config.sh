#!/bin/bash

declare -a moduleNames=(core stun syslog shell monitor web webservices tr069)

for module in ${moduleNames[@]} ; do
    cp /opt/freeacs-${module}/config/application-config.conf \
        /opt/freeacs-${module}/config/application-config.conf.$(date +%Y%m%d%H%M) 2>/dev/null || :
done