Create freeacs user if not exists

    sudo useradd freeacs

Create new directory to hold service and copy jar file and config

    sudo mkdir -p /var/freeacs/monitor
    sudo cp freeacs-monitor.jar /var/freeacs/monitor
    sudo cp freeacs-monitor.conf /var/freeacs/monitor
    sudo chown freeacs:freeacs -R /var/freeacs/monitor

For systemd (suggested)

    sudo cp freeacs-monitor.service /etc/systemd/system
    sudo systemctl enable freeacs-monitor
    sudo systemctl restart freeacs-monitor
    journalctl -f -u freeacs-monitor

For systemv (aka init.d, not tested)

    sudo ln -s /var/freeacs/monitor/freeacs-monitor.jar /etc/init.d/freeacs-monitor
    sudo update-rc.d freeacs-monitor defaults
    sudo service freeacs-monitor restart

tail -f /var/freeacs/monitor/log/monitor.log

To stop the service:
    sudo systemctl stop freeacs-monitor
    or
    sudo service freeacs-monitor stop