package com.github.freeacs.monitor.task;

import com.github.freeacs.common.db.ConnectionProperties;
import com.github.freeacs.common.db.ConnectionProvider;
import com.github.freeacs.common.db.NoAvailableConnectionException;
import com.github.freeacs.common.scheduler.TaskDefaultImpl;
import com.github.freeacs.dbi.*;
import com.owera.xaps.dbi.*;
import com.github.freeacs.monitor.MonitorServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

import static com.github.freeacs.monitor.Properties.*;

public class TriggerNotificationHourly extends TaskDefaultImpl {

	private static Logger log = LoggerFactory.getLogger(TriggerNotificationHourly.class);
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
		ConnectionProperties sysCp = ConnectionProvider.getConnectionProperties(getUrl("syslog"), getMaxAge("syslog"), getMaxConn("syslog"));
		Syslog syslog = new Syslog(sysCp, id);
		return new DBI(Integer.MAX_VALUE, xapsCp, syslog);
	}

}
