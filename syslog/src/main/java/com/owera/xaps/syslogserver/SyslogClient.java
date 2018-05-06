package com.owera.xaps.syslogserver;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SyslogClient {

	/**
	 * @param args
	 */
	public static void send(String host, SyslogPacket receivedPacket) {

		try {
			
			String hostname = host;
			int port = Properties.getPort();
			if (host.indexOf(":") > -1) {
				hostname = host.substring(0,host.indexOf(":"));
				port = new Integer(host.substring(host.indexOf(":")+1));
			}
			InetAddress address = InetAddress.getByName(hostname);
			DatagramSocket socket = new DatagramSocket();
			String msg = receivedPacket.getSyslogStr();
			if (msg.indexOf("|||") == -1) 
				msg += "|||"+receivedPacket.getAddress();
			byte[] message = msg.getBytes();
			DatagramPacket packet = new DatagramPacket(message, message.length);
			packet.setPort(port);
			packet.setAddress(address);
			socket.send(packet);
			socket.close();
		} catch (Throwable t) {
			System.err.println("An error occured in SyslogClient.send(): " + t);
		}
	}
}
