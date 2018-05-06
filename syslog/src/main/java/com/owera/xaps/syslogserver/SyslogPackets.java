package com.owera.xaps.syslogserver;

import java.util.LinkedList;
import java.util.NoSuchElementException;

import com.owera.common.log.Logger;
import com.owera.common.util.Sleep;

public class SyslogPackets {

	public static class BufferCounter {

		private int failoverPackets;
		private int bufferOverflow;
		private int size;

		public BufferCounter() {

		}

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

		public int getFailoverPackets() {
			return failoverPackets;
		}

		public int getBufferOverflow() {
			return bufferOverflow;
		}

		public int getSize() {
			return size;
		}
	}

	public static LinkedList<SyslogPacket> packets = new LinkedList<SyslogPacket>();

	private static BufferCounter counter = new BufferCounter();

	private static Logger messages = new Logger("Messages");

	private static Logger logger = new Logger(SyslogPackets.class);

	//	private static FailoverFile ff = FailoverFile.getInstance();

	private static int MAX_MESSAGES = Properties.getMaxMessagesInBuffer();

	private static Object monitor = new Object();

	//	private static boolean highReceiveLoad = false;

	//	private static int highLoadCounter = 0;

	public static void add(SyslogPacket syslogPacket) {
		if (packets.size() < MAX_MESSAGES) {
			synchronized (monitor) {
				packets.add(syslogPacket);
				if (syslogPacket.isFailoverPacket())
					counter.incFailoverPackets();
			}
			//			if (highLoadCounter > 0)
			//				highLoadCounter--;
			//			if (highLoadCounter == 0 && highReceiveLoad) {
			//				String mbsStr = " (message-in-buffer: " + SyslogPackets.packets.size() + ")";
			//				logger.info("High priority on database storage, failover logging has ceased" + mbsStr);
			//				highReceiveLoad = false;
			//			}
			if (logger.isDebugEnabled())
				logger.debug("Packet added to message buffer. Buffer size is " + packets.size() + ". Packet content: " + syslogPacket.getSyslogStr());
			//		} else if (packets.size() < MAX_MESSAGES) {
			//			packets.add(syslogPacket);
			//			if (!highReceiveLoad) {
			//				String mbsStr = " (message-in-buffer: " + SyslogPackets.packets.size() + ")";
			//				logger.info("High priority on receive message, failover logging activated" + mbsStr);
			//				highReceiveLoad = true;
			//			}
			//			highLoadCounter = 1000;
		} else {
			//			ff.write(syslogPacket + "Too many messages in buffer (" + packets.size() + ").\n");
			//			if (logger.isDebugEnabled())
			//				logger.debug("Packet written to failover log. Buffer size is " + packets.size() + ". Packet content: " + syslogPacket.getSyslogStr());
			counter.incBufferOverflow();
		}
		if (messages.isDebugEnabled())
			messages.debug(syslogPacket.toString());
	}

	public static SyslogPacket get() {
		SyslogPacket packet = null;
		while (true) {
			try {
				if (packets == null)
					return null;
				synchronized (monitor) {
					packet = packets.removeFirst();
					if (packet.isFailoverPacket())
						counter.decFailoverPackets();
				}
				//				if (packets.size() == 0 && highReceiveLoad) {
				//					String mbsStr = " (message-in-buffer: " + SyslogPackets.packets.size() + ")";
				//					logger.info("High priority on database storage" + mbsStr);
				//					highReceiveLoad = false;
				//				}
				//				if (highLoadCounter > 0)
				//					highLoadCounter--;
				//				if (highLoadCounter == 0 && highReceiveLoad) {
				//					String mbsStr = " (message-in-buffer: " + SyslogPackets.packets.size() + ")";
				//					logger.info("High priority on database storage, failover logging has ceased" + mbsStr);
				//					highReceiveLoad = false;
				//				}

				break;
			} catch (NoSuchElementException nsee) {
				try {
					//					Syslog2DB.incSleep(100);
					Thread.sleep(100);
					if (Sleep.isTerminated())
						return null;
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

	//	public static boolean isHighReceiveLoad() {
	//		return highReceiveLoad;
	//	}
}
