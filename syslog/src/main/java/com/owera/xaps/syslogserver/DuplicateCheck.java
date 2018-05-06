package com.owera.xaps.syslogserver;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.owera.common.db.NoAvailableConnectionException;
import com.owera.common.log.Logger;
import com.owera.xaps.dbi.SyslogEntry;
import com.owera.xaps.dbi.SyslogEvent;

public class DuplicateCheck {
	private static HashMap<String, Duplicate> duplicateMap = new HashMap<String, Duplicate>();
	private static int counter = 0;
	private static int MAX_SIZE = Properties.getMaxMessagesInDuplicateBuffer();
	private static int CLEANUP_LIMIT_COUNTER = MAX_SIZE / 4;
	private static int CLEANUP_LIMIT_SIZE = 3 * MAX_SIZE / 4;
	private static Logger logger = new Logger(DuplicateCheck.class);

	public static int getDuplicateSize() {
		return duplicateMap.size();
	}

	private static void updateSyslogEntry(Duplicate duplicate) throws SQLException, NoAvailableConnectionException {
		if (duplicate.getCount() > 0) {
			logger.debug("Syslog entry is updated with duplicate counter = " + duplicate.getCount());
			String orgMsg = duplicate.getEntry().getContent();
			String newMsg = orgMsg + " (duplicate-count: " + duplicate.getCount() + " skipped from now until " + new Date(duplicate.getTimeout()) + ")";

			Syslog2DB.getSyslog().updateContent(duplicate.getTimeout() - SyslogEvent.DUPLICATE_TIMEOUT * 60000, orgMsg, newMsg, duplicate.getEntry().getUnitId());
		}
	}

	private static void cleanup(int counter) throws SQLException, NoAvailableConnectionException {
		if (counter <= CLEANUP_LIMIT_COUNTER || duplicateMap.size() <= CLEANUP_LIMIT_SIZE)
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

	private static boolean duplicate(String msg) throws SQLException, NoAvailableConnectionException {
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
	 * @param duplicateTimeout
	 * @return
	 * @throws SQLException
	 * @throws NoAvailableConnectionException
	 */
	public synchronized static boolean addMessage(String key, SyslogEntry entry, int duplicateTimeoutMinutes) throws SQLException, NoAvailableConnectionException {
		Duplicate duplicate = new Duplicate(entry, System.currentTimeMillis() + duplicateTimeoutMinutes * 60000);
		if (duplicateMap.size() <= MAX_SIZE && !duplicate(key)) {
			counter++;
			if (counter > CLEANUP_LIMIT_COUNTER && duplicateMap.size() > CLEANUP_LIMIT_SIZE) {
				counter = 0;
				cleanup(counter);
			}
			if (duplicateMap.size() < MAX_SIZE) {
				duplicateMap.put(key, duplicate);
				return true; // can only happen if no duplicate was found
			}
		}
		if (logger.isDebugEnabled())
			logger.debug("Message " + entry.getContent() + " was a duplicate.");
		return false; // duplicate was found or duplicate-map is too big
	}

}
