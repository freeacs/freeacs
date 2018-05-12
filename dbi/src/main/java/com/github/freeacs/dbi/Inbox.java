package com.github.freeacs.dbi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class Inbox {

	private static Logger logger = LoggerFactory.getLogger(Inbox.class);
	private Map<Integer, Message> map = new TreeMap<Integer, Message>();
	private List<Message> filters = new ArrayList<Message>();

	public void addFilter(Message m) {
		filters.add(m);
	}

	/**
	 *  Supports the following filters:
	 *  o) Sender
	 *  o) MessageType
	 *  o) ObjectType
	 *  o) Receiver
	 *  Setting a field to NULL is like matching every message on that field
	 *  Negative matches is not supported
	 *  
	 */
	public synchronized boolean addToInbox(Message m) {
		boolean added = false;
		for (Message filter : filters) {
			if (filter.getSender() != null && !filter.getSender().equals(m.getSender()))
				continue;
			if (filter.getMessageType() != null && !filter.getMessageType().equals(m.getMessageType()))
				continue;
			if (filter.getObjectType() != null && !filter.getObjectType().equals(m.getObjectType()))
				continue;
			if (filter.getReceiver() != null && !filter.getReceiver().equals(m.getReceiver()))
				continue;
			added = true;
			if (logger.isDebugEnabled()) {
				logger.debug("Message: [" + m + "] was added to an inbox");
			}
			map.put(m.getId(), m);
		}
		return added;
	}

	public synchronized List<Message> getUnreadMessages() {
		List<Message> list = new ArrayList<Message>();
		for (Message m : map.values()) {
			if (!m.isProcessed())
				list.add(m);
		}
		return list;
	}

	public synchronized void markMessageAsRead(Message m) {
		m.setProcessed(true);
	}

	public synchronized List<Message> getAllMessages() {
		List<Message> list = new ArrayList<Message>();
		for (Message m : map.values()) {
			list.add(m);
		}
		return list;
	}

	public synchronized void deleteReadMessage() {
		Iterator<Integer> iterator = map.keySet().iterator();
		while (iterator.hasNext()) {
			Message m = map.get(iterator.next());
			if (m.isProcessed())
				iterator.remove();
		}
	}
}
