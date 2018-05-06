package com.owera.xaps.syslogserver;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import com.owera.common.log.Logger;
import com.owera.common.util.Sleep;

public class SyslogServer implements Runnable {

	private static DatagramSocket socket = null;
	private static Logger logger = new Logger(SyslogServer.class);
	private static boolean ok = true;
	private static Throwable throwable;
	private static boolean started;
	private static boolean pause;
	public static int SOCKET_TIMEOUT = 100;

	private static int packetCount;

	private static DatagramPacket initServer() {
		while (true) {
			logger.notice("Will try to bind server to port " + Properties.getPort());
			try {
				socket = new DatagramSocket(Properties.getPort());
				socket.setSoTimeout(SOCKET_TIMEOUT);
				socket.setReceiveBufferSize(Properties.getReceiveBufferSize() * 1024);
				logger.notice("Created a socket and bound to port " + Properties.getPort());
				byte[] log_buffer = new byte[socket.getReceiveBufferSize()];
				logger.notice("Created receive buffer, size " + log_buffer.length / 1024 + " KB");
				DatagramPacket packet = new DatagramPacket(log_buffer, log_buffer.length);
				FailoverFileReader failoverFileReader = new FailoverFileReader();
				Thread failoverFileReaderThread = new Thread(failoverFileReader);
				failoverFileReaderThread.setName("FailoverFileReader");
				failoverFileReaderThread.start();
				logger.notice("Created FailoverFileReader thread to read from any failover files created by the syslog server");
				return packet;
			} catch (Throwable t) {
				throwable = t;
				ok = false;
				if (!Sleep.isTerminated())
					logger.error("Error occured, server did not start, will try again in 60 sec", t);
				else
					break;
				try {
					Thread.sleep(60000); // Try again in 60 sec
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		return null; // unreachable code
	}

	private void initDBThreads() {
		while (true) {
			logger.notice("Will try to start Syslog2DB threads");
			try {
				int maxSyslogDBThreads = Properties.getMaxSyslogdbThreads();
				List<Syslog2DB> syslog2DBList = new ArrayList<Syslog2DB>();
				for (int i = 0; i < maxSyslogDBThreads; i++) {
					Syslog2DB syslog2DB = new Syslog2DB(i);
					syslog2DBList.add(syslog2DB);
					Thread syslog2DBThread = new Thread(syslog2DB);
					syslog2DBThread.setName("Syslog2DB-" + i);
					syslog2DBThread.start();
					logger.notice("Created a thread (Syslog2DB-" + i + ") to store syslog messages to database");
				}
				break;
			} catch (Throwable t) {
				throwable = t;
				ok = false;
				if (!Sleep.isTerminated())
					logger.error("Error occured, server did not start, will try again in 60 sec", t);
				else
					break;
				try {
					Thread.sleep(60000); // Try again in 60 sec
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void run() {
		started = true;
		DatagramPacket packet = null;
		try {
			packet = initServer();
			initDBThreads();
			logger.notice("Server startup completed - will start to receive syslog packets");
			while (true) {
				try {
					if (Sleep.isTerminated()) {
						socket.close();
						break;
					}
					if (pause) {
						Thread.sleep(1000);
						continue;
					}
					socket.receive(packet);
					packetCountInc();
					if (packetCount > SummaryLogger.getMaxMessagesPrMinute()) 
						continue;
					SyslogPacket syslogPacket = new SyslogPacket(packet);
					SyslogPackets.add(syslogPacket);
					ok = true;
				} catch (SocketTimeoutException ste) {
					ok = true;
				} catch (Throwable t) {
					throwable = t;
					ok = false;
					logger.error("Error occured, server continues", t);
				}
			}
		} catch (Throwable t) {
			throwable = t;
			ok = false;
			if (socket != null)
				socket.close();
			if (!Sleep.isTerminated())
				logger.fatal("Error occured, server did not start - no attempts to restart it", t);
		}
	}

	public static boolean isOk() {
		return ok;
	}

	public static boolean isStarted() {
		return started;
	}

	public static Throwable getThrowable() {
		return throwable;
	}

	public static boolean isPause() {
		return pause;
	}

	public static void pause(boolean newState) {
		pause = newState;
	}

	public static synchronized void packetCountInc() {
		packetCount++;
	}

	public static synchronized int resetPacketCount() {
		int tmp = packetCount;
		packetCount = 0;
		return tmp;
	}

}
