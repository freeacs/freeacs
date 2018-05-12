package com.owera.xaps.syslogserver;

import java.net.DatagramPacket;

public class SyslogPacket {

	private String syslogStr;

	private String address;

	private long tms;

	private boolean failoverPacket = false;

	public SyslogPacket(String syslogStr, String address, long tms) {
		this.syslogStr = syslogStr;
		this.address = address;
		this.tms = tms;
	}

	public SyslogPacket(String syslogStr, String address, long tms, boolean failoverPacket) {
		this.syslogStr = syslogStr;
		this.address = address;
		this.tms = tms;
		this.failoverPacket = failoverPacket;
	}

	public String removeSpecialChar(String s) {
		char[] charArray = s.toCharArray();
		StringBuilder sb = new StringBuilder();
		for (char c : charArray) {
			if (c >= 32 && c < 127)
				sb.append(c);
		}
		return sb.toString();
	}
	
	public SyslogPacket(DatagramPacket packet) {
		this.syslogStr = removeSpecialChar(new String(packet.getData(), 0, packet.getLength()));
		this.address = packet.getAddress().toString();
		if (this.address.startsWith("/"))
			this.address = this.address.substring(1);
		this.tms = System.currentTimeMillis();
	}

	public String getSyslogStr() {
		return syslogStr;
	}

	public String getAddress() {
		return address;
	}

	public long getTms() {
		return tms;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("#@@#");
		sb.append(tms);
		sb.append("#@@#");
		sb.append(address);
		sb.append("#@@#");
		sb.append(syslogStr);
		sb.append("#@@#");
		return sb.toString();
	}

	public boolean isFailoverPacket() {
		return failoverPacket;
	}

	public void setSyslogStr(String syslogStr) {
		this.syslogStr = syslogStr;
	}
}
