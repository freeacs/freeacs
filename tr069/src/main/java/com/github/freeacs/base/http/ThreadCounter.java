package com.github.freeacs.base.http;

import com.github.freeacs.base.SessionDataI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * A class which matches incoming requests with outgoing responses. If a second request from the
 * same Unit arrive before the first request has gotten a response, it will signal 'false' - to
 * allow a provisioning server to discard the second request.
 *
 * <p>At the same time, this class will deliver information to make it possible to track
 * hang-situation, where a response is delayed for a very long tim.
 *
 * @author Morten
 */
@Component
public class ThreadCounter {
  private final Map<String, Long> currentSessions = new HashMap<>();

  private static Logger logger = LoggerFactory.getLogger(ThreadCounter.class);

  /**
   * Returns false if a request has not been responded to, and a second request is "counted".
   */
  public synchronized boolean isRequestAllowed(SessionDataI sessionData) {
    if (sessionData.getUnitId() != null) {
      String unitId = sessionData.getUnitId();
      Long tms = currentSessions.get(unitId);
      if (tms == null) {
        currentSessions.put(unitId, System.currentTimeMillis());
      } else {
        long diff = (System.currentTimeMillis() - tms) / 1000L;
        if (diff > 300) { // 5 minutes
          currentSessions.put(unitId, System.currentTimeMillis());
          logger.warn(
              "Request from "
                  + unitId
                  + ",  "
                  + diff
                  + " seconds ago, has not received a response yet. Request will be allowed.");
        } else {
          logger.warn(
              "Request from "
                  + unitId
                  + ", "
                  + diff
                  + " seconds ago, has not received a response yet. Request will be denied.");
          return false;
        }
      }
    }
    return true;
  }

  public synchronized void responseDelivered(SessionDataI sessionData) {
    if (sessionData.getUnitId() != null) {
      currentSessions.remove(sessionData.getUnitId());
    }
  }

  synchronized Map<String, Long> cloneCurrentSessions() {
    Map<String, Long> currentSessionsClone = new HashMap<>();
    for (Entry<String, Long> entry : currentSessions.entrySet()) {
      currentSessionsClone.put(entry.getKey(), entry.getValue());
    }
    return currentSessionsClone;
  }

  int currentSessionsCount() {
    return currentSessions.size();
  }
}
