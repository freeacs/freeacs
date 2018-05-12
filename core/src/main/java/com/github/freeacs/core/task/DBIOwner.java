package com.github.freeacs.core.task;

import com.github.freeacs.common.db.ConnectionProperties;
import com.github.freeacs.common.db.ConnectionProvider;
import com.github.freeacs.common.db.NoAvailableConnectionException;
import com.github.freeacs.common.scheduler.Task;
import com.github.freeacs.core.CoreServlet;
import com.github.freeacs.dbi.*;

import org.slf4j.Logger;

import java.sql.SQLException;

import static com.github.freeacs.core.Properties.getMaxAge;
import static com.github.freeacs.core.Properties.getMaxConn;
import static com.github.freeacs.core.Properties.getUrl;

/**
 * You can extend this object if you need to manipulate the state of the XAPS object, then you will not
 * cause concurrent modification problems for other threads extending the same object (as would happen 
 * if you extended DBIShare).
 * @author Morten
 *
 */
public abstract class DBIOwner implements Task {

	private static ConnectionProperties xapsCp = null;
	private static ConnectionProperties sysCp = null;
	static {
		xapsCp = ConnectionProvider.getConnectionProperties(getUrl("xaps"), getMaxAge("xaps"), getMaxConn("xaps"));
		sysCp = ConnectionProvider.getConnectionProperties(getUrl("syslog"), getMaxAge("syslog"), getMaxConn("syslog"));
		if (sysCp == null)
			sysCp = xapsCp;
	}
	private Users users;
	private DBI dbi;
	private Syslog syslog;
	private Identity id;

	private String taskName;
	private long launchTms;
	private boolean running = false;

	private Throwable throwable;

	public DBIOwner(String taskName) throws SQLException, NoAvailableConnectionException {
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
