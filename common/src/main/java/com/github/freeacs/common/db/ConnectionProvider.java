package com.github.freeacs.common.db;

import com.github.freeacs.common.util.PropertyReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This connection provider can offer 
 * - connection pooling 
 * - specify max connections 
 * - specify max age 
 * - throw away all connection involved in SQLExceptions 
 * - connection meta data 
 * - count successful accesses 
 * - count rejected accesses 
 * - count simultaneous accesses 
 * - count free connections/used connections 
 * - multiple database connections (connect to several different db in the same runtime/JVM) 
 * - logging of all events and debug-logging 
 * - possible to decide to run without autocommit=true 
 * - tested and works fine on MySQL. Should work on any Database supporting JDBC.
 * 
 * @author morten
 */

public class ConnectionProvider {

	private static Logger log = LoggerFactory.getLogger(ConnectionProvider.class);

	private static Map<String, ConnectionPoolData> poolMap = new HashMap<String, ConnectionPoolData>();

	private static Map<Connection, ConnectionPoolData> connMap = new HashMap<Connection, ConnectionPoolData>();

	public static ConnectionPoolData getConnectionPoolData(ConnectionProperties props) {
		return poolMap.get(props.toString());
	}

	/**
	 * Use this method if you want to control the autoCommit feature of the
	 * connection. IMPORTANT! If you use this method, you should make sure than
	 * NO ONE ELSE (e.g. another app/user/whateever) is reusing the same
	 * connection without going through this method.
	 */
	public static Connection getConnection(ConnectionProperties props, boolean autoCommit) throws SQLException, NoAvailableConnectionException {
		Connection c = getConnection(props);
		if (c.getTransactionIsolation() == Connection.TRANSACTION_NONE)
			return c;
		if (c.getAutoCommit() && !autoCommit)
			c.setAutoCommit(autoCommit);
		else if (!c.getAutoCommit() && autoCommit) {
			c.setAutoCommit(autoCommit);
		}
		return c;
	}

	public static Connection getConnection(ConnectionProperties props) throws SQLException, NoAvailableConnectionException {
		long start = System.currentTimeMillis();
		NoAvailableConnectionException throwNace = null;
		while (System.currentTimeMillis() - start < 10000) {
			try {
				Connection c = getConnectionImpl(props);
				if (log.isDebugEnabled())
					log.debug("Connection returned in " + (System.currentTimeMillis() - start) + " ms");
				return c;
			} catch (NoAvailableConnectionException nace) {
				throwNace = nace;
				try {
					log.warn("Reached connection limit (" + props.getMaxConn() + ") for connection towards " + props.getUrl() + ", will wait 500 ms and retry");
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		ConnectionPoolData cpd = poolMap.get(props.toString());
		cpd.getMetaData().incDenied();
		log.error("No more available connections - retried " + cpd.getMetaData().getRetries() + " times, total " + (System.currentTimeMillis() - start) + " ms");
		throw throwNace;
	}

	/**
	 * The main interface to this class. Retrieves a connection. Will check to
	 * see if maxiumum number of connections is reached (maxConn), and it that
	 * case it throws an exception. Also resets a timestamp each time a
	 * connection is handed out, and puts that information in the usedConn-map.
	 */
	private static synchronized Connection getConnectionImpl(ConnectionProperties props) throws SQLException, NoAvailableConnectionException {
		if (poolMap.get(props.toString()) == null)
			poolMap.put(props.toString(), new ConnectionPoolData(props));
		ConnectionPoolData cpd = poolMap.get(props.toString());
		if (cpd.getCleanup().isBorn() && cpd.getCleanup().isDead()) {
			cpd.startCleanup();
		}

		if (cpd.getFreeConn().size() == 0) {
			if (cpd.getUsedConn().size() >= props.getMaxConn()) {
				cpd.getMetaData().incRetries();
				throw new NoAvailableConnectionException(props.getMaxConn());
			}
			Connection c = getNewConnection(cpd);
			cpd.getMetaData().incAccessed(cpd.getUsedConn().size());
			return c;
		} else {
			try {
				Connection c = cpd.getFreeConn().pop();
				if (cpd.getAllConn().get(c) < 0) { // marked as old
					cpd.getAllConn().put(c, -2l);
					connMap.remove(c);
					return getConnectionImpl(props);
				}
				if (c != null) {
					cpd.getUsedConn().put(c, new Long(System.currentTimeMillis()));
					log.debug("Reusing connection " + c);
					cpd.getMetaData().incAccessed(cpd.getUsedConn().size());
					return c;
				} else {
					cpd.getMetaData().incAccessed(cpd.getUsedConn().size());
					return getNewConnection(cpd);
				}
			} catch (EmptyStackException ese) {
				cpd.getMetaData().incAccessed(cpd.getUsedConn().size());
				return getNewConnection(cpd);
			}
		}
	}

	/**
	 * This method allows you to specify database credentials in a property file
	 * if you specify file name and database-property-key.
	 *
	 * Given a property file with this content:
	 *
	 * mydb.url = morten/morten@jdbc:mysql://xaps-a.owera.com:3306/xaps
	 * mydb.maxconn = 10 mydb.maxage = 600
	 *
	 * This method will need to know the name of this file and the key "mydb" to
	 * read and populate the ConnectionProperties object.
	 *
	 * url syntax: <user>/<password>@<jdbc-url-including-dbname> maxage:
	 * (Connection will be taken out of the pool at this point - will not abort
	 * running queries/executions) Specified in seconds. Default is 600.
	 * maxconn: Specified in number of connections. Default is 10.
	 *
	 *
	 * Another feature is "symlinks", where one property points to an already
	 * defined db-property
	 *
	 * anotherdb = mydb
	 *
	 * With this setup, the property file can contain the option of having
	 * several database connections (to various database), but also to be setup
	 * to point to the same database.
	 *
	 * @param propertyfile
	 *            - the name of the property file containing database
	 *            credentials/url and possibly maxage/maxconn
	 * @param dbkey
	 *            - the key used in one or more properties to identify the
	 *            credentials/url and possibly maxage/maxconn
	 * @return
	 */
	public static ConnectionProperties getConnectionProperties(String propertyfile, String dbkey) {
		// TODO: Change signature of method: propertyfile, dbname
		// TODO: Read user/password/url/maxage/maxconn from propertyfile
		PropertyReader pr = new PropertyReader(propertyfile);
		// Symlink check
		String symlink = pr.getProperty(dbkey);
		if (symlink != null && !symlink.contains("@")) {
			if (symlink.equals(dbkey)) {
				throw new IllegalArgumentException(dbkey + " references itself in the " + propertyfile + ", must point to another database configuration.");
			}
			dbkey = symlink;
		}
		return getConnectionProperties(getDbUrl(dbkey, pr), getMaxAge(pr.getProperty(dbkey + ".maxage")), getMaxConn(pr.getProperty(dbkey + ".maxconn")));
	}

	public static ConnectionProperties getConnectionProperties(String url, Long maxAge, int maxConn) {

		if (url == null)
			return null;

		// url is now hopefully a proper configuration on the form
		// <user>/<password>@<jdbc-url-including-dbname>
		ConnectionProperties props = new ConnectionProperties();
		try {
			props.setUrl(url.substring(url.indexOf("@") + 1));
			props.setUser(url.substring(0, url.indexOf("/")));
			props.setPassword(url.substring(url.indexOf("/") + 1, url.indexOf("@")));
		} catch (StringIndexOutOfBoundsException seoobe) {
			throw new IllegalArgumentException(url + " is not on a correct database-config-format (<user>/<password>@<jdbc-url>");
		}

		props.setMaxAge(maxAge);
		props.setMaxConn(maxConn);

		if (props.getUrl().contains("mysql"))
			props.setDriver("com.mysql.jdbc.Driver"); // This class must be specified in the classpath (dynamically loaded)
		else
			throw new IllegalArgumentException("The url is not pointing to a MySQL database");
		return props;
	}

	public static long getMaxAge(String property) {
		try {
			return Long.parseLong(property);
		} catch (Exception e) {
			return ConnectionProperties.maxage;
		}
	}

	public static int getMaxConn(String property) {
		try {
			return Integer.parseInt(property);
		} catch (Exception e) {
			return ConnectionProperties.maxconn;
		}
	}

	private static String getDbUrl(String dbkey, PropertyReader pr) {
		return Optional.ofNullable(pr.getProperty(dbkey + ".url"))
				.orElseGet(() -> pr.getProperty(dbkey));
	}

	/**
	 * Actually makes a new connection. Problems could be divided in two
	 * categories: 1. problems with wrong password, url, user and so on 2.
	 * problems with the driver, classpath and so on The second type of problem
	 * is more severe, because it will indicated a problem with the installation
	 * of the system. By throwing a RunTimeException we will probabaly abort the
	 * operation, all the way to the top.
	 */
	private static Connection getNewConnection(ConnectionPoolData cpd) throws SQLException, NoAvailableConnectionException {
		Statement s = null;
		try {
			ConnectionProperties props = cpd.getProps();
			try {
				Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			} catch(Exception ignored) {
				Class.forName(props.getDriver()).newInstance();
			}
			Connection c = DriverManager.getConnection(props.getUrl(), props.getUser(), props.getPassword());
			s = c.createStatement();
			s.execute("SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED");
			cpd.getUsedConn().put(c, new Long(System.currentTimeMillis()));
			cpd.getAllConn().put(c, new Long(System.currentTimeMillis()));
			connMap.put(c, cpd);
			log.debug("Created a new connection " + c);
			return c;
		} catch (SQLException sqle) {
			log.error("Tried to create a new connection", sqle);
			throw sqle;
		} catch (Exception e) {
			log.error("Tried to create a new connection, but something is seriously wrong", e);
			// This should only happen after if the driver
			// is not present in the classpath, or if the
			// driver is of the wrong version. Instead of throwing
			// a checked exception, we throw an unchecked
			throw new RuntimeException(e);
		} finally {
			try {
				if (s != null)
					s.close();
			} catch (Throwable t) {
				// do nothing
			}
		}
	}

	/**
	 * Returns a connection to the pool.
	 *
	 * If the connection is associated by a an SQL-exception or if it's too old,
	 * then the connection is not put back into the pool. If not, we check to
	 * see if the connection is closed or not. if it's closed ('accidentally' by
	 * an application it is not returned to the pool). But those that passes
	 * these tests are of course returned to the pool.
	 */
	public static synchronized void returnConnection(Connection c, SQLException sqle) {
		ConnectionPoolData cpd = null;
		try {
			if (c != null) {
				cpd = connMap.get(c);
				if (cpd == null) {
					log.error("Returned a connection which isn't created by this connectionprovider!!");
				} else {
					Long tms = cpd.getUsedConn().get(c);
					cpd.getUsedConn().remove(c);
					if (tms != null)
						cpd.getMetaData().addUsedTime(System.currentTimeMillis() - tms);
					else
						log.error("This method is run twice for the same connection. Wrong usage.");
					if (sqle != null) {
						cpd.getMetaData().incSqlEx();
						cpd.getAllConn().put(c, -2l);
						connMap.remove(c);
						log.warn("SQLException caused the connection to be invalidated", sqle);
					} else if (cpd.getAllConn().get(c) == -1) {
						cpd.getAllConn().put(c, -2l);
						connMap.remove(c);
						log.debug("Connection " + c + " is too old and will be removed");
					} else if (c.isClosed()) {
						cpd.getAllConn().put(c, -2l);
						connMap.remove(c);
						log.error("Connection is already closed by the application. Wrong usage.");
					} else {
						cpd.getFreeConn().push(c);
						log.debug("Connection " + c + " is returned to the pool");
					}
				}
			} else {
				log.error("Returning Connection=null. Wrong usage.");
			}
		} catch (Throwable t) {
			log.error("An error ocurred. The connection is invalidated.");
			if (c != null && cpd != null && cpd.getAllConn() != null) {
				cpd.getAllConn().put(c, -2l);
			}
		}
	}

	public static synchronized Map<Connection, Long> getUsedConnCopy(ConnectionProperties cp) {
		ConnectionPoolData cpd = getConnectionPoolData(cp);
		if (cpd != null)
			return new HashMap<Connection, Long>(cpd.getUsedConn());
		return null;
	}

}
