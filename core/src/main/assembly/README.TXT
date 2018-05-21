Create freeacs user if not exists

    sudo useradd freeacs

Create new directory to hold service and copy jar file and config

    sudo mkdir -p /var/freeacs/core
    sudo cp freeacs-core.jar /var/freeacs/core
    sudo cp freeacs-core.conf /var/freeacs/core
    sudp cp application-config.properties /var/freeacs/core
    sudo chown freeacs:freeacs -R /var/freeacs/core

For systemd (suggested)

    sudo cp freeacs-core.service /etc/systemd/system
    sudo systemctl enable freeacs-core
    sudo systemctl restart freeacs-core
    journalctl -f -u freeacs-core

For systemv (aka init.d, not tested)

    sudo ln -s /var/freeacs/core/freeacs-core.jar /etc/init.d/freeacs-core
    sudo update-rc.d freeacs-core defaults
    sudo service freeacs-core restart

tail -f /var/freeacs/core/log/core.log

To stop the service:
    sudo systemctl stop freeacs-core
    or
    sudo service freeacs-core stop