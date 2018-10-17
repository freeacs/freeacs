package com.github.freeacs.syslogserver;

import com.github.freeacs.common.util.Sleep;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FailoverFileReader implements Runnable {
  private static FailoverCounter counter = new FailoverCounter();

  private static Logger logger = LoggerFactory.getLogger(FailoverFileReader.class);

  private static Logger failedMessages = LoggerFactory.getLogger("FAILED");
  private final Properties properties;

  private Function<Properties, Long> MAX_AGE =
      (properties) -> properties.getMaxFailoverMessageAge().longValue() * 60 * 60 * 1000;

  private Function<Properties, Long> PROCESS_INTERVAL =
      (properties) -> properties.getFailoverProcessInterval().longValue() * 60 * 1000;

  private static boolean ok = true;

  private static Throwable throwable;

  public FailoverFileReader(Properties properties) {
    this.properties = properties;
  }

  public void run() {
    FailoverFile ff = FailoverFile.getInstance(properties);
    do {
      try {
        Thread.sleep(1000);
        if (Sleep.isTerminated()) {
          return;
        }
        if (System.currentTimeMillis() - ff.createdTms() > PROCESS_INTERVAL.apply(properties)
            && FailoverFile.getFailoverCount(true) > 0) {
          logger.info("Will process failover messages - that is to add them back into the system");
          File processFile = ff.rotate();
          if (processFile != null) {
            logger.info("The failover message log file is renamed and will be processed");
            FileReader fr = new FileReader(processFile);
            BufferedReader br = new BufferedReader(fr);
            int lineCounter = 0;
            do {
              if (Sleep.isTerminated()) {
                break;
              }
              int size = SyslogPackets.packets.size();
              if (size > 0) {
                Thread.sleep(SyslogPackets.packets.size() / 10 + 1);
              }
              String line = br.readLine();
              if (line == null) {
                break;
              }
              lineCounter++;
              String[] args = line.split("#@@#");
              long tms = Long.parseLong(args[1]);
              SyslogPacket packet = new SyslogPacket(args[3], args[2], tms, true);
              if (System.currentTimeMillis() - tms > MAX_AGE.apply(properties)) {
                failedMessages.error(packet + "Message was too old to retry");
                counter.incTooOld();
                continue;
              }
              SyslogPackets.add(packet, properties);
              counter.incRecycled();
            } while (true);
            br.close();
            fr.close();
            if (processFile.delete()) {
              logger.info(
                  "The "
                      + processFile.getName()
                      + " is processed ("
                      + lineCounter
                      + " messages) and deleted.");
              ok = true;
            } else {
              String msg =
                  "The "
                      + processFile.getName()
                      + " is processed ("
                      + lineCounter
                      + " messages) but not deleted! It will therefore be reprocessed!";
              throwable = new Throwable(msg);
              ok = false;
              logger.error(msg);
            }
          } else {
            String msg = "Could not rename the failover message log - no processing can occur.";
            throwable = new Throwable(msg);
            ok = false;
            logger.error(msg);
          }
        } else {
          ok = true;
        }
      } catch (Throwable t) {
        throwable = t;
        ok = false;
        if (!Sleep.isTerminated()) {
          logger.error("Error occured, FailoverFileReader continues", t);
        }
      }
    } while (true);
  }

  public static boolean isOk() {
    return ok;
  }

  public static Throwable getThrowable() {
    return throwable;
  }

  public static FailoverCounter getCounter() {
    return counter;
  }

  public static class FailoverCounter {
    private int recycled;
    private int tooOld;

    public FailoverCounter() {}

    public FailoverCounter(int recycled, int tooOld) {
      this.recycled = recycled;
      this.tooOld = tooOld;
    }

    public synchronized void incRecycled() {
      recycled++;
    }

    public synchronized void incTooOld() {
      tooOld++;
    }

    public synchronized FailoverCounter resetCounters() {
      FailoverCounter c = new FailoverCounter(recycled, tooOld);
      this.recycled = 0;
      this.tooOld = 0;
      return c;
    }

    public int getRecycled() {
      return recycled;
    }

    public int getTooOld() {
      return tooOld;
    }
  }
}
