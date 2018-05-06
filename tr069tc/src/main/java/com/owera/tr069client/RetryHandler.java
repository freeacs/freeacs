package com.owera.tr069client;

import java.io.IOException;

import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.protocol.HttpContext;

import com.owera.tr069client.monitor.Status;

public class RetryHandler implements HttpRequestRetryHandler {

  private Status status;

  public RetryHandler(Status status) {
    this.status = status;
  }

  public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
    status.incRetryOccured(status.getCurrentOperation());
    long delay = Util.getRetrySleep(executionCount);
    if (delay == -1)
      return false;
    try {
      int previousDelay = status.getRetrySleep();
      status.setRetrySleep((int) delay + previousDelay);
      Thread.sleep(delay);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return true;

  }

}
