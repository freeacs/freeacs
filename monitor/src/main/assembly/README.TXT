Create freeacs user

    sudo useradd freeacs

Create new directory to hold service and copy jar file

    sudo mkdir -p /var/freeacs/monitor
    sudo cp freeacs-monitor.jar /var/freeacs/monitor
    sudo cp freeacs-monitor.conf /var/freeacs/monitor
    sudo chown freeacs:freeacs -R /var/freeacs/monitor

Copy service file to systemd

    sudo cp freeacs-monitor.service /etc/systemd/system
    sudo systemctl enable freeacs-monitor

Check logs from system out

    journalctl -f -u freeacs-monitor

Or check logs in /var/freeacs/monitor