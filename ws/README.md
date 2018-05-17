FreeACS Fusion - Web Services Server
====================================
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
Eclipse will kick on an issue regarding "access restriction" - I have circumvented this by disable those checks in Eclipse!


Overview
--------
The Web Service server (see docs) allows other servers to connect to this 
product.