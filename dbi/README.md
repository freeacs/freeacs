FreeACS Fusion - DBI
====================
This project is a just a part of the whole product. Get the full picture here: 
http://www.freeacs.com/

Dependencies
------------
https://github.com/freeacs/common.git  
I recommend setting up this project as a Project reference

https://github.com/freeacs/lib.git  
The following jarfiles are necessary on the classpath:
* jcommon-1.0.21.jar
* jfreechar-1.0.17.jar
* mysql-connector-java-5.1.28-bin.jar

Eclipse setup
-------------
Git view: Import git repo  
Git view: Import projects from git repo, import as general project    
Package/Navigator view: Change project facets to Java 1.7  
Package/Navigator view: Project reference to the freeacs-common project    
Package/Navigator view: Add libs to classpath from freeacs-lib "project"   

Overview
--------
DBI is short for Database Interface, and provides access to the database from
all the various servers and modules of this product. Here is a short summary
of what this project offers (Classname-references in parenthesis)

* Interface to the database - all SQL should be found here! (with some
exceptions of course - there's always some exceptions)
* A object model for all important/common concepts of this product
* Caching of data that doesn't change very often (XAPS)
* Messaging system between servers (DBI, Inbox, Message)
* Permission/Authorisation handling (Users, Permissions)
* Certificate handling (probably not useful anymore - since going open-source)
* Report-generation (report-package)
* TR069-testing (tr069-package + xml-files)
* Syslog-client (SyslogClient in util-package)
* Database-version-check (XAPSVersionCheck)

Interface and object model
--------------------------
To get started using the DBI, there are two, three classes which are important.

* com.owera.xaps.dbi.DBI
* com.owera.xaps.dbi.XAPS
* com.owera.xaps.dbi.XAPSUnit

The DBI-class must be instantiated first. It will then setup a thread and run 
itself forever. From this object you can always retrieve the XAPS-object which 
holds the cached (and most important part of) object model. Whever some objects
in the cache are updated (on another server/module/etc), the DBI will get the
information (from the database) and update the XAPS object. It is therefore
important to always use the getXAPS() method on the DBI every time you want to
use the XAPS-object.

The XAPS-object contains information about Unittypes and Unittype. As soon as
you retrieve one Unittype object you also have access to Profiles, Groups, Jobs
and other objects. You must learn to traverse the object model, starting from
Unittype.

The XAPSUnit-object offers methods to retrieve information about Units (a Unit
is a logical representation of a physical device). You can search for units, 
change units, etc.

Messaging system
----------------
All servers/modules share the same database, and changes to the database will
eventually be detected by all modules. The before mentioned cache system for 
the most important objects uses this messaging system to update each other about
changes in the state of the model.

Apart from that, there are a few special cases were servers need to communicate
closely to perform some complex operation. Typical example is when the Web
interface needs to communicate with the STUN-server to initiate a TR-111 
request. All messages are visible in the message table for min. 90 sec.

Permission/Authorisation handling
---------------------------------
Permissions and information about users are read early, to decide which
authorisation level the user is going to operate at. The various controls/checks
on write actions are spread around to the appropriate write-routines, while
read actions are controlled in XAPS-object only. The assumption is that you will
not be able to use any other objects, unless you do not have the proper access
on the XAPS object.

Certificate handling
--------------------
There is a crypto-package to help generate certificates. The certificate
generation stuff is probably useful in itself, but perhaps no longer for this
product. Some years ago it was introduced to be able to sell parts of the
product, certificates were then mandatory to access this or that. This no
longer seems relevant, when going open-source. I will most likely remove all
certificate-barriers - but leave the code in this project for new use cases
that may appear.

Report generation
-----------------
Reports are compiled in the Core server, by usage of the report-package. The
reports are displaye in the Web server, again using this report-package. The 
report stuff is at times a bit complicated, due to my effort in making the
class generic. My apologies. 

TR-069 Testing
--------------
Some classes to offer testing of a TR-069 datamodel. Uses the xml-files in this
project to verify data coming from a device.


 
 






