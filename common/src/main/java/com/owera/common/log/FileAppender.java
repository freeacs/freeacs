package com.owera.common.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.owera.common.util.Sleep;

public class FileAppender extends Appender {

  /*
   * Log method to debug the workings of FileAppender, not the actual log method
   * of FileAppender.
   */
  private String partOfMsg(String method) {
    StackTraceElement[] steArr = new Exception().getStackTrace();
    int i = 0;
    boolean triggeredByRollTimeThread = false;
    while (steArr[i].getClassName().indexOf("FileAppender") > -1) {
      i++;
      if (steArr[i].getClassName().indexOf("FileAppender$RollTime") > -1) {
        triggeredByRollTimeThread = true;
      }
    }
    String methodStr = "FileAppender." + method;
    if (triggeredByRollTimeThread)
      methodStr += " (T)";
    return String.format("%1$-50s%2$-15s", methodStr, appenderName);
  }

  /*
   * Log method to debug the workings of FileAppender, not the actual log method
   * of FileAppender.
   */
  private void sysout(String method, String msg) {
    System.out.println(new Date() + " INFO  " + partOfMsg(method) + " " + msg);
  }

  /*
   * Log method to debug the workings of FileAppender, not the actual log method
   * of FileAppender.
   */
  private void syserr(String method, String msg) {
    System.out.println(new Date() + " ERROR " + partOfMsg(method) + " " + msg);
  }

  public class RollTime implements Runnable {

    private void deleteOldBackups(File[] files) {
      Map<Long, File> lastModifiedMap = new TreeMap<Long, File>();
      for (File f : files) {
        Matcher m = filenamePattern.matcher(f.getName());
        if (m.matches()) {
          if (m.group(1).equals(filename) || (m.group(1) + m.group(3)).equals(filename)) {
            lastModifiedMap.put(f.lastModified(), f);
          }
        }
      }
      int filesToBeDeleted = lastModifiedMap.size() - backups;
      if (filesToBeDeleted > 0) {
        for (Entry<Long, File> entry : lastModifiedMap.entrySet()) {
          if (debug)
            sysout("RollTime.deleteOldBackups()", "Will delete " + entry.getValue().getAbsolutePath());
          boolean success = entry.getValue().delete();
          if (debug && success)
            sysout("RollTime.deleteOldBackups()", "Have deleted " + entry.getValue().getAbsolutePath());
          if (!success)
            syserr("RollTime.deleteOldBackups()", "Have NOT deleted" + entry.getValue().getAbsolutePath());
          filesToBeDeleted--;
          if (filesToBeDeleted <= 0)
            break;
        }
      }
    }

    public void run() {
      Sleep sleep = new Sleep(5000, 5000, true);
      while (true) {
        try {
          sleep.sleep();
          if (file != null) {
            // if (debug)
            // sysout("RollTime.run()", "Will process " +
            // file.getAbsolutePath() +
            // " to see if there is any need for a roll or deletion of old backups");
            LogObject lo = new LogObject();
            long now = System.currentTimeMillis();
            lo.setCurrentSecond(now / 1000);
            lo.setTms(now);
            rollOnTime(lo);
            deleteOldBackups(backupDir.listFiles());
          }
          if (Sleep.isTerminated())
            break;
        } catch (Throwable t) {
          if (monitor == null)
            return; // app is undeployed
          else
            syserr("RollTime.run()", "An error occured :" + t);
        }
      }
    }
  }

  public class Flush implements Runnable {

    public void run() {
      Sleep sleep = new Sleep(1000, 1000, true);
      while (true) {
        try {
          sleep.sleep();
          synchronized (monitor) {
            if (bufferedWriter != null)
              bufferedWriter.flush();
            if (Sleep.isTerminated()) {
              if (bufferedWriter != null)
                bufferedWriter.flush();
              break;
            }

          }
        } catch (Throwable t) {
          if (monitor == null)
            return; // app is undeployed
          else
            syserr("Flush.run()", "An error occured :" + t);
        }
      }
    }
  }

  private static int TYPE_NO_ROLL = 0;
  private static int TYPE_ROLL_SIZE = 1;
  private static int TYPE_ROLL_TIME = 2;
  private static File backupDir;
  private static Pattern rollEveryPattern = Pattern.compile("(\\d+)(MB|KB)|minute|hour|day|month|year");
  private static Pattern tmsPattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
  private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private static Pattern filenamePattern = Pattern.compile("([^\\.]+).(\\d+)(\\..*)?");

  private int type = TYPE_NO_ROLL;

  private int maxSize;
  private int backups;
  private String timeFormat;
  private String filename;
  private File file;
  private long fileSize;
  private BufferedWriter bufferedWriter;
  // Use lastSecond to avoid some computations more than once pr sec
  private long lastSecond;
  // Use lastMinute to avoid some computations more than once pr min
  private int lastMinute = -1; // use -1 as initial state - no backup at that
  // point
  // Use lastMinuteTms to avoid some computations more than once pr min
  private long lastMinuteTms;
  // Is used to calculate the exact minute we're in
  private int lastHour;
  private long lastHourTms;
  private int lastDay;
  private long lastDayTms;
  private int lastMonth;
  private long lastMonthTms;
  private int lastYear;
  private Calendar cal = Calendar.getInstance();
  // A monitor object to synchronize backups
  private Object monitor = new Object();
  private Thread rollOnTimeThread;
  private Thread flushThread;
  private boolean debug = false;

  private String getFilename(String filename, String backupStr) {
    if (filename.indexOf(".") > -1) {
      int lastDotPos = filename.lastIndexOf(".");
      return filename.substring(0, lastDotPos) + backupStr + filename.substring(lastDotPos);
    } else {
      return filename + "." + backupStr;
    }
  }

  // Produces a time-format filename
  private String getFilename(String filename) {
    StringBuilder sb = new StringBuilder();
    sb.append(lastYear);
    if (!timeFormat.equals("year")) { // must be month, day, hour or minute
      if (lastMonth < 10)
        sb.append("0");
      sb.append(lastMonth);
      if (!timeFormat.equals("month")) { // must be day, hour or minute
        if (lastDay < 10)
          sb.append("0");
        sb.append(lastDay);
        if (!timeFormat.equals("day")) { // must be hour or minute
          if (lastHour < 10)
            sb.append("0");
          sb.append(lastHour);
          if (!timeFormat.equals("hour")) { // must be minute
            if (lastMinute < 10)
              sb.append("0");
            sb.append(lastMinute);
          }
        }
      }
    }
    return getFilename(filename, "." + sb.toString());

  }

  private void rollOnTime(LogObject lo) throws IOException {
    // Calculate the minute at maximum once pr second
    if (lo.getCurrentSecond() != lastSecond) {
      lastSecond = lo.getCurrentSecond();
      cal.setTimeInMillis(lo.getTms());
      int currentMinute = cal.get(Calendar.MINUTE);
      if (currentMinute != lastMinute || lo.getTms() - lastMinuteTms > 60000) {
        synchronized (monitor) {
          if (file == null)
            return;
          if (currentMinute != lastMinute || lo.getTms() - lastMinuteTms > 60000) {
            int currentHour = cal.get(Calendar.HOUR_OF_DAY);
            int currentDay = cal.get(Calendar.DAY_OF_MONTH);
            int currentMonth = cal.get(Calendar.MONTH) + 1;
            int currentYear = cal.get(Calendar.YEAR);
            if (lastMinute > -1) {
              boolean roll = false;
              if (timeFormat.equals("minute")) {
                roll = true;
              } else if (timeFormat.equals("hour") && (currentHour != lastHour || lo.getTms() - lastHourTms > 60000 * 60)) {
                roll = true;
              } else if (timeFormat.equals("day") && (currentDay != lastDay || lo.getTms() - lastDayTms > 60000 * 60 * 24)) {
                roll = true;
              } else if (timeFormat.equals("month") && (currentMonth != lastMonth || lo.getTms() - lastMonthTms > 60000l * 60l * 24l * 31l)) {
                roll = true;
              } else if (timeFormat.equals("year") && currentYear != lastYear) {
                roll = true;
              }
              if (roll && file.length() > 0) {
                if (bufferedWriter != null)
                  bufferedWriter.close();
                String renameFilename = getFilename(filename);
                File backup = new File(backupDir.getName() + File.separator + renameFilename);
                if (debug)
                  sysout("rollOnTime()", "Will roll " + file.getAbsolutePath() + " to " + backup.getAbsolutePath());
                boolean success = file.renameTo(backup);
                if (!success)
                  syserr("rollOnTime()", "Have NOT rolled " + file.getAbsolutePath());
                if (success && debug)
                  sysout("rollOnTime()", "Have rolled " + file.getAbsolutePath());
                file = new File(filename);
                fileSize = file.length();
                bufferedWriter = new BufferedWriter(new FileWriter(file, true));
              }
            }
            lastYear = currentYear;
            lastMonth = currentMonth;
            lastMonthTms = lo.getTms();
            lastDay = currentDay;
            lastDayTms = lo.getTms();
            lastHour = currentHour;
            lastHourTms = lo.getTms();
            lastMinute = currentMinute;
            lastMinuteTms = lo.getTms();
          }
        }
      }
    }
  }

  private void rollOnSize(LogObject lo) throws IOException {
    if (fileSize >= maxSize) {
      synchronized (monitor) {
        if (file == null)
          return;
        if (fileSize >= maxSize) {
          for (int i = backups; i > 0; i--) {
            String backupFilename = getFilename(filename, "." + i);
            File f = new File(backupDir.getName() + File.separator + backupFilename);
            if (f.exists()) {
              if (i == backups) {
                if (debug)
                  sysout("rollOnSize()", "Will delete " + f.getAbsolutePath());
                boolean success = f.delete();
                if (!success)
                  syserr("rollOnSize()", "Have NOT deleted " + f.getAbsolutePath());
                if (success && debug)
                  sysout("rollOnSize()", "Have deleted " + f.getAbsolutePath());
              } else {
                String renameFilename = getFilename(filename, "." + (i + 1));
                String renameFilenameFullPath = backupDir.getName() + File.separator + renameFilename;
                if (debug)
                  sysout("rollOnSize()", "Will roll " + f.getAbsolutePath() + " to " + renameFilenameFullPath);
                boolean success = f.renameTo(new File(renameFilenameFullPath));
                if (!success)
                  syserr("rollOnSize", "Have NOT rolled " + f.getAbsolutePath());
                if (success && debug)
                  sysout("rollOnSize", "Have rolled " + f.getAbsolutePath());
              }
            }
          }
          if (bufferedWriter != null)
            bufferedWriter.close();
          String renameFilename = getFilename(filename, ".1");
          File backup = new File(backupDir.getName() + File.separator + renameFilename);
          if (debug)
            sysout("rollOnSize()", "Will roll " + file.getAbsolutePath() + " to " + backup.getAbsolutePath());
          boolean success = file.renameTo(backup);
          if (!success)
            syserr("rollOnSize()", "Have NOT rolled " + backup.getAbsolutePath());
          if (success && debug)
            sysout("rollOnSize()", "Have rolled " + backup.getAbsolutePath());
          file = new File(filename);
          fileSize = file.length();
          bufferedWriter = new BufferedWriter(new FileWriter(file, true));
        }
      }
      int backups_to_be_deleted = backups + 1;
      while (true) {
        String backupFilename = getFilename(filename, "." + backups_to_be_deleted);
        File f = new File(backupDir.getName() + File.separator + backupFilename);
        if (f.exists()) {
          if (debug)
            sysout("rollOnSize()", "Will delete " + f.getAbsolutePath());
          boolean success = f.delete();
          if (!success)
            syserr("rollOnSize()", "Have NOT deleted " + f.getAbsolutePath());
          if (success && debug)
            sysout("rollOnSize()", "Have deleted " + f.getAbsolutePath());
        } else
          break;
        backups_to_be_deleted++;
      }

    }
  }

  private void backup(LogObject lo) throws IOException {
    if (type == TYPE_ROLL_TIME) {
      rollOnTime(lo);
    } else if (type == TYPE_ROLL_SIZE) {
      rollOnSize(lo);
    }
  }

  // We synchronize this process, to avoid file-write conflicts
  // and problems that might occur when logs are rolled.
  public void log(LogObject lo) throws IOException {
    if (file != null)
      backup(lo);
    String s = lo.getCompleteMessage();
    synchronized (monitor) {
      if (file == null) {
        file = new File(filename);
        fileSize = file.length();
      }
      if (bufferedWriter == null)
        bufferedWriter = new BufferedWriter(new FileWriter(file, true));
      bufferedWriter.write(s);
      fileSize += s.length();
    }
  }

  @Override
  public void constructor() {
    String rollEvery = propertyReader.getProperty(appenderName + ".roll-every");
    if (rollEvery != null) {
      Matcher m = rollEveryPattern.matcher(rollEvery);
      if (m.matches()) {
        if (m.group(2) != null) { // Hit on KB or MB
          type = TYPE_ROLL_SIZE;
          maxSize = new Integer(m.group(1)) * 1024;
          if (m.group(2).endsWith("MB"))
            maxSize *= 1024;
        } else {
          timeFormat = rollEvery;
          type = TYPE_ROLL_TIME;
        }
      } else {
        ConfigErrors.add("The roll-every value for appender " + appenderName + " is not allowed, defaults to 10MB");
        type = TYPE_ROLL_SIZE;
        maxSize = 10 * 1024 * 1024;
      }
      String backupStr = propertyReader.getProperty(appenderName + ".backups");
      if (backupStr != null) {
        try {
          backups = new Integer(backupStr);
        } catch (NumberFormatException nfe) {
          ConfigErrors.add(appenderName + ".backups was not a number, defaults to " + defaultBackups());
        }
      } else {
        ConfigErrors.add(appenderName + ".backups was not specified was not a number, defaults to " + defaultBackups());
      }
    } else {
      backups = 0; // if no rolling - no backups
    }
    String debugStr = propertyReader.getProperty(appenderName + ".debug");
    if (debugStr != null && (debugStr.equals("1") || debugStr.equalsIgnoreCase("true") || debugStr.equalsIgnoreCase("yes") || debugStr.equalsIgnoreCase("on")))
      debug = true;
    String backupDirName = propertyReader.getProperty("backup-directory");
    if (backupDirName == null)
      backupDir = new File("backup-logs");
    else
      backupDir = new File(backupDirName);
    if (!backupDir.exists())
      backupDir.mkdir();
    if (filename == null && type == TYPE_ROLL_TIME) {// This is the FIRST
      // time
      // Rolling.constructor()
      // is called
      filename = propertyReader.getProperty(appenderName + ".file");
      backupOldLog(filename);
    } else
      filename = propertyReader.getProperty(appenderName + ".file");
    if (rollOnTimeThread == null && type == TYPE_ROLL_TIME) {
      rollOnTimeThread = new Thread(new RollTime());
      rollOnTimeThread.setName("RollOnTimeThread for " + appenderName);
      rollOnTimeThread.setDaemon(true);
      rollOnTimeThread.start();
    }
    if (flushThread == null) {
      flushThread = new Thread(new Flush());
      flushThread.setName("FlushThread for " + appenderName);
      flushThread.setDaemon(true);
      flushThread.start();
    }
  }

  private String defaultBackups() {
    if (type == TYPE_ROLL_SIZE) {
      backups = 0;
      return "0 (no backups are taken)";
    }
    if (type == TYPE_ROLL_TIME) {
      backups = Integer.MAX_VALUE;
      return "no limit (actually limit is " + Integer.MAX_VALUE + " backups)";
    }
    return "Illegal state - this should not have happened";
  }

  /*
   * This method will check the old log to see if there is a tms at the end of
   * the file. If there is, it is parsed and then the system sends this tms to
   * the rollOnTime(LogObject) method, thereby deciding if the log should roll
   * immediately.
   */
  private void backupOldLog(String filename) {
    try {
      RandomAccessFile raf = new RandomAccessFile(filename, "r");
      long bufsize = 5000;
      if (raf.length() > bufsize) // We investigate maximum 5KB from at
        // the end of the file
        raf.seek(raf.length() - bufsize);
      else {
        bufsize = raf.length();
      }
      byte[] buf = new byte[(int) bufsize];
      raf.readFully(buf);
      raf.close();
      String endOfFile = new String(buf);
      Matcher matcher = tmsPattern.matcher(endOfFile);
      int pos = 0;
      Date d = null;
      while (matcher.find(pos)) {
        d = dateFormat.parse(matcher.group(0));
        pos = matcher.end();
      }
      if (d != null) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        lastYear = c.get(Calendar.YEAR);
        lastMonth = c.get(Calendar.MONTH) + 1;
        lastMonthTms = d.getTime();
        lastDay = c.get(Calendar.DAY_OF_MONTH);
        lastDayTms = d.getTime();
        lastHour = c.get(Calendar.HOUR_OF_DAY);
        lastHourTms = d.getTime();
        lastMinute = c.get(Calendar.MINUTE);
        lastMinuteTms = d.getTime();
        lastSecond = d.getTime() / 1000;
        file = new File(filename);
        long now = System.currentTimeMillis();
        LogObject lo = new LogObject();
        lo.setCurrentSecond(now / 1000);
        lo.setTms(now);
        rollOnTime(lo);
      }
    } catch (Throwable t) {
      // Don't bother - the file may not exist - and the worst thing
      // that can happen is that it's not rolled.
    }
  }

  @Override
  public String toString() {
    if (type == TYPE_NO_ROLL)
      return this.getClass().getSimpleName() + ", no rolling";
    else
      return this.getClass().getSimpleName() + ", rolling type is " + type;
  }

}
