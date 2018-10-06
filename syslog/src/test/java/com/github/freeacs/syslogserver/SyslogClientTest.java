package com.github.freeacs.syslogserver;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class SyslogClientTest {

  protected static Random random = new Random();

  private static SimpleDateFormat deviceTmsFormat =
      new SimpleDateFormat("MMM dd HH:mm:ss", new Locale("EN"));

  protected boolean realtime = false;
  protected boolean initiallyRealtime = false;
  private int counter = 0;

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

  protected static String makeMessage(
      boolean realtime, long timestamp, String severity, String mac, String msg) {
    String deviceTms = "Jan 1 00:00:00";
    if (!realtime) deviceTms = deviceTmsFormat.format(new Date(timestamp));
    if (severity.equals("DEBUG")) return "<135>" + deviceTms + " cpe [" + mac + "]: " + msg;
    if (severity.equals("INFO")) return "<134>" + deviceTms + " cpe [" + mac + "]: " + msg;
    if (severity.equals("NOTICE")) return "<133>" + deviceTms + " cpe [" + mac + "]: " + msg;
    if (severity.equals("WARNING")) return "<132>" + deviceTms + " cpe [" + mac + "]: " + msg;
    if (severity.equals("ERROR")) return "<131>" + deviceTms + " cpe [" + mac + "]: " + msg;
    return "<135>" + deviceTms + " cpe [" + mac + "]: " + msg;
  }

  enum TestType {
    RANDOM_MAC,
    VALID_MAC,
    VALID_MAC_WITH_SWVER,
    SINGLE_MSG;
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
    StringBuilder sb = new StringBuilder();
    sb.append("<").append(20 * 6 + severity).append(">");
    sb.append(timestamp);
    sb.append(" ");
    sb.append(hostname).append(" ");
    sb.append("syslogclient");
    sb.append("[").append(mac).append("]:");
    if (random.nextBoolean()) sb.append("A message");
    else sb.append("Another message");
    return sb.toString();
  }

  protected long sleep(long tms, long sleeptime) throws InterruptedException {
    if (realtime) Thread.sleep(sleeptime);
    if (tms > System.currentTimeMillis()) realtime = true;
    tms += sleeptime;
    return tms;
  }
}
