package com.github.freeacs.core.task;

import com.github.freeacs.common.scheduler.Task;
import com.github.freeacs.core.CoreServlet;
import com.github.freeacs.dbi.*;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * You can extend this object if you need to manipulate the state of the ACS object, then you will not
 * cause concurrent modification problems for other threads extending the same object (as would happen 
 * if you extended DBIShare).
 * @author Morten
 *
 */
public abstract class DBIOwner implements Task {

	private final DataSource mainDataSource;
	private final DataSource syslogDataSource;

	private Users users;
	private DBI dbi;
	private Syslog syslog;
	private Identity id;

	private String taskName;
	private long launchTms;
	private boolean running = false;

	private Throwable throwable;

	public DBIOwner(String taskName, DataSource mainDataSource, DataSource syslogDataSource) throws SQLException {
		this.mainDataSource = mainDataSource;
		this.syslogDataSource = syslogDataSource;
		this.taskName = taskName;
		if (users == null)
			users = new Users(mainDataSource);
		if (id == null)
			id = new Identity(SyslogConstants.FACILITY_CORE, CoreServlet.version, users.getUnprotected(Users.USER_ADMIN));
		if (dbi == null) {
			syslog = new Syslog(syslogDataSource, id);
			dbi = new DBI(Integer.MAX_VALUE, mainDataSource, syslog);
		}
	}

	protected ACS getLatestACS() {
		return dbi.getAcs();
	}

	protected DataSource getSyslogDataSource() {
		return syslogDataSource;
	}

	protected Identity getIdentity() {
		return syslog.getIdentity();
	}

	protected DataSource getMainDataSource() {
		return mainDataSource;
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
