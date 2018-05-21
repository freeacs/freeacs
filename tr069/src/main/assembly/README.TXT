Create freeacs user if not exists

    sudo useradd freeacs

Create new directory to hold service and copy jar file and config

    sudo mkdir -p /var/freeacs/tr069
    sudo cp freeacs-tr069.jar /var/freeacs/tr069
    sudo cp freeacs-tr069.conf /var/freeacs/tr069
    sudp cp application-config.properties /var/freeacs/tr069
    sudo chown freeacs:freeacs -R /var/freeacs/tr069

For systemd (suggested)

    sudo cp freeacs-tr069.service /etc/systemd/system
    sudo systemctl enable freeacs-tr069
    sudo systemctl restart freeacs-tr069
    journalctl -f -u freeacs-tr069

For systemv (aka init.d, not tested)

    sudo ln -s /var/freeacs/tr069/freeacs-tr069.jar /etc/init.d/freeacs-tr069
    sudo update-rc.d freeacs-tr069 defaults
    sudo service freeacs-tr069 restart

tail -f /var/freeacs/tr069/log/tr069.log

To stop the service:
    sudo systemctl stop freeacs-tr069
    or
    sudo service freeacs-tr069 stop