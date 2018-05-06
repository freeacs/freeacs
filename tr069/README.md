FreeACS Fusion - TR-069 Server
==============================
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
This server is an essential part of this product, as it is running the TR-069
communication with the various devices (routers, ATA, RGWs, etc) connected to
it. The server supports TR-069 fairly well supporting all REQUIRED methods for
an ACS (see http://www.broadband-forum.org/technical/download/TR-069_Amendment-5.pdf 
chapter 3.6) with the sole exception of AutonomousTransferComplete. 

The server is responsive and offers a wide range of features. It has been
tested with several hundred thousands of devices (at Telenor). It has also been
tested with a range of devices:

* Eltek R7121  
* Eltek R7921  
* Inteno routers  
* Ping Communication RGW208EN  
* Ping Communication IAD208AN  
* Ping Communication NPA201E  
* Speedtouch 585i  
* Zyxel P2602  

No doubt the server can work with any other TR-069 device, if the device does 
not contain very troublesome bugs (which it very often does!).

The features offered by the server are:

* TR-069 provisioning of configuration and firmware
* TR-111 support (in combination with the STUN server (https://github.com/freeacs/stun.git))
* TR-XXX generic datamodel support (TR-098, TR-104, TR-181, etc.
* Automatic discovery of a new device - create necessary objects in database
* Download limit management
* Quirks handling of device not complying with standard
* Execution of complex provisioning, execution order, time of week, etc
* Spread-of-traffic - avoid troublesome peak loads of devices connecting
* Detailed logging, to file and syslog
* Two test-systems, to enable really complex testing of the devices

Also see the documentation found in the docs-folder



 
  





 

