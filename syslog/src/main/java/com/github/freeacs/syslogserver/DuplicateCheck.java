package com.github.freeacs.syslogserver;

import com.github.freeacs.dbi.SyslogEntry;
import com.github.freeacs.dbi.SyslogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class DuplicateCheck {
	private static HashMap<String, Duplicate> duplicateMap = new HashMap<String, Duplicate>();
	private static int counter = 0;
	private static Logger logger = LoggerFactory.getLogger(DuplicateCheck.class);

	private static int getMaxSize() {
		return Properties.MAX_MESSAGES_IN_DUPLICATE_BUFFER;
	}

	private static int getCleanupLimitCounter() {
		return getMaxSize() / 4;
	}

	private static int getCleanupLimitSize() {
		return 3 * getMaxSize() / 4;
	}

	static int getDuplicateSize() {
		return duplicateMap.size();
	}

	private static void updateSyslogEntry(Duplicate duplicate) throws SQLException {
		if (duplicate.getCount() > 0) {
			logger.debug("Syslog entry is updated with duplicate counter = " + duplicate.getCount());
			String orgMsg = duplicate.getEntry().getContent();
			String newMsg = orgMsg + " (duplicate-count: " + duplicate.getCount() + " skipped from now until " + new Date(duplicate.getTimeout()) + ")";

			Syslog2DB.getSyslog().updateContent(duplicate.getTimeout() - SyslogEvent.DUPLICATE_TIMEOUT * 60000, orgMsg, newMsg, duplicate.getEntry().getUnitId());
		}
	}

	private static void cleanup(int counter) throws SQLException {
		if (counter <= getCleanupLimitCounter() || duplicateMap.size() <= getCleanupLimitSize())
			return;
		logger.info("Duplicate Message Buffer cleanup initiated (counter:" + counter + ", size:" + duplicateMap.size() + ")");
		Iterator<String> i = duplicateMap.keySet().iterator();
		long now = System.currentTimeMillis();
		int removeCounter = 0;
		while (i.hasNext()) {
			Duplicate duplicate = duplicateMap.get(i.next());
			if (duplicate.getTimeout() > now) {
				updateSyslogEntry(duplicate);
				i.remove();
				removeCounter++;
			}
		}
		logger.info(removeCounter + " messages removed from Duplicate Message Buffer (size:" + duplicateMap.size() + ")");
	}

	private static boolean duplicate(String msg) throws SQLException {
		Duplicate duplicate = duplicateMap.get(msg);
		if (logger.isDebugEnabled()) {
			if (duplicate == null)
				logger.debug("No duplicate found for message " + msg);
			else
				logger.debug("Duplicate found, tms " + new Date(duplicate.getTimeout()) + " for message " + msg);

		}
		if (duplicate == null) // no duplicate
			return false;
		else if (System.currentTimeMillis() > duplicate.getTimeout()) { // duplicate is too old 
			updateSyslogEntry(duplicate);
			duplicateMap.remove(msg);
			return false;
		} // else  - duplicate found
		duplicate.incCount();
		return true;
	}

	/**
	 * Will add a message to the duplicate map if no duplicate is found or if duplicate is too old
	 * @param key
	 * @param entry
	 * @return
	 * @throws SQLException
	 *
	 */
	public synchronized static boolean addMessage(String key, SyslogEntry entry, int duplicateTimeoutMinutes) throws SQLException {
		Duplicate duplicate = new Duplicate(entry, System.currentTimeMillis() + duplicateTimeoutMinutes * 60000);
		if (duplicateMap.size() <= getMaxSize() && !duplicate(key)) {
			counter++;
			if (counter > getCleanupLimitCounter() && duplicateMap.size() > getCleanupLimitSize()) {
				counter = 0;
				cleanup(counter);
			}
			if (duplicateMap.size() < getMaxSize()) {
				duplicateMap.put(key, duplicate);
				return true; // can only happen if no duplicate was found
			}
		}
		if (logger.isDebugEnabled())
			logger.debug("Message " + entry.getContent() + " was a duplicate.");
		return false; // duplicate was found or duplicate-map is too big
	}

}
