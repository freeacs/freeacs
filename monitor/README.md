FreeACS Fusion - Monitor Server
===============================
This project is a just a part of the whole product. Get the full picture here: 
http://www.freeacs.com/

Dependencies
------------
https://github.com/freeacs/common.git  
I recommend referencing this project as a Deployment Assembly Project

https://github.com/freeacs/dbi.git  
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
Package/Navigator view: Deployment Assembly project reference to the freeacs-common and dbi project    
Package/Navigator view: Specify Library->Server runtime on classpath - ex: use an installed Tomcat 7 server  


Overview
--------
Monitor server is responsible for monitoring all the other servers of this 
product (TR-069 server, Web server, STUN server, etc). It also has the 
responsibility of sending emails if some "situation" has occurred. A quick
summary:

* Monitoring all other servers of this product
* Providing a web interface to see the state + version of all servers
* Providing one URL to use in an external monitoring system (ex: HP OpenView)
* Sending a heartbeat message every morning at 7 am to indicate that the product is running
* Sending trigger notifications

This server is very lightweight/simple, and it uses the Scheduler-system of
the Common-project a lot. 
