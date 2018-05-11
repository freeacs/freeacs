Create freeacs user

    sudo useradd freeacs

Create new directory to hold service and copy jar file

    sudo mkdir -p /var/freeacs/core
    sudo cp freeacs-core.jar /var/freeacs/core
    sudo cp freeacs-core.conf /var/freeacs/core
    sudo chown freeacs:freeacs -R /var/freeacs/core

Copy service file to systemd

    sudo cp freeacs-core.service /etc/systemd/system
    sudo systemctl enable freeacs-core

Then start the service
    sudo systemctl start freeacs-core
    or
    sudo systemctl restart freeacs-core

Check logs from system out

    journalctl -f -u freeacs-core

Or check logs in /var/freeacs/core

To stop the service:
    sudo systemctl stop freeacs-core