package com.github.freeacs.base.http;

import com.github.freeacs.base.SessionDataI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A class which matches incoming requests with outgoing responses. If a second request from the same Unit 
 * arrive before the first request has gotten a response, it will signal 'false' - to allow a provisioning
 * server to discard the second request. 
 * 
 * At the same time, this class will deliver information to make it possible to track hang-situation, where
 * a response is delayed for a very long tim.
 * @author Morten
 *
 */
public class ThreadCounter {

	private static Map<String, Long> currentSessions = new HashMap<String, Long>();

	private static Logger logger = LoggerFactory.getLogger(ThreadCounter.class);

	/**
	 * Returns false if a request has not been responded to, and a second request is "counted"
	 * @param sessionData
	 * @return
	 */
	public static synchronized boolean isRequestAllowed(SessionDataI sessionData) {
		if (sessionData.getUnitId() != null) {
			String unitId = sessionData.getUnitId();
			Long tms = currentSessions.get(unitId);
			if (tms == null)
				currentSessions.put(unitId, System.currentTimeMillis());
			else {
				long diff = (System.currentTimeMillis() - tms) / 1000l;
				if (diff > 300) { // 5 minutes
					currentSessions.put(unitId, System.currentTimeMillis());
					logger.warn("Request from " + unitId + ",  " + diff + " seconds ago, has not received a response yet. Request will be allowed.");
				} else {
					logger.warn("Request from " + unitId + ", " + diff + " seconds ago, has not received a response yet. Request will be denied.");
					return false;
				}
			}
		}
		return true;
	}

	public static synchronized void responseDelivered(SessionDataI sessionData) {
		if (sessionData.getUnitId() != null)
			currentSessions.remove(sessionData.getUnitId());
	}

	public synchronized static Map<String, Long> cloneCurrentSessions() {
		Map<String, Long> currentSessionsClone = new HashMap<String, Long>();
		for (Entry<String, Long> entry : currentSessions.entrySet())
			currentSessionsClone.put(entry.getKey(), entry.getValue());
		return currentSessionsClone;
	}
	
	public static int currentSessionsCount() {
		return currentSessions.size();
	}
}