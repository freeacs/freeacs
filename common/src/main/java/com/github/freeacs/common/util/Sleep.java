package com.github.freeacs.common.util;

/**
 * The Sleep-class offers three important methods: sleep() - this method should return every
 * maxSleep ms terminateApplication() - will terminate any sleep() in the application isTerminated()
 * - will return the internal "terminate" flag (use in application to break/close/terminate threads)
 *
 * @author Morten
 */
public class Sleep {
  private static volatile boolean terminated = false;
  private final int maxSleep;
  private final int minSleep;
  private long lastReturnTms;

  public Sleep(int maxSleep, int minSleep, boolean sleepFirstTime) {
    this.maxSleep = maxSleep;
    this.minSleep = minSleep;
    this.lastReturnTms = sleepFirstTime ? System.currentTimeMillis() : 0;
  }

  /**
   * The sleep method should return every maxSleep ms. However, if it is not invoked before
   * (maxSleep-minSleep) ms has passed, it must sleep at least minSleep ms.
   */
  public void sleep() {
    if (terminated) {
      return;
    }
    long timeSinceLastReturn = System.currentTimeMillis() - lastReturnTms;
    long sleepTime = Math.max(minSleep, maxSleep - timeSinceLastReturn);

    try {
      Thread.sleep(sleepTime);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt(); // Preserve interrupt status
      System.out.println("Sleep was interrupted");
    }

    lastReturnTms = System.currentTimeMillis();
  }

  /** By running this method you will abort the sleep-routine. */
  public static void terminateApplication() {
    terminated = true;
  }

  public static boolean isTerminated() {
    return terminated;
  }
}
