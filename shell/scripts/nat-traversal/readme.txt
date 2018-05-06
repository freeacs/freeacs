Overview

This folder includes 3 scripts. The master-script (calling the other scripts) is opentelnet_alldevices.xss. 
This script was made to test the new concepts of if/else and while/do, plus some other stuff. Thus they 
serve as an example of the possibilities that exist with Fusion Shell.

Purpose

The purpose of these scripts is to set up a "channel" going through a gateway (sitting on public IP) through
to the device sitting behind this gateway (on private IP). When this is done, the idea is to be able to run
Telnet-scripts on the "private devices".

Details

To be able to open a channel from gateway/public device to local/private device, we need to establish if there
is a relation between two devices. This is the first obstacle, and we go through all the Unit Types in our
system and search for devices that are connected. When we find this relation, we then change the configuration
of the gateway so that a NAT port forwarding rule is specified, pointing at the Telnet-port on the local device.
We also take extra care to make sure this rule is not already specified, so the scripts must go through
all the rules that is already made, to see if there's a match or not.

All in all, this is fairly complex, but a careful study of the scripts and the comments will hopefully guide
you on the way.