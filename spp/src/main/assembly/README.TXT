Create freeacs user

    sudo useradd freeacs

Create new directory to hold service and copy jar file

    sudo mkdir -p /var/freeacs/spp
    sudo cp freeacs-spp.jar /var/freeacs/spp
    sudo cp freeacs-spp.conf /var/freeacs/spp
    sudo chown freeacs:freeacs -R /var/freeacs/spp

Copy service file to systemd

    sudo cp freeacs-spp.service /etc/systemd/system
    sudo systemctl enable freeacs-spp

Then start the service
    sudo systemctl start freeacs-spp
    or
    sudo systemctl restart freeacs-spp

Check logs from system out

    journalctl -f -u freeacs-spp

Or check logs in /var/freeacs/spp

To stop the service:
    sudo systemctl stop freeacs-spp