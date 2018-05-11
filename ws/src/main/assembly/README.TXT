Create freeacs user

    sudo useradd freeacs

Create new directory to hold service and copy jar file

    sudo mkdir -p /var/freeacs/ws
    sudo cp freeacs-ws.jar /var/freeacs/ws
    sudo cp freeacs-ws.conf /var/freeacs/ws
    sudo chown freeacs:freeacs -R /var/freeacs/ws

Copy service file to systemd

    sudo cp freeacs-ws.service /etc/systemd/system
    sudo systemctl enable freeacs-ws

Then start the service
    sudo systemctl start freeacs-ws
    or
    sudo systemctl restart freeacs-ws

Check logs from system out

    journalctl -f -u freeacs-ws

Or check logs in /var/freeacs/ws

To stop the service:
    sudo systemctl stop freeacs-ws