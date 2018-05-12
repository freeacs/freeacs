package com.owera.xapsws.netadmin;

import com.owera.common.db.ConnectionProperties;
import com.owera.common.db.ConnectionProvider;
import com.owera.common.util.Sleep;
import com.owera.xaps.dbi.*;
import com.owera.xapsws.impl.XAPSWS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.owera.xapsws.impl.Properties.*;

public class MessageListener implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);

	private ConnectionProperties cp;
	private Identity id;
	private DBI dbi;
	private Syslog syslog;
	private boolean started = false;
	private boolean stop = false;
	private Inbox inbox = new Inbox();
	private Sleep sleep;

	public MessageListener() {
		try {
			cp = ConnectionProvider.getConnectionProperties(getUrl("xaps"), getMaxAge("xaps"), getMaxConn("xaps"));
			Users users = new Users(cp);
			User user = users.getUnprotected(Users.USER_ADMIN);
			//		if (user == null)
			//			throw error("The user " + login.getUsername() + " is unknown");

			// At this stage we have a positivt authentication of the user
			id = new Identity(SyslogConstants.FACILITY_WEBSERVICE, XAPSWS.VERSION, user);
			syslog = new Syslog(ConnectionProvider.getConnectionProperties(getUrl("syslog"), getMaxAge("syslog"), getMaxConn("syslog")), id);
			dbi = new DBI(Integer.MAX_VALUE, cp, syslog);
			inbox.addFilter(new Message(null, Message.MTYPE_PUB_PS, null, null));
			dbi.registerInbox("publishSPVS", inbox);
			sleep = new Sleep(1000, 10000, false);
			started = true;
		} catch (Throwable t) {
			logger.error("MessageListener was unable to start because of an error, no messages will be transmitted to 3-party integrators", t);
		}
	}

	public void run() {
		while (started) {
			sleep.sleep();
			if (stop)
				return;
			List<Message> messages = inbox.getUnreadMessages();
			if (messages != null && messages.size() > 0)
				processMessages(messages);
		}
	}

	private void processMessages(List<Message> messages) {
		// TODO: Send message using a Web Service from NetAdmin - waiting for interface publication
	}

	public void stop() {
		stop = true;
		Sleep.terminateApplication();
	}
}
