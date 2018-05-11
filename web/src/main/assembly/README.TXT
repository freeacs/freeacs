Create freeacs user

    sudo useradd freeacs

Create new directory to hold service and copy jar file

    sudo mkdir -p /var/freeacs/web
    sudo cp freeacs-web.jar /var/freeacs/web
    sudo cp freeacs-web.conf /var/freeacs/web
    sudo chown freeacs:freeacs -R /var/freeacs/web

Copy service file to systemd

    sudo cp freeacs-web.service /etc/systemd/system
    sudo systemctl enable freeacs-web

Check logs from system out

    journalctl -f -u freeacs-web

Or check logs in /var/freeacs/web