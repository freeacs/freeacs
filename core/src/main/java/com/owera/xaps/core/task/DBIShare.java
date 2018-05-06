package com.owera.xaps.core.task;

import java.sql.SQLException;

import com.owera.common.db.ConnectionProperties;
import com.owera.common.db.ConnectionProvider;
import com.owera.common.db.NoAvailableConnectionException;
import com.owera.common.log.Logger;
import com.owera.common.scheduler.Task;
import com.owera.xaps.core.CoreServlet;
import com.owera.xaps.dbi.DBI;
import com.owera.xaps.dbi.Identity;
import com.owera.xaps.dbi.Syslog;
import com.owera.xaps.dbi.SyslogConstants;
import com.owera.xaps.dbi.Users;
import com.owera.xaps.dbi.XAPS;

/**
 * You can extend DBIShare if, and only if, you do not manipulate the contents of the xAPS object. 
 * Then you can share the same DBI object - and reduce the load on the system.
 * @author Morten
 *
 *
 */
public abstract class DBIShare implements Task {

	private static ConnectionProperties xapsCp = null;
	private static ConnectionProperties sysCp = null;
	static {
		xapsCp = ConnectionProvider.getConnectionProperties("xaps-core.properties", "db.xaps");
		sysCp = ConnectionProvider.getConnectionProperties("xaps-core.properties", "db.syslog");
		if (sysCp == null)
			sysCp = xapsCp;
	}
	private static Users users;
	private static DBI dbi;
	private static Syslog syslog;
	private static Identity id;

	@SuppressWarnings("unused")
	private Logger logger = new Logger();

	private String taskName;
	private long launchTms;
	private boolean running = false;

	private Throwable throwable;

	public DBIShare(String taskName) throws SQLException, NoAvailableConnectionException {
		this.taskName = taskName;
		if (users == null)
			users = new Users(xapsCp);
		if (id == null)
			id = new Identity(SyslogConstants.FACILITY_CORE, CoreServlet.version, users.getUnprotected(Users.USER_ADMIN));
		if (dbi == null) {
			syslog = new Syslog(sysCp, id);
			dbi = new DBI(Integer.MAX_VALUE, xapsCp, syslog);
		}
	}

	protected XAPS getLatestXAPS() {
		return dbi.getXaps();
	}

	protected ConnectionProperties getSysCp() {
		return sysCp;
	}

	protected Identity getIdentity() {
		return syslog.getIdentity();
	}

protected ConnectionProperties getXapsCp() {
		return xapsCp;
	}

	protected long getLaunchTms() {
		return launchTms;
	}

	protected Syslog getSyslog() {
		return syslog;
	}

	public void run() {
		try {
			running = true;
			runImpl();
			running = false;
		} catch (Throwable t) {
			running = false;
			throwable = t;
			getLogger().error("An error occurred", t);
		}
	}

	public boolean isRunning() {
		return running;
	}

	public abstract void runImpl() throws Exception;

	public abstract Logger getLogger();

	@Override
	public void setThisLaunchTms(long launchTms) {
		this.launchTms = launchTms;
	}

	@Override
	public String getTaskName() {
		return taskName;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public void setThrowable(Throwable throwable) {
		this.throwable = throwable;
	}

}
