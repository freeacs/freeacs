package com.github.freeacs.monitor.task;

import com.github.freeacs.common.db.ConnectionProperties;
import com.github.freeacs.common.db.ConnectionProvider;
import com.github.freeacs.common.db.NoAvailableConnectionException;
import com.github.freeacs.common.scheduler.TaskDefaultImpl;
import com.github.freeacs.dbi.*;

import com.github.freeacs.monitor.MonitorServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;

import static com.github.freeacs.monitor.Properties.*;

public class TriggerNotificationSecondly extends TaskDefaultImpl {

	private static Logger log = LoggerFactory.getLogger(TriggerNotificationSecondly.class);
	private DBI dbi;
	private Inbox inbox = new Inbox();

	public TriggerNotificationSecondly(String taskName, DataSource xapsCp, DataSource sysCp) throws SQLException, NoAvailableConnectionException {
		super(taskName);
		log.info("TriggerNotificationTask starts...");
		dbi = initializeDBI(xapsCp, sysCp);
		inbox.addFilter(new Message(SyslogConstants.FACILITY_CORE, Message.MTYPE_PUB_TRG_REL, SyslogConstants.FACILITY_MONITOR, Message.OTYPE_UNIT_TYPE));
		dbi.registerInbox("TriggerNotificationTask", inbox);
	}

	@Override
	public void runImpl() throws Throwable {
		for (Message m : inbox.getUnreadMessages()) {
			String triggerId = m.getContent();
			String unittypeId = m.getObjectId();
			log.debug("Message of trigger release on trigger " + triggerId + " was received");
			XAPS xaps = dbi.getXaps();
			Unittype unittype = xaps.getUnittype(new Integer(unittypeId));
			Triggers triggers = unittype.getTriggers();
			Trigger trigger = triggers.getById(new Integer(triggerId));
			TriggerNotification.checkTrigger(unittype, triggers, trigger, xaps);
			inbox.markMessageAsRead(m);
		}
		inbox.deleteReadMessage();
	}

	@Override
	public Logger getLogger() {
		return log;
	}

	public static DBI initializeDBI(DataSource xapsCp, DataSource sysCp) throws SQLException, NoAvailableConnectionException {
		Users users = new Users(xapsCp);
		User user = users.getUnprotected(Users.USER_ADMIN);
		Identity id = new Identity(SyslogConstants.FACILITY_STUN, MonitorServlet.VERSION, user);
		Syslog syslog = new Syslog(sysCp, id);
		return new DBI(Integer.MAX_VALUE, xapsCp, syslog);
	}

}
