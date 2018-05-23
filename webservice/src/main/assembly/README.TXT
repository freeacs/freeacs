Create freeacs user if not exists

    sudo useradd freeacs

Create new directory to hold service and copy jar file and config

    sudo mkdir -p /var/freeacs/webservice
    sudo cp freeacs-webservice.jar /var/freeacs/webservice
    sudo cp freeacs-webservice.conf /var/freeacs/webservice
    sudp cp application-config.properties /var/freeacs/webservice
    sudo chown freeacs:freeacs -R /var/freeacs/webservice

For systemd (suggested)

    sudo cp freeacs-webservice.service /etc/systemd/system
    sudo systemctl enable freeacs-webservice
    sudo systemctl restart freeacs-webservice
    journalctl -f -u freeacs-webservice

For systemv (aka init.d, not tested)

    sudo ln -s /var/freeacs/webservice/freeacs-webservice.jar /etc/init.d/freeacs-webservice
    sudo update-rc.d freeacs-webservice defaults
    sudo service freeacs-webservice restart

tail -f /var/freeacs/webservice/log/webservice.log

To stop the service:
    sudo systemctl stop freeacs-webservice
    or
    sudo service freeacs-webservice stop