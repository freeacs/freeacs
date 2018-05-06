package com.owera.common.db;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.owera.common.log.Logger;
import com.owera.common.util.Sleep;

/**
 * The basic idea of this cleanup-class is that it will
 * check every minute if a connection is "too old". If it is,
 * it sets a mark (-1) in the allConn-map. 
 * 
 * Every time a connection is returned to the pool, they
 * have to check if this mark has been set. If it is, then the
 * connection will not be put back into the pool, but instead
 * the mark is changed (-2).
 * 
 * Then this cleanup thread can be sure that the connection is
 * not in use by anyone, and can safely close and remove it
 * from the allConn-map.
 * 
 * What does it not cover: It doesn't cover stalled connections. If
 * a connection hangs, then usually the only way to overcome that
 * is to use the method setTimeout() on the statement (java.sql.Statement).
 * That is not possible todo from this Connection-handler package anyway.
 * 
 * @author Morten
 */
public class ConnectionCleanup implements Runnable {

	private static final Logger log = new Logger();

	public int CLEANUP_SLEEP = 10 * 1000; // 10 sec.

	private ConnectionPoolData poolData;
	private Sleep sleep;

	public ConnectionCleanup(ConnectionPoolData poolData) {
		this.poolData = poolData;
		this.born = true;
		this.sleep = new Sleep(CLEANUP_SLEEP, 1000, true);
	}

	private boolean dead = false;

	private boolean born = false;

	public void run() {
		try {
			while (true) {
				try {
					sleep.sleep();
					if (Sleep.isTerminated())
						break;
					Map<Connection, Long> allConn = poolData.getAllConn();
					List<Connection> thrownOutList = new ArrayList<Connection>();
					for (Entry<Connection, Long> entry : allConn.entrySet()) {
						if (entry.getValue() > 0) {
							long ageLimit = poolData.getProps().getMaxage();
							long ageConn = System.currentTimeMillis() - entry.getValue();
							if (ageConn > ageLimit) {
								log.debug("Connection is old (" + ageConn + " ms) and will be invalidated");
								entry.setValue(-1l);
							}
						} else if (entry.getValue() == -2) {
							thrownOutList.add(entry.getKey());
						}
					}
					for (int i = 0; i < thrownOutList.size(); i++) {
						Connection c = thrownOutList.get(i);
						allConn.remove(c);
						poolData.getMetaData().incThrownOut();
						try {
							if (!c.getAutoCommit()) {
								try {
									c.commit();
								} catch (Throwable t1) {
									log.warn("Could not commit the connection, proceed to close the connection anyway");
								}
							}
							c.close();
						} catch (Throwable t) {
							log.warn("Could not close connection, will non-the-less be thrown out of the connection pool", t);
						}
					}
				} catch (Throwable t) {
					log.warn("Something happened in the run()-method:", t);
				}
			}
			Map<Connection, Long> allConn = poolData.getAllConn();
			for (Entry<Connection, Long> entry : allConn.entrySet()) {
				Connection c = entry.getKey();
				try {
					if (c != null && !c.isClosed() && !c.getAutoCommit())
						c.commit();
				} catch (Throwable t) {
					// Ignore, the server is going down anyway
				}
			}
		} finally {
			dead = true;
		}
	}

	public boolean isBorn() {
		return born;
	}

	public boolean isDead() {
		return dead;
	}
}
