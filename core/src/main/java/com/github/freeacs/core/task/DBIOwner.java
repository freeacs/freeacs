package com.github.freeacs.core.task;

import com.github.freeacs.common.scheduler.Task;
import com.github.freeacs.core.CoreServlet;
import com.github.freeacs.dbi.*;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * You can extend this object if you need to manipulate the state of the XAPS object, then you will not
 * cause concurrent modification problems for other threads extending the same object (as would happen 
 * if you extended DBIShare).
 * @author Morten
 *
 */
public abstract class DBIOwner implements Task {

	private final DataSource xapsCp;
	private final DataSource sysCp;

	private Users users;
	private DBI dbi;
	private Syslog syslog;
	private Identity id;

	private String taskName;
	private long launchTms;
	private boolean running = false;

	private Throwable throwable;

	public DBIOwner(String taskName, DataSource xapsCp, DataSource sysCp) throws SQLException {
		this.xapsCp = xapsCp;
		this.sysCp = sysCp;
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

	protected DataSource getSysCp() {
		return sysCp;
	}

	protected Identity getIdentity() {
		return syslog.getIdentity();
	}

	protected DataSource getXapsCp() {
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
