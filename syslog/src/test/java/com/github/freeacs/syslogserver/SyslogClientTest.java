package com.github.freeacs.syslogserver;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.*;

public class SyslogClientTest {

	protected static Random random = new Random();

	private static SimpleDateFormat deviceTmsFormat = new SimpleDateFormat("MMM dd HH:mm:ss", new Locale("EN"));

	protected boolean realtime = false;
	protected boolean initiallyRealtime = false;
	protected int counter = 0;

	public void send(String msg, String hostname) {
		try {
			InetAddress address = InetAddress.getByName(hostname);
			DatagramSocket socket = new DatagramSocket();
			byte[] message = msg.getBytes();
			DatagramPacket packet = new DatagramPacket(message, message.length);
			packet.setPort(9116);
			packet.setAddress(address);
			socket.send(packet);
			socket.close();
			System.out.println("Sent (count:" + (++counter) + "): " + msg);
		} catch (Throwable t) {
			System.err.println("An error occured: " + t);
		}
	}

	protected static String makeMessage(boolean realtime, long timestamp, String severity, String mac, String msg) {
		String deviceTms = "Jan 1 00:00:00";
		if (!realtime)
			deviceTms = deviceTmsFormat.format(new Date(timestamp));
		if (severity.equals("DEBUG"))
			return "<135>" + deviceTms + " cpe [" + mac + "]: " + msg;
		if (severity.equals("INFO"))
			return "<134>" + deviceTms + " cpe [" + mac + "]: " + msg;
		if (severity.equals("NOTICE"))
			return "<133>" + deviceTms + " cpe [" + mac + "]: " + msg;
		if (severity.equals("WARNING"))
			return "<132>" + deviceTms + " cpe [" + mac + "]: " + msg;
		if (severity.equals("ERROR"))
			return "<131>" + deviceTms + " cpe [" + mac + "]: " + msg;
		return "<135>" + deviceTms + " cpe [" + mac + "]: " + msg;
	}

	enum TestType {
		RANDOM_MAC, VALID_MAC, VALID_MAC_WITH_SWVER, SINGLE_MSG;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		try {
			// Controlling the test
			TestType testType = TestType.SINGLE_MSG;
			int MESSAGES_PR_MAC = 1000;

			System.out.println("SyslogClientTest starts");
			List<String> macs = new ArrayList<String>();
			for (int i = 0; i < 1000000; i++) {
				macs.add(String.format("%012x", i));
			}
			String hostname = "localhost";
			InetAddress address = InetAddress.getByName(hostname);
			DatagramSocket socket = new DatagramSocket();
			if (testType == TestType.SINGLE_MSG) {
				String msg = makeSingleMessage();
				byte[] message = msg.getBytes();
				DatagramPacket packet = new DatagramPacket(message, message.length);
				packet.setPort(9116);
				packet.setAddress(address);
				socket.send(packet);				
			} else {
				System.out.println(macs.size() + " MAC generated. " + (MESSAGES_PR_MAC * macs.size()) + " messages will be generated, approx 2000 msg/sec");
				for (int i = 0; i < macs.size(); i++) {
					for (int j = 0; j < MESSAGES_PR_MAC; j++) {
						//					String msg = makeSyslogMessage(i);

						String msg = makeMessage(macs.get(i));
						byte[] message = msg.getBytes();
						DatagramPacket packet = new DatagramPacket(message, message.length);
						packet.setPort(9116);
						packet.setAddress(address);
						socket.send(packet);

						msg = makeDNSMessage(macs.get(i));
						message = msg.getBytes();
						packet = new DatagramPacket(message, message.length);
						packet.setPort(9116);
						packet.setAddress(address);
						socket.send(packet);

						Thread.sleep(1);

					}
				}
			}
			System.out.println("SyslogClientTest ends");
			socket.close();
		} catch (Throwable t) {
			System.err.println("An error occured: " + t);
		}

	}

	private static String makeDNSMessage(String mac) {
		return "<133>Jan  1 00:00:00 cpe [" + mac + "]: DNS failed";
	}

	private static String makeMessage(String mac) {
		return "<133>Jan  1 00:00:00 cpe [" + mac + "]: gw: Clocked packet from IP 182.101.101.101";
	}

	private static String makeSingleMessage() {
		return "<143>Jan 15 14:36:15 Hydrogen.pingcom.net [60EB69991012]:  PLATFORM [NAS]  VOIP: UDP Msg (Size: 403) sent at (1358256975:089437) to [10.10.7.6:5060] on [10.10.0.254]:  OPTIONS sip:10.10.7.6 SIP/2.0  Via: SIP/2.0/UDP 10.10.0.254:49156;rport;branch=z9hG4bKd753ce1600734  From: <sip:4202@10.10.7.6>;tag=5dace18";
	}

	@SuppressWarnings("unused")
	private static String makeSyslogMessage(String mac) {
		int severity = random.nextInt(7);
		String timestamp = deviceTmsFormat.format(new Date());
		String hostname = "foo";
		//		String unitId = "000000-TR069TestClient-" + String.format("%012d", serialNumber);
		StringBuilder sb = new StringBuilder();
		sb.append("<" + (20 * 6 + severity) + ">");
		sb.append(timestamp);
		sb.append(" ");
		if (hostname != null)
			sb.append(hostname + " ");
		sb.append("syslogclient");
		sb.append("[" + mac + "]:");
		if (random.nextBoolean())
			sb.append("A message");
		else
			sb.append("Another message");
		return sb.toString();
	}

	protected long sleep(long tms, long sleeptime) throws InterruptedException {
		if (realtime)
			Thread.sleep(sleeptime);
		if (tms > System.currentTimeMillis())
			realtime = true;
		tms += sleeptime;
		return tms;
	}

}
