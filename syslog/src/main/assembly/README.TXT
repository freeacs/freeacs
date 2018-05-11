Create freeacs user

    sudo useradd freeacs

Create new directory to hold service and copy jar file

    sudo mkdir -p /var/freeacs/syslog
    sudo cp freeacs-syslog.jar /var/freeacs/syslog
    sudo cp freeacs-syslog.conf /var/freeacs/syslog
    sudo chown freeacs:freeacs -R /var/freeacs/syslog

Copy service file to systemd

    sudo cp freeacs-syslog.service /etc/systemd/system
    sudo systemctl enable freeacs-syslog

Then start the service
    sudo systemctl start freeacs-syslog
    or
    sudo systemctl restart freeacs-syslog

Check logs from system out

    journalctl -f -u freeacs-syslog

Or check logs in /var/freeacs/syslog

To stop the service:
    sudo systemctl stop freeacs-syslog