package com.github.freeacs.common.util;

/**
 * The Sleep-class offers three important methods: sleep() - this method should return every
 * maxSleep ms terminateApplication() - will terminate any sleep() in the application isTerminated()
 * - will return the internal "terminate" flag (use in application to break/close/terminate threads)
 *
 * @author Morten
 */
public class Sleep {
  private static boolean terminated;
  private int maxSleep;
  private int minSleep;
  private long lastReturnTms;

  public Sleep(int maxSleep, int minSleep, boolean sleepFirstTime) {
    this.maxSleep = maxSleep;
    this.minSleep = minSleep;
    if (sleepFirstTime) {
      this.lastReturnTms = System.currentTimeMillis();
    } else {
      this.lastReturnTms = 0;
    }
  }

  /**
   * The sleep method should return every maxSleep ms. However, if it is not invoked before
   * (maxSleep-minSleep) ms has passed, it must sleep at least minSleep ms.
   */
  public void sleep() {
    if (terminated) {
      return;
    }
    long invokeTms = System.currentTimeMillis();
    long timeSinceLastReturn = invokeTms - lastReturnTms; // could be any number of ms
    // if timeSinceLastReturn > maxSleep, sleep at least minSleep ms
    // if timeSinceLastReturn > maxSleep-minSleep, sleep at least minSleep ms
    // if timeSinceLastReturn <= maxSleep-minSleep, sleep maxSleep-timeSinceLastReturn ms
    long suggestedSleepTime = maxSleep - timeSinceLastReturn;
    long sleepTime; // minimum sleep time is default
    if (suggestedSleepTime < minSleep) {
      sleepTime = minSleep;
    } else {
      sleepTime = suggestedSleepTime;
    }
    try {
      while (sleepTime > 1000) {
        Thread.sleep(1000);
        sleepTime -= 1000;
        if (terminated) {
          return;
        }
      }
      if (sleepTime > 0) {
        Thread.sleep(sleepTime);
      }
      if (terminated) {
        return;
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    lastReturnTms = System.currentTimeMillis();
  }

  /** By running this method you will abort the sleep-routine. */
  public static void terminateApplication() {
    if (!terminated) {
      terminated = true;
      try {
        // Expect the server to be fully terminated within 5 seconds
        Thread.sleep(2000);
      } catch (InterruptedException ignored) {
      }
    }
  }

  public static boolean isTerminated() {
    return terminated;
  }
}
