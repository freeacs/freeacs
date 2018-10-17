package com.github.freeacs.syslogserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FailoverFile {
  private static String LOG_FILE_NAME = "fusion-syslog-failover.log";

  private static String PROCESSING_FILE_NAME = "fusion-syslog-failover-processing.log";

  private static BufferedWriter bw;

  private static File f = new File(LOG_FILE_NAME);

  private static int failoverCount;

  private static Logger logger = LoggerFactory.getLogger(FailoverFile.class);

  private static FailoverFile instance;

  private static Long created;

  private static long lastFlushed = System.currentTimeMillis();
  private final Properties properties;

  public FailoverFile(Properties properties) {
    this.properties = properties;
  }

  protected static FailoverFile getInstance(Properties properties) {
    if (instance == null) {
      instance = new FailoverFile(properties);
    }
    return instance;
  }

  protected long createdTms() {
    if (created == null) {
      created = System.currentTimeMillis() - properties.getFailoverProcessInterval() * 60 * 1000;
    }
    return created;
  }

  public synchronized void write(String s) {
    try {
      long tms = System.currentTimeMillis();
      getBufferedWriter().write(s);
      failoverCount++;
      if (tms > lastFlushed + 60000) {
        getBufferedWriter().flush();
        lastFlushed = tms;
      }
    } catch (IOException ioe) {
      logger.error("Tried to log to failover-file, but an exception ocurred: " + ioe);
    }
  }

  private BufferedWriter getBufferedWriter() throws IOException {
    if (bw == null) {
      bw = new BufferedWriter(new FileWriter(f, true));
    }
    return bw;
  }

  protected static long getFailoverCount(boolean checkStorage) {
    if (checkStorage && f.exists()) {
      try {
        BufferedReader br = new BufferedReader(new FileReader(f));
        int failoverFileLineCount = 0;
        while (br.readLine() != null) {
          failoverFileLineCount++;
        }
        br.close();
        failoverCount = failoverFileLineCount;
        if (failoverFileLineCount > 0) {
          logger.info("Counted " + failoverFileLineCount + " messages from failover-file.");
        }
      } catch (IOException ioe) {
        logger.error("Tried to count lines in failover-file, but an exception occured: " + ioe);
      }
    }
    return failoverCount;
  }

  protected synchronized File rotate() throws IOException {
    try {
      created = System.currentTimeMillis();
      File tmp = f;
      BufferedWriter oldBw = getBufferedWriter();
      oldBw.close();
      oldBw = null;
      boolean success = new File(PROCESSING_FILE_NAME).delete();
      logger.info("The result of the deletion of file " + PROCESSING_FILE_NAME + " was " + success);
      success = tmp.renameTo(new File(PROCESSING_FILE_NAME));
      logger.info("The result of the rename of tmp to " + PROCESSING_FILE_NAME + " was " + success);
      failoverCount = 0;
      return new File(PROCESSING_FILE_NAME);
    } catch (IOException ioe) {
      logger.error("The rotate operation could not be performed");
      return null;
    } finally {
      f = new File(LOG_FILE_NAME);
      bw = new BufferedWriter(new FileWriter(f));
      logger.info("Created a new file for logging failover messages");
    }
  }
}
