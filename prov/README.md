FreeACS Fusion - Prov
=====================
This project is a just a part of the whole product. Get the full picture here: 
http://www.freeacs.com/

Dependencies
------------
https://github.com/freeacs/common.git  
I recommend setting up this project as a Project reference

https://github.com/freeacs/dbi.git  
I recommend setting up this project as a Project reference

https://github.com/freeacs/lib.git  
The following jarfiles are necessary on the classpath:
* servlet-api.jar (this jar file will of course be taken from the web container
in a deployment - this file is just here for compilation purposes)
* commons-codec-1.2.jar

Eclipse setup
-------------
Git view: Import git repo  
Git view: Import projects from git repo, import as general project    
Package/Navigator view: Change project facets to Java 1.7  
Package/Navigator view: Project reference to the freeacs-common and dbi project    
Package/Navigator view: Add libs to classpath from freeacs-lib "project"  

Overview
--------
The prov module offers functionality shared by the TR-069 server and the SPP
server (Simple Provisioning Protocol - support HTTP/TFTP/Telnet):

* Database-access
* 
* Cache (BaseCache)
* SessionData-interface (SessionDataI)
* Download-control logick (DownloadLogic)
* Firmware download (FileServlet)
* Job-related logic (JobLogic + JobHistoryEntry)
* Log stuff (Log)
* Periodic Inform Interval calculation (PIIDecision)
* Monitoring (OKServlet)





