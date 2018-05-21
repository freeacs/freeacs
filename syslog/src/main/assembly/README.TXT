Create freeacs user if not exists

    sudo useradd freeacs

Create new directory to hold service and copy jar file and config

    sudo mkdir -p /var/freeacs/syslog
    sudo cp freeacs-syslog.jar /var/freeacs/syslog
    sudo cp freeacs-syslog.conf /var/freeacs/syslog
    sudp cp application-config.properties /var/freeacs/syslog
    sudo chown freeacs:freeacs -R /var/freeacs/syslog

For systemd (suggested)

    sudo cp freeacs-syslog.service /etc/systemd/system
    sudo systemctl enable freeacs-syslog
    sudo systemctl restart freeacs-syslog
    journalctl -f -u freeacs-syslog

For systemv (aka init.d, not tested)

    sudo ln -s /var/freeacs/syslog/freeacs-syslog.jar /etc/init.d/freeacs-syslog
    sudo update-rc.d freeacs-syslog defaults
    sudo service freeacs-syslog restart

tail -f /var/freeacs/syslog/log/syslog.log

To stop the service:
    sudo systemctl stop freeacs-syslog
    or
    sudo service freeacs-syslog stop