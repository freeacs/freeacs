FreeACS Fusion - Common
=======================
This project is a just a part of the whole product. Get the full picture here: 
http://www.freeacs.com/

Dependencies
------------
Junit in compile time classpath  
MySQL-driver in runtime classpath 

Eclipse setup
-------------
Git view: Import git repo
Git view: Import projects from git repo, import as general project
Package/Navigator view: Change project facets to Java 1.7
Add Junit to classpath (I have compiled with v4.1 - but v3.8 will also work)

Overview
--------
Provide common functionality used in the FreeACS product and is a result of more than 10 years of development. The features could provide useful for other projects. 

Common offers the following. In parenthesis I have specified the equivalent open source alternative - if applicable)

* Database connection pooling (+ some means to track what's going on) (Commons DBCP)
* A log4j replacement - simpler/better in some ways (Log4j)
* Scheduler for tasks/thread executions - example: run a cleanup job every 24h og every minute (Quartz)
* Caching mechanism - possible to specify timeout-values/types (EHCache)
* Property reader - reading a property file + reloading changes during runtime
* Pretty fast alphanumeric sort implementation - sorts alphanumeric strings correctly, taking both number value and alphabet into account

There are also other features here, which will be explained later.

Database connection pooling
---------------------------
Initially this was developed two escape dependency on Websphere database connection handling. These days I'm not sure how relevant this is, Apache commons DBCP is most likely a much better alternative (I have not investigated it). The main idea was to

* offer connection pooling without any dependency to application servers
* throw away all connection involved in SQLExceptions - all connections are returned explicitly back to this framework
* count connection meta data (successful accesses, rejected accesses, simultaneous accesses, free/used connections)
* multiple database connections (connect to several different db in the same runtime/JVM) 
* logging of all events and debug-logging 
* retries if rejected (some seconds) - hoping to overcome a temporary peak situation
* possible to dynamically decide to run with and without autocommit=true 
* tested and works fine on MySQL. Should work on any Database supporting JDBC.
* configurable both through property files and through coding (your choice).

All in all, this system has worked reasonably well for many years.

Log4j replacement
-----------------
I had used Log4j for many years, when I one day had enough of it. Classpath conflicts was one issue, since log4j was often used both in the application server and in my application. But the main driver was a number of other issues that this replacement fixes (IMHO):

* no conflicts with log4j-libraries in J2EE-server => no need to think of classloader-strategies
* simpler/better configuration - easier for customers/documentation
	- specify roll-on-size/time in one appender-configuration (e.g. roll log every 1 hour or if size > 1M)
	- specify backups for roll-on-time (for automatic cleanup in production environment)
	- no need to specify root-logger
	- no need to specify inheritance
	- automatic report of configuration-errors -> easy to correct
* automatic roll-on-time, even with no log-traffic!
* automatic roll-on-time in startup (an old log will be rolled out to correct file name)
* filename suffix convention kept in case of backups, to allow ease of use with editors (no .1 suffix anymore!)  
* performance increased with up to 300% (some cases - no increase) 
* complete dynamic reload of configuration - everything can change runtime!
* supports the Syslog-severity hierarchy (plus FATAL from log4j)

Of course, some features of log4j has been sacrificed. Even though it it possible to write new Appender-types (writing to 
database instead of file), it is perhaps not what this replacement library does best. The syntax/options of what can be printed
to file is also much more limited.

All in all, I believe this small system carries some merit, and can be useful for more than just FreeACS/Fusion.

Scheduler
---------
I did have a look on the Quartz Scheduler a long time ago. I cannot remember why I didn't choose it at the time, perhaps I thought it was too cumbersome - too much setup I guess. Anyway - the Scheduler does schedule tasks/processes/threads for execution. It offers the following:

- simple setup (ex: scheduler.registerTask(new Schedule(10000, false, ScheduleType.INTERVAL, new StabilityTask("StabilityLogger")));)
- schedule to run of an exact time every minute, hour, day or run at a specified interval
- if a task is not completed, the same task (on the next round) will not be initiated - will wait to next round
- tight control on exceptions in a task - you will be 100% sure that the task is either finished or failed
- a minimum of implementation is necessary if one subclasses TaskDefaultImpl
- can run a lot of different threads at the same time - limited to how quickly the JVM can spawn new threads

All in all: Rather simple, but easy to use (in my opinion). Quartz is probably much richer, but so far Scheduler has covered a lot of needs.

Caching mechanism
-----------------
EHCache was not made by the time this Cache was built. Really simple, but offers timeout based on last-access or time-of-creation or an absolute time limit. Can store anything, no limits. Reliable cleanup of the cache. Not sure if this is interesting for anyone.

Property reader
---------------
I needed a property reader which could detect changes in the property files. This is done through simple polling every 30 sec. In java 7 one can perhaps
implement this in a much neater way using NIO and the Watch Service API. 

Alphanumeric (NaturalComparator) sort
-----------------
If you have strings like this (think of version strings):

version-1.0.1  
version-1.70.1  
version-1.9.1

They should be sorted like this with alphanumeric sort:

version-1.0.1  
version-1.9.1  
version-1.70.1

This make sense for some types of strings. There has been put down some effort to speed up the comparison, each compare takes 9200 ns on one particular laptop some years ago.

Other stuff
-----------
* FileDatabase: A super-simple java-database. 
* ObjectGraph: An attempt on a making a way of measuring the number of bytes spent on a single object (and all it's references) - not entirely successful
* Sleep: A simple way to sleep some amount of time, and still detect if the server is going down
* TimestampMap: If you want to store Strings which have associated timestamp, but want to throw away automatically those strings that are too old. Also possible to traverse the map in chronological order, no matter which sequence they are added to the map. Very special use case...
* TimeWindow: Models a timewindow (ma-fr:0800-2000) and can always calculate the exact timestamp of each timewindow - can roll back/forth timewindows.
* Validation: Some classes to validate according to some rules
* HTML: A simple library to specify html. The idea was to code html in java, and then "render" it in the end. No need to think of end-tags anymore, the rendering sees to that. A bit hard to read perhaps..




	




