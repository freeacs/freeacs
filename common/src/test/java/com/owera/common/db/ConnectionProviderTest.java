package com.owera.common.db;

import junit.framework.TestCase;
import org.junit.Ignore;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Ignore
public class ConnectionProviderTest extends TestCase {

	private ConnectionProperties props;

	private ConnectionProperties getConnectionProperties(int db) {

		ConnectionProperties connectionProperties = new ConnectionProperties();
		if (db == 1) {
			connectionProperties.setDriver("oracle.jdbc.driver.OracleDriver");
			connectionProperties.setUser("intern");
			connectionProperties.setPassword("intern");
			connectionProperties.setUrl("jdbc:oracle:thin:@//xaps-c.owera.com:1521/orcl");
		} else {
			connectionProperties.setDriver("com.mysql.jdbc.Driver");
			connectionProperties.setUser("dlink");
			connectionProperties.setPassword("dlink");
			connectionProperties.setUrl("jdbc:mysql://xaps-c.owera.com:3306/dlink");
		}
		connectionProperties.setMaxConn(1);
		connectionProperties.setMaxAge(5000);
		return connectionProperties;
	}

	public ConnectionProviderTest(String name) {
		super(name);
	}

	public void testSingleConnection() {
		try {
			Connection c = ConnectionProvider.getConnection(props);
			assertNotNull(c);
			assertFalse(c.isClosed());
			ConnectionProvider.returnConnection(c, null);
			assertFalse(c.isClosed());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public void testConnectionPerformance() {
		long start = System.currentTimeMillis();
		int noConnect = 10000;
		for (int i = 0; i < noConnect; i++) {
			try {
				Connection c = ConnectionProvider.getConnection(props);
				ConnectionProvider.returnConnection(c, null);
			} catch (NoAvailableConnectionException nace) {
				System.err.println("No available connection");
			} catch (SQLException e) {
				System.err.println("SQLException:" + e);
			}
		}
		long end = System.currentTimeMillis();
		long execute = end - start;
		System.out.println(noConnect + " were performed in " + execute + " ms, that is " + (float) execute / (float) noConnect + " ms pr connect.");
	}

	public void testMultipleConnections() {
		try {
			props.setMaxConn(2);
			Connection c = ConnectionProvider.getConnection(props);
			Connection c2 = ConnectionProvider.getConnection(props);
			assertNotNull(c);
			assertFalse(c.isClosed());
			assertNotNull(c2);
			assertFalse(c2.isClosed());
			assertFalse(c.equals(c2));
			ConnectionProvider.returnConnection(c, null);
			ConnectionProvider.returnConnection(c2, null);
			assertFalse(c.isClosed());
			assertFalse(c2.isClosed());
			props.setMaxConn(1);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public void testTooManyConnections() {
		try {
			props.setMaxConn(10);
			List<Connection> conns = new ArrayList<Connection>();
			try {
				for (int i = 0; i < 11; i++) {
					conns.add(ConnectionProvider.getConnection(props));
				}
			} catch (NoAvailableConnectionException nace) {
				assertNotNull(nace);
			}
			for (int i = 0; i < 10; i++) {
				Connection c = conns.get(i);
				ConnectionProvider.returnConnection(c, null);
			}
			testSingleConnection();
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public void testConnectionTimeout() {
		try {
			ConnectionPoolData cpd = ConnectionProvider.getConnectionPoolData(props);
			cpd.getCleanup().CLEANUP_SLEEP = 500;
			cpd.getProps().setMaxAge(200);
			cpd.getCleanupThread().interrupt();
			Connection c = ConnectionProvider.getConnection(props);
			ConnectionProvider.returnConnection(c, null);
			Thread.sleep(750); // 500 ms cleanup sleep
			Connection c2 = ConnectionProvider.getConnection(props);
			assertFalse(c2.equals(c));
			Thread.sleep(750);// 500 ms cleanup sleep
			assertTrue(c.isClosed());
			ConnectionProvider.returnConnection(c2, null);
		} catch (Exception e) {
			fail(e.getMessage());
		}

	}

	public void testMultipleDatabases() {
		try {
			ConnectionProperties props1 = getConnectionProperties(1);
			ConnectionProperties props2 = getConnectionProperties(2);
			Connection c1 = ConnectionProvider.getConnection(props1);
			Connection c2 = ConnectionProvider.getConnection(props2);
			String db1 = c1.getMetaData().getDatabaseProductName();
			String db2 = c2.getMetaData().getDatabaseProductName();
			assertFalse(db1.equals(db2));
			ConnectionProvider.returnConnection(c2, null);
			c2 = ConnectionProvider.getConnection(props2);
			ConnectionProvider.returnConnection(c1, null);
			ConnectionProvider.returnConnection(c2, null);
		} catch (Exception e) {
			fail(e.getMessage());
		}

	}
	
	
	protected void setUp() throws Exception {
		super.setUp();
		props = getConnectionProperties(2);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
