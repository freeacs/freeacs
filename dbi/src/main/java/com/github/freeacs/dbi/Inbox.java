package com.github.freeacs.dbi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Inbox {
  private static Logger logger = LoggerFactory.getLogger(Inbox.class);
  private Map<Integer, Message> map = new TreeMap<>();
  private List<Message> filters = new ArrayList<>();

  public void addFilter(Message m) {
    filters.add(m);
  }

  /**
   * Supports the following filters: o) Sender o) MessageType o) ObjectType o) Receiver Setting a
   * field to NULL is like matching every message on that field Negative matches is not supported
   */
  public synchronized void addToInbox(Message m) {
    for (Message filter : filters) {
      if ((filter.getSender() != null && !filter.getSender().equals(m.getSender()))
          || (filter.getMessageType() != null
              && !filter.getMessageType().equals(m.getMessageType()))
          || (filter.getObjectType() != null && !filter.getObjectType().equals(m.getObjectType()))
          || (filter.getReceiver() != null && !filter.getReceiver().equals(m.getReceiver()))) {
        continue;
      }
      if (logger.isDebugEnabled()) {
        logger.debug("Message: [" + m + "] was added to an inbox");
      }
      map.put(m.getId(), m);
    }
  }

  public synchronized List<Message> getUnreadMessages() {
    List<Message> list = new ArrayList<>();
    for (Message m : map.values()) {
      if (!m.isProcessed()) {
        list.add(m);
      }
    }
    return list;
  }

  public synchronized void markMessageAsRead(Message m) {
    m.setProcessed(true);
  }

  public synchronized List<Message> getAllMessages() {
    return new ArrayList<>(map.values());
  }

  public synchronized void deleteReadMessage() {
    Iterator<Integer> iterator = map.keySet().iterator();
    while (iterator.hasNext()) {
      Message m = map.get(iterator.next());
      if (m.isProcessed()) {
        iterator.remove();
      }
    }
  }
}
