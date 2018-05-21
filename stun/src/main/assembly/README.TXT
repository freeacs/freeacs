Create freeacs user if not exists

    sudo useradd freeacs

Create new directory to hold service and copy jar file and config

    sudo mkdir -p /var/freeacs/stun
    sudo cp freeacs-stun.jar /var/freeacs/stun
    sudo cp freeacs-stun.conf /var/freeacs/stun
    sudp cp application-config.properties /var/freeacs/stun
    sudo chown freeacs:freeacs -R /var/freeacs/stun

For systemd (suggested)

    sudo cp freeacs-stun.service /etc/systemd/system
    sudo systemctl enable freeacs-stun
    sudo systemctl restart freeacs-stun
    journalctl -f -u freeacs-stun

For systemv (aka init.d, not tested)

    sudo ln -s /var/freeacs/stun/freeacs-stun.jar /etc/init.d/freeacs-stun
    sudo update-rc.d freeacs-stun defaults
    sudo service freeacs-stun restart

tail -f /var/freeacs/stun/log/stun.log

To stop the service:
    sudo systemctl stop freeacs-stun
    or
    sudo service freeacs-stun stop