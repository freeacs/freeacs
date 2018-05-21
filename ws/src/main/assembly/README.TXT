Create freeacs user if not exists

    sudo useradd freeacs

Create new directory to hold service and copy jar file and config

    sudo mkdir -p /var/freeacs/ws
    sudo cp freeacs-ws.jar /var/freeacs/ws
    sudo cp freeacs-ws.conf /var/freeacs/ws
    sudp cp application-config.properties /var/freeacs/ws
    sudo chown freeacs:freeacs -R /var/freeacs/ws

For systemd (suggested)

    sudo cp freeacs-ws.service /etc/systemd/system
    sudo systemctl enable freeacs-ws
    sudo systemctl restart freeacs-ws
    journalctl -f -u freeacs-ws

For systemv (aka init.d, not tested)

    sudo ln -s /var/freeacs/ws/freeacs-ws.jar /etc/init.d/freeacs-ws
    sudo update-rc.d freeacs-ws defaults
    sudo service freeacs-ws restart

tail -f /var/freeacs/ws/log/ws.log

To stop the service:
    sudo systemctl stop freeacs-ws
    or
    sudo service freeacs-ws stop