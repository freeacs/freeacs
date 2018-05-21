Create freeacs user if not exists

    sudo useradd freeacs

Create new directory to hold service and copy jar file and config

    sudo mkdir -p /var/freeacs/web
    sudo cp freeacs-web.jar /var/freeacs/web
    sudo cp freeacs-web.conf /var/freeacs/web
    sudp cp application-config.properties /var/freeacs/web
    sudo chown freeacs:freeacs -R /var/freeacs/web

For systemd (suggested)

    sudo cp freeacs-web.service /etc/systemd/system
    sudo systemctl enable freeacs-web
    sudo systemctl restart freeacs-web
    journalctl -f -u freeacs-web

For systemv (aka init.d, not tested)

    sudo ln -s /var/freeacs/web/freeacs-web.jar /etc/init.d/freeacs-web
    sudo update-rc.d freeacs-web defaults
    sudo service freeacs-web restart

tail -f /var/freeacs/web/log/web.log

To stop the service:
    sudo systemctl stop freeacs-web
    or
    sudo service freeacs-web stop