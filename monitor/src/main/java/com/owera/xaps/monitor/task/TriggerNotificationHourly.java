package com.owera.xaps.monitor.task;

import java.sql.SQLException;

import com.owera.common.db.ConnectionProperties;
import com.owera.common.db.ConnectionProvider;
import com.owera.common.db.NoAvailableConnectionException;
import com.owera.common.log.Logger;
import com.owera.common.scheduler.TaskDefaultImpl;
import com.owera.xaps.dbi.DBI;
import com.owera.xaps.dbi.Identity;
import com.owera.xaps.dbi.Inbox;
import com.owera.xaps.dbi.Message;
import com.owera.xaps.dbi.Syslog;
import com.owera.xaps.dbi.SyslogConstants;
import com.owera.xaps.dbi.User;
import com.owera.xaps.dbi.Users;
import com.owera.xaps.monitor.MonitorServlet;

public class TriggerNotificationHourly extends TaskDefaultImpl {

	private Logger log = new Logger();
	private DBI dbi;
	private Inbox inbox = new Inbox();

	public TriggerNotificationHourly(String taskName, ConnectionProperties xapsCp) throws SQLException, NoAvailableConnectionException {
		super(taskName);
		log.info("TriggerNotificationRoundUpTask starts...");
		dbi = initializeDBI(xapsCp);
		inbox.addFilter(new Message(SyslogConstants.FACILITY_CORE, Message.MTYPE_PUB_TRG_REL, SyslogConstants.FACILITY_MONITOR, Message.OTYPE_UNIT_TYPE));
		dbi.registerInbox("TriggerNotificationRoundUpTask", inbox);
	}

	@Override
	public void runImpl() throws Throwable {
		TriggerNotification.checkAllTriggers(dbi);
	}

	@Override
	public Logger getLogger() {
		return log;
	}

	public static DBI initializeDBI(ConnectionProperties xapsCp) throws SQLException, NoAvailableConnectionException {
		Users users = new Users(xapsCp);
		User user = users.getUnprotected(Users.USER_ADMIN);
		Identity id = new Identity(SyslogConstants.FACILITY_STUN, MonitorServlet.VERSION, user);
		ConnectionProperties sysCp = ConnectionProvider.getConnectionProperties("xaps-monitor.properties", "db.syslog");
		Syslog syslog = new Syslog(sysCp, id);
		return new DBI(Integer.MAX_VALUE, xapsCp, syslog);
	}

}
