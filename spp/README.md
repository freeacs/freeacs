FreeACS Fusion - Simple Provisioning Protocol Server
====================================================
This project is a just a part of the whole product. Get the full picture here: 
http://www.freeacs.com/

Dependencies
------------
https://github.com/freeacs/common.git  
I recommend referencing this project as a Deployment Assembly Project

https://github.com/freeacs/dbi.git  
I recommend referencing this project as a Deployment Assembly Project

https://github.com/freeacs/prov.git  
I recommend referencing this project as a Deployment Assembly Project

Tomcat7 (or equivalent web container) must/should be installed

Jarfiles needed to make WAR file and run the project is part of the project
(WebContent/WEB-INF/lib), but these may of course be exchanged for newer 
versions (if necessary) upon making a WAR file. 


Eclipse setup
-------------
Git view: Import git repo  
Git view: Import projects from git repo, import as general project    
Package/Navigator view: Change project facets to Java 1.7 and Dynamic Web Module 3.0      
Package/Navigator view: Deployment Assembly project reference to the freeacs-common, dbi and prov project      
Package/Navigator view: Specify Library->Server runtime on classpath - ex: use an installed Tomcat 7 server  

Overview
--------
Simple Provisioning Protocol Server can handle HTTP, TFTP and Telnet protocols. 
For HTTP and TFTP it is assumed that one will use the server to transmit
files to the device (the device still is the one to initiate contact to the 
server - the server respond with a file). This file will be different for 
each device, since there is not universal provisioning standard for this
(that's the reason why TR-069 came along). For this reason, one has to 
implement an implementation of com.owera.xaps.spp.response.ProvisioningResponse
interface. There has been implemented an SPA-class, supporting SPA2102 and 
probably others of the same type of devices (from Linksys).

Telnet is used to connect *to* the device directly. It will of course require
access through firewall and NATs, so for some devices this is not an option.
The point is that the Telnet-server can execute telnet-scripts (which you will
make and run from Web/Shell interface). 

One big idea of the SPP server, was that it should (as much as possible) support
the same concepts that the TR-069 server supported, without any changes to 
the Web/Shell interface. The transport protocol should be "invisible". Of course,
this is not that easy, and some things are not really possible. 

All in all, this server has never gotten much attention - never been really
used in production.

However, since the data transported over these protocols are not standardised, 
the server must be adapted to trans
