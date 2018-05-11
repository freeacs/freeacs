Create freeacs user

    sudo useradd freeacs

Create new directory to hold service and copy jar file

    sudo mkdir -p /var/freeacs/stun
    sudo cp freeacs-stun.jar /var/freeacs/stun
    sudo cp freeacs-stun.conf /var/freeacs/stun
    sudo chown freeacs:freeacs -R /var/freeacs/stun

Copy service file to systemd

    sudo cp freeacs-stun.service /etc/systemd/system
    sudo systemctl enable freeacs-stun

Then start the service
    sudo systemctl start freeacs-stun
    or
    sudo systemctl restart freeacs-stun

Check logs from system out

    journalctl -f -u freeacs-stun

Or check logs in /var/freeacs/stun

To stop the service:
    sudo systemctl stop freeacs-stun