Create freeacs user

    sudo useradd freeacs

Create new directory to hold service and copy jar file

    sudo mkdir -p /var/freeacs/tr069
    sudo cp freeacs-tr069.jar /var/freeacs/tr069
    sudo cp freeacs-tr069.conf /var/freeacs/tr069
    sudo chown freeacs:freeacs /var/freeacs/tr069

Copy service file to systemd

    sudo cp freeacs-tr069.service /etc/systemd/system
    sudo systemctl enable freeacs-tr069

Check logs from system out

    journalctl -f -u freeacs-tr069

Or check logs in /var/freeacs/tr069