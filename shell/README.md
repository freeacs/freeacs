FreeACS Fusion - Shell (Command line interface)
===============================================
This project is a just a part of the whole product. Get the full picture here: 
http://www.freeacs.com/

Dependencies
------------
https://github.com/freeacs/common.git  
I recommend setting up this project as a Project reference

https://github.com/freeacs/dbi.git  
I recommend setting up this project as a Project reference

Jarfiles needed to make runnable JAR file and run the project is part of the 
project (lib-folder), but these may of course be exchanged for newer 
versions (if necessary) upon making the JAR-file. 

Eclipse setup
-------------
Git view: Import git repo  
Git view: Import projects from git repo, import as general project    
Package/Navigator view: Change project facets to Java 1.7  
Package/Navigator view: Java Build Path:  project reference to the freeacs-common and dbi project      
Package/Navigator view: Add libs to classpath from lib-folder 

Overview
--------
The Shell is a command line interface to the product. It enables a user to
read and change all kinds of data inside the database in a safe way. The number
of commands offered a close to 100, so it can be a lot to start with. The 
basic idea is that you can always type "help" to get help - anywhere in the 
system. The help is context-sensitive. 

The main purposes of the shell is this:

* Provide an interface to the system (as an alternative to the Web interface)
* Offer scripting capabilities
* Migration of old versions to new versions of the product
* Export/import data of the database
* Cleanup and large-scale jobs
* Interface to a pretty advanced test system for TR-069 devices
* Run in restricted mode - can be setup to open directly on a SSH-connection

The Shell offers piping and variables, if/else, while/done, and some
other constructs to really make complex scripts. The scripts can also call
upon other scripts.

All in all, this script language is a rather simple subset of bash-like
script language. It was really never intended to be this advanced, but somehow
it grew into it for various reasons. 

The scripts-folder holds a number of scripts made over the past years, you
may find tip and tricks there. Also the docs-folder contains User Manuals
for the whole project.





