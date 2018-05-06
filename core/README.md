FreeACS Fusion - Core Server
============================
This project is a just a part of the whole product. Get the full picture here: 
http://www.freeacs.com/

Dependencies
------------
https://github.com/freeacs/common.git  
I recommend referencing this project as a Deployment Assembly Project

https://github.com/freeacs/dbi.git  
I recommend referencing this project as a Deployment Assembly Project

https://github.com/freeacs/shell.git  
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
Package/Navigator view: Deployment Assembly project reference to the freeacs-common, dbi and shell project    
Package/Navigator view: Specify Library->Server runtime on classpath - ex: use an installed Tomcat 7 server  

Overview
--------
The Core Server could just as well be named "Background Server" because it does
just that kind of stuff. Very simply put it does cleanup, make reports and some other
background processing. It has a vital importance is some tasks (Trigger
processing, Job processing). One should not run more than one instance of
this server, that would just make for trouble. The functions it performs are:

* Delete old jobs
* Delete old scripts
* Delete old syslog
* Detect missing heartbeat from devices (check syslog data)
* Enforce job rules (stop jobs if necessary)
* Generate reports
* Execute scripts (that's why we have the Shell-dependency)
* Release triggers

See docs-folder for more information
