package com.github.freeacs.ws.netadmin;

import com.github.freeacs.common.util.Sleep;
import com.github.freeacs.dbi.*;
import com.github.freeacs.ws.impl.ACSWS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;

public class MessageListener implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);

	private Identity id;
	private DBI dbi;
	private Syslog syslog;
	private boolean started = false;
	private boolean stop = false;
	private Inbox inbox = new Inbox();
	private Sleep sleep;

	public MessageListener(DataSource xapsDs, DataSource syslogDs) {
		try {
			Users users = new Users(xapsDs);
			User user = users.getUnprotected(Users.USER_ADMIN);
			//		if (user == null)
			//			throw error("The user " + login.getUsername() + " is unknown");

			// At this stage we have a positivt authentication of the user
			id = new Identity(SyslogConstants.FACILITY_WEBSERVICE, ACSWS.VERSION, user);
			syslog = new Syslog(syslogDs, id);
			dbi = new DBI(Integer.MAX_VALUE, xapsDs, syslog);
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
