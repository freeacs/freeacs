package com.github.freeacs.syslogserver;

import com.github.freeacs.common.util.Sleep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class SyslogPackets {

  public static class BufferCounter {

    private int failoverPackets;
    private int bufferOverflow;
    private int size;

    public BufferCounter() {}

    public BufferCounter(int failoverPackets, int bufferOverflow, int size) {
      super();
      this.failoverPackets = failoverPackets;
      this.bufferOverflow = bufferOverflow;
      this.size = size;
    }

    public synchronized void incFailoverPackets() {
      failoverPackets++;
    }

    public synchronized void decFailoverPackets() {
      failoverPackets--;
    }

    public synchronized void incBufferOverflow() {
      bufferOverflow++;
    }

    public synchronized BufferCounter resetCounters() {
      BufferCounter c = new BufferCounter(failoverPackets, bufferOverflow, packets.size());
      this.failoverPackets = 0;
      this.bufferOverflow = 0;
      this.size = 0;
      return c;
    }

    public int getBufferOverflow() {
      return bufferOverflow;
    }

    public int getSize() {
      return size;
    }
  }

  public static List<SyslogPacket> packets = new LinkedList<>();

  private static BufferCounter counter = new BufferCounter();

  private static Logger messages = LoggerFactory.getLogger("Messages");

  private static Logger logger = LoggerFactory.getLogger(SyslogPackets.class);

  private static int MAX_MESSAGES = Properties.MAX_MESSAGES_IN_BUFFER;

  private static final Object monitor = new Object();

  public static void add(SyslogPacket syslogPacket) {
    if (packets.size() < MAX_MESSAGES) {
      synchronized (monitor) {
        packets.add(syslogPacket);
        if (syslogPacket.isFailoverPacket()) counter.incFailoverPackets();
      }
      if (logger.isDebugEnabled())
        logger.debug(
            "Packet added to message buffer. Buffer size is "
                + packets.size()
                + ". Packet content: "
                + syslogPacket.getSyslogStr());
    } else {
      counter.incBufferOverflow();
    }
    if (messages.isDebugEnabled()) messages.debug(syslogPacket.toString());
  }

  public static SyslogPacket get() {
    SyslogPacket packet = null;
    while (true) {
      try {
        if (packets == null) return null;
        synchronized (monitor) {
          packet = packets.remove(0);
          if (packet.isFailoverPacket()) counter.decFailoverPackets();
        }
        break;
      } catch (NoSuchElementException nsee) {
        try {
          Thread.sleep(100);
          if (Sleep.isTerminated()) return null;
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
    if (logger.isDebugEnabled()) {
      logger.debug("Returns a packet from buffer. Buffer size is " + packets.size() + ".");
    }
    return packet;
  }

  public static BufferCounter getCounter() {
    return counter;
  }
}
