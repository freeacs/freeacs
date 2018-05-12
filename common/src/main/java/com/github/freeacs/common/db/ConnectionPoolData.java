package com.github.freeacs.common.db;

import java.sql.Connection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;

public class ConnectionPoolData {
	// Thread-safe to use java.util.Stack
	private Stack<Connection> freeConn = new Stack<Connection>();

	// Thread-safe to use java.util.Hashtable
	private Map<Connection, Long> usedConn = new Hashtable<Connection, Long>();

	// Thread-safe to use java.util.Hashtable
	private Map<Connection, Long> allConn = new Hashtable<Connection, Long>();

	private ConnectionMetaData metaData = new ConnectionMetaData();
	
	private ConnectionProperties props;

	private ConnectionCleanup cleanup;

	private Thread cleanupThread;
	
	private boolean newOracleVersion = true;

	public ConnectionPoolData(ConnectionProperties props) {
		this.props = props;
		startCleanup();
	}
	
	public void startCleanup() {
		cleanup = new ConnectionCleanup(this);
		cleanupThread = new Thread(cleanup);
		cleanupThread.setName("Cleanup of connections");
		cleanupThread.setDaemon(true);
		cleanupThread.start();		
	}
	

	public Map<Connection, Long> getAllConn() {
		return allConn;
	}

	public void setAllConn(Map<Connection, Long> allConn) {
		this.allConn = allConn;
	}

	public ConnectionCleanup getCleanup() {
		return cleanup;
	}

	public void setCleanup(ConnectionCleanup cleanup) {
		this.cleanup = cleanup;
	}

	public Thread getCleanupThread() {
		return cleanupThread;
	}

	public void setCleanupThread(Thread cleanupThread) {
		this.cleanupThread = cleanupThread;
	}

	public Stack<Connection> getFreeConn() {
		return freeConn;
	}

	public void setFreeConn(Stack<Connection> freeConn) {
		this.freeConn = freeConn;
	}

	public ConnectionMetaData getMetaData() {
		return metaData;
	}

	public void setMetaData(ConnectionMetaData metaData) {
		this.metaData = metaData;
	}

	public Map<Connection, Long> getUsedConn() {
		return usedConn;
	}

	public void setUsedConn(Map<Connection, Long> usedConn) {
		this.usedConn = usedConn;
	}

	public ConnectionProperties getProps() {
		return props;
	}

	public void setProps(ConnectionProperties props) {
		this.props = props;
	}

	public boolean isNewOracleVersion() {
		return newOracleVersion;
	}

	public void setNewOracleVersion(boolean newOracleVersion) {
		this.newOracleVersion = newOracleVersion;
	}

}
