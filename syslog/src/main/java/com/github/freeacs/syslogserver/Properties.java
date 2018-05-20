package com.github.freeacs.syslogserver;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Properties {

	public static Integer MIN_SYSLOGDB_COMMIT_DELAY;
	public static Integer MAX_SYSLOGDB_COMMIT_QUEUE;
	public static Integer MAX_MESSAGES_PER_MINUTE;
	public static Integer MIN_FREE_DISK_SPACE;
	public static String UNKNOWN_UNITS_ACTION;
	public static Integer MAX_FAILOVER_MESSAGE_AGE;
	public static Integer MAX_SYSLOGDB_THREADS;
	public static Integer MAX_MESSAGES_IN_DUPLICATE_BUFFER;
	public static Integer MAX_MESSAGES_IN_BUFFER;
	public static Integer RECEIVE_BUFFER_SIZE;
	public static Integer FAILOVER_PROCESS_INTERVAL;
	public static Integer PORT;
	public static boolean SIMULATION;

	@Value("${simulation:false}")
	public void setSimulation(Boolean simulation) {
		SIMULATION = simulation;
	}

	@Value("${port:9116}")
	public void setPort(Integer port) {
		PORT = port;
	}

	@Value("${failover-process-interval:30}")
	public void setFailoverProcessInterval(Integer interval) {
		FAILOVER_PROCESS_INTERVAL = interval;
	}

	@Value("${receive-buffer-size:10240}")
	public void setReceiveBufferSize(Integer bufferSize) {
		RECEIVE_BUFFER_SIZE = bufferSize;
	}

	@Value("${max-messages-in-buffer:100000}")
	public void setMaxMessagesInBuffer(Integer maxMessages) {
		MAX_MESSAGES_IN_BUFFER = maxMessages;
	}

	@Value("${max-message-in-duplicate-buffer:100000}")
	public void setMaxMessagesInDuplicateBuffer(Integer maxMessages) {
		MAX_MESSAGES_IN_DUPLICATE_BUFFER = maxMessages;
	}

	@Value("${max-syslogdb-threads:1}")
	public void setMaxSyslogdbThreads(Integer maxThreads) {
		MAX_SYSLOGDB_THREADS = maxThreads;
	}

	@Value("${max-failover-message-age:24}")
	public void setMaxFailoverMessageAge(Integer maxAge) {
		MAX_FAILOVER_MESSAGE_AGE = maxAge;
	}

	@Value("${unknown-units:discard}")
	public void setUnknownUnitsAction(String action) {
		UNKNOWN_UNITS_ACTION = action;
	}

	@Value("${min-free-disk-space:100}")
	public void setMinFreeDiskSpace(Integer minFreeDiskSpace) {
		MIN_FREE_DISK_SPACE = minFreeDiskSpace;
	}

	@Value("${max-message-pr-minute:10000}")
	public void setMaxMessagesPrMinute(Integer maxMessagesPrMinute) {
		MAX_MESSAGES_PER_MINUTE = maxMessagesPrMinute;
	}

	@Value("${max-syslog-db-commit-queue:1000}")
	public void setMaxDBCommitQueue(Integer maxDBCommitQueue) {
		MAX_SYSLOGDB_COMMIT_QUEUE = maxDBCommitQueue;
	}

	@Value("${min-syslog-db-commit-delay:5000}")
	public void setMinDBCommitDelay(Integer minDBCommitDelay) {
		MIN_SYSLOGDB_COMMIT_DELAY = minDBCommitDelay;
	}

	public static String getDeviceIdPattern(int index) {
		// TODO should this be reimplemented?
		return null;
	}

}
