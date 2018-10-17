package com.github.freeacs.syslogserver;

import com.github.freeacs.common.util.Sleep;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyslogPackets {
  public static List<SyslogPacket> packets = new LinkedList<>();

  private static BufferCounter counter = new BufferCounter();

  private static Logger messages = LoggerFactory.getLogger("Messages");

  private static Logger logger = LoggerFactory.getLogger(SyslogPackets.class);

  private static final Object monitor = new Object();

  public static void add(SyslogPacket syslogPacket, Properties properties) {
    if (packets.size() < properties.getMaxMessagesInBuffer()) {
      synchronized (monitor) {
        packets.add(syslogPacket);
        if (syslogPacket.isFailoverPacket()) {
          counter.incFailoverPackets();
        }
      }
      if (logger.isDebugEnabled()) {
        logger.debug(
            "Packet added to message buffer. Buffer size is "
                + packets.size()
                + ". Packet content: "
                + syslogPacket.getSyslogStr());
      }
    } else {
      counter.incBufferOverflow();
    }
    if (messages.isDebugEnabled()) {
      messages.debug(syslogPacket.toString());
    }
  }

  public static SyslogPacket get() {
    SyslogPacket packet = null;
    do {
      try {
        if (packets == null) {
          return null;
        }
        synchronized (monitor) {
          packet = packets.remove(0);
          if (packet.isFailoverPacket()) {
            counter.decFailoverPackets();
          }
        }
        break;
      } catch (IndexOutOfBoundsException ignored) {
        try {
          Thread.sleep(100);
          if (Sleep.isTerminated()) {
            return null;
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    } while (true);
    if (logger.isDebugEnabled()) {
      logger.debug("Returns a packet from buffer. Buffer size is " + packets.size() + ".");
    }
    return packet;
  }

  public static BufferCounter getCounter() {
    return counter;
  }

  public static class BufferCounter {
    private int failoverPackets;
    private int bufferOverflow;
    private int size;

    public BufferCounter() {}

    public BufferCounter(int failoverPackets, int bufferOverflow, int size) {
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
}
