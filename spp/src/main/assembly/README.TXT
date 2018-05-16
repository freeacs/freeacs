Create freeacs user if not exists

    sudo useradd freeacs

Create new directory to hold service and copy jar file and config

    sudo mkdir -p /var/freeacs/spp
    sudo cp freeacs-spp.jar /var/freeacs/spp
    sudo cp freeacs-spp.conf /var/freeacs/spp
    sudo chown freeacs:freeacs -R /var/freeacs/spp

For systemd (suggested)

    sudo cp freeacs-spp.service /etc/systemd/system
    sudo systemctl enable freeacs-spp
    sudo systemctl restart freeacs-spp
    journalctl -f -u freeacs-spp

For systemv (aka init.d, not tested)

    sudo ln -s /var/freeacs/spp/freeacs-spp.jar /etc/init.d/freeacs-spp
    sudo update-rc.d freeacs-spp defaults
    sudo service freeacs-spp restart

tail -f /var/freeacs/spp/log/spp.log

To stop the service:
    sudo systemctl stop freeacs-spp
    or
    sudo service freeacs-spp stop