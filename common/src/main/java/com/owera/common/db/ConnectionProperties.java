package com.owera.common.db;

import com.owera.common.log.Logger;

/**
 * A simple wrapper class for the connection properties.
 * 
 * @author morten
 * 
 */
public class ConnectionProperties {
	private String driver;
	private String url;
	private String user;
	private String password;
	public static int maxconn = 10; // Default setting
	public static long maxage = 600000; // Default setting is 600 seconds

	private static final Logger log = new Logger();

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * Gets the maximum number of connections allowed.
	 * 
	 * @return int
	 */
	public int getMaxConn() {
		return maxconn;
	}

	public void setMaxConn(int maxconn) {
		if (maxconn < 2) {
			log.warn("Maximum connctions set below 2, changing it to 2 (url: " + url + ")");
			maxconn = 2;
		}
		this.maxconn = maxconn;
	}

	/**
	 * Gets the maximum ms a connection can be "idle".
	 * 
	 * @return
	 */
	public long getMaxage() {
		return maxage;
	}

	public void setMaxAge(long maxage) {
		if (maxage < 60000) {
			log.warn("Maximum age set below 60 sec, changing it to 60 (url: " + url + ")");
			maxconn = 60000;
		}
		this.maxage = maxage;
	}

	public String toString() {
		return url + user;
	}
}
