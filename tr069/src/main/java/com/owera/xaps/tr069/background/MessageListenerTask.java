package com.owera.xaps.tr069.background;

import java.util.List;

import com.owera.common.log.Logger;
import com.owera.common.scheduler.TaskDefaultImpl;
import com.owera.xaps.dbi.DBI;
import com.owera.xaps.dbi.Inbox;
import com.owera.xaps.dbi.Message;
import com.owera.xaps.dbi.SyslogConstants;
import com.owera.xaps.tr069.test.system2.TestUnitCache;

public class MessageListenerTask extends TaskDefaultImpl {

	private Logger logger = new Logger();
	private Inbox tr069ServerListenerInbox = new Inbox();

	public MessageListenerTask(String taskName, DBI dbi) {
		super(taskName);
		tr069ServerListenerInbox.addFilter(new Message(null, null, SyslogConstants.FACILITY_TR069, null));
		dbi.registerInbox("tr069ServerListener", tr069ServerListenerInbox);
	}

	@Override
	public void runImpl() throws Throwable {
		List<Message> messages = tr069ServerListenerInbox.getUnreadMessages();
		for (Message message : messages) {
			if (message.getMessageType().equals(Message.MTYPE_PUB_TR069_TEST_END) && message.getObjectType().equals(Message.OTYPE_UNIT)) {
				logger.debug("Message signals end to Test, remove TestUnit from TestUnitCahe (unitId: " + message.getObjectId() + ")");
				TestUnitCache.remove(message.getObjectId());
				message.setProcessed(true);
			}
		}
		tr069ServerListenerInbox.deleteReadMessage();
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

}
