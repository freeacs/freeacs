package com.github.freeacs.core.task;

import com.github.freeacs.core.Properties;
import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.SyslogEvent;
import com.github.freeacs.dbi.Unittype;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteOldSyslog extends DBIShare {
  private final Properties properties;
  private ACS acs;

  private static Logger logger = LoggerFactory.getLogger(DeleteOldSyslog.class);

  public DeleteOldSyslog(String taskName, DBI dbi, Properties properties) {
    super(taskName, dbi);
    this.properties = properties;
  }

  @Override
  public void runImpl() throws Exception {
    acs = getLatestACS();
    removeOldSyslogEntries();
  }

  /**
   * 1. Find all eventIdLimits longer than 0 (which is default) 2. Iterate through severities (0-7),
   * for each severity 2.1. Find all eventIdLimits longer than severityLimit -> put eventId in a
   * list 2.2. Delete appropriate syslog statements 3. Iterate trough evetIds, for each eventId 3.1.
   * Find all severityLimits longer than eventIdLimits -> put severity in a list 3.2. Delete
   * appropriate syslog statments
   */
  private void removeOldSyslogEntries() throws SQLException {
    // 1.
    Unittype[] unittypeArr = acs.getUnittypes().getUnittypes();
    List<SyslogEvent> events = new ArrayList<>();
    for (Unittype ut : unittypeArr) {
      SyslogEvent[] eventArr = ut.getSyslogEvents().getSyslogEvents();
      for (SyslogEvent event : eventArr) {
        if (event.getEventId() >= 1000) {
          events.add(event);
        }
      }
    }
    // 2.
    removeOldSyslogEntriesSeverityBased(events);
    // 3.
    removeOldSyslogEntriesEventBased(events);
  }

  private void removeOldSyslogEntriesEventBased(List<SyslogEvent> events) throws SQLException {
    // 3.
    for (SyslogEvent event : events) {
      Calendar limitCal = Calendar.getInstance();
      if (event.getDeleteLimit() != null && event.getDeleteLimit() > 0) {
        limitCal.set(Calendar.DATE, limitCal.get(Calendar.DATE) - event.getDeleteLimit());
        limitCal.set(Calendar.HOUR_OF_DAY, 0);
        limitCal.set(Calendar.MINUTE, 0);
        limitCal.set(Calendar.SECOND, 0);
        limitCal.set(Calendar.MILLISECOND, 0);

        Calendar toCal = Calendar.getInstance();
        toCal.setTime(limitCal.getTime());

        Calendar fromCal = Calendar.getInstance();
        fromCal.setTime(limitCal.getTime());
        fromCal.roll(Calendar.DAY_OF_MONTH, -1);

        int days = 0;
        int rowsDeleted = 0;
        int loopCounter = 0;
        if (logger.isInfoEnabled()) {
          logger.info(
              "DeleteOldSyslog: Will delete all syslog rows older than "
                  + limitCal.getTime()
                  + " with eventId "
                  + event.getEventId()
                  + ", unittype "
                  + event.getUnittype().getName());
        }
        do {
          if (fromCal != null
              && toCal.getTime().getTime() - fromCal.getTime().getTime() >= 7 * 86400 * 1000) {
            logger.debug(
                "DeleteOldSyslog: Period is now one week - setting from to null => ask for all remaining entries");
            fromCal = null;
          }
          rowsDeleted = getSyslog().deleteOldEventsEntries(fromCal, toCal, event, 500000);
          if (fromCal != null) {
            if (logger.isInfoEnabled()) {
              logger.info(
                  "DeleteOldSyslog: "
                      + rowsDeleted
                      + " rows were deleted from the syslog table for the period "
                      + fromCal.getTime()
                      + " - "
                      + toCal.getTime());
            }
          } else if (logger.isInfoEnabled()) {
            logger.info(
                "DeleteOldSyslog: "
                    + rowsDeleted
                    + " rows were deleted from the syslog table from beginning - "
                    + toCal.getTime());
          }
          if (rowsDeleted == 500000) {
            loopCounter++;
          } else if (fromCal != null) {
            if (!"comprehensive".equals(properties.getSyslogCleanup()) && rowsDeleted == 0) {
              logger.debug(
                  "DeleteOldSyslog: Assuming no more dates need to be checked for deletion. Set to syslog.cleanup = comprehensive (in config) to check all dates");
              fromCal = null;
            } else {
              toCal.setTimeInMillis(fromCal.getTimeInMillis());
              if (loopCounter == 0) {
                fromCal.set(Calendar.DAY_OF_MONTH, fromCal.get(Calendar.DAY_OF_MONTH) - (++days));
                logger.debug(
                    "DeleteOldSyslog: Increasing the period by one day, since all entries were deleted on the first try");
              }
              fromCal.set(Calendar.DAY_OF_MONTH, fromCal.get(Calendar.DAY_OF_MONTH) - 1);
              loopCounter = 0;
            }
          }
        } while (rowsDeleted > 0 || fromCal != null);
      }
    }
  }

  /**
   * This method has been a lot more complex than initially intended. The reason is that deleting
   * rows in MySQL seems to be a major hassle. In an attempt to be nice to the database, we will not
   * delete all rows in one go. That why we delete only 10K or 100K rows at a time. However, to
   * match the indexes properly, we have put in a time frame (period) which makes the queries
   * (somewhat surprisingly) faster. The whole logic attempts to work it's way through the syslog
   * database back until there's is no more data.
   */
  private void removeOldSyslogEntriesSeverityBased(List<SyslogEvent> events) throws SQLException {
    // 2.
    for (int severity = 0; severity <= 7; severity++) {
      int severityLimit = properties.getSyslogSeverityLimit(severity);
      Calendar limitCal = Calendar.getInstance();
      limitCal.set(Calendar.DATE, limitCal.get(Calendar.DATE) - severityLimit);
      limitCal.set(Calendar.HOUR_OF_DAY, 0);
      limitCal.set(Calendar.MINUTE, 0);
      limitCal.set(Calendar.SECOND, 0);
      limitCal.set(Calendar.MILLISECOND, 0);

      Calendar toCal = Calendar.getInstance();
      toCal.setTime(limitCal.getTime());

      Calendar fromCal = Calendar.getInstance();
      fromCal.setTime(limitCal.getTime());
      fromCal.roll(Calendar.DAY_OF_MONTH, -1);

      // 2.1.
      List<SyslogEvent> eventList = new ArrayList<>();
      String eventListStr = "";
      for (SyslogEvent event : events) {
        // limit for the eventId is longer than the severity limit - add event id to list
        if (event.getDeleteLimit() != null && event.getDeleteLimit() > severityLimit) {
          eventList.add(event);
          eventListStr += "(" + event.getEventId() + "," + event.getUnittype().getName() + "), ";
        }
      }
      String logMsg =
          "Will delete all syslog rows older than "
              + limitCal.getTime()
              + " with severity level "
              + severity;
      if (eventListStr.length() > 2) {
        eventListStr = eventListStr.substring(0, eventListStr.length() - 2);
        logMsg += ", but these eventIds (" + eventListStr + ") will not be deleted";
      }

      // 2.2.
      int days = 0;
      int rowsDeleted = 0;
      int loopCounter = 0;
      if (logger.isInfoEnabled()) {
        logger.info("DeleteOldSyslog: " + logMsg);
      }
      do {
        if (fromCal != null
            && toCal.getTime().getTime() - fromCal.getTime().getTime() >= 7 * 86400 * 1000) {
          if (logger.isDebugEnabled()) {
            logger.debug(
                "DeleteOldSyslog: Period is now one week - setting from-tms to null => ask for all remaining entries");
          }
          fromCal = null;
        }
        rowsDeleted =
            getSyslog().deleteOldSeverityEntries(fromCal, toCal, severity, eventList, 500000);
        if (fromCal != null) {
          if (logger.isInfoEnabled()) {
            logger.info(
                "DeleteOldSyslog: "
                    + rowsDeleted
                    + " rows were deleted from the syslog table for the period "
                    + fromCal.getTime()
                    + " - "
                    + toCal.getTime());
          }
        } else if (logger.isInfoEnabled()) {
          logger.info(
              "DeleteOldSyslog: "
                  + rowsDeleted
                  + " rows were deleted from the syslog table from beginning - "
                  + toCal.getTime());
        }
        if (rowsDeleted == 500000) {
          loopCounter++;
        } else if (fromCal != null) {
          if (!"comprehensive".equals(properties.getSyslogCleanup()) && rowsDeleted == 0) {
            logger.debug(
                "DeleteOldSyslog: Assuming no more dates need to be checked for deletion. Set to comprehensive mode to check all dates");
            fromCal = null;
          } else {
            toCal.setTimeInMillis(fromCal.getTimeInMillis());
            if (loopCounter == 0) {
              fromCal.set(Calendar.DAY_OF_MONTH, fromCal.get(Calendar.DAY_OF_MONTH) - (++days));
              logger.debug(
                  "DeleteOldSyslog: Increasing the period by one day, since all entries were deleted on the first try");
            }
            fromCal.set(Calendar.DAY_OF_MONTH, fromCal.get(Calendar.DAY_OF_MONTH) - 1);
            loopCounter = 0;
          }
        }
      } while (rowsDeleted > 0 || fromCal != null);
    }
  }

  @Override
  public Logger getLogger() {
    return logger;
  }
}
