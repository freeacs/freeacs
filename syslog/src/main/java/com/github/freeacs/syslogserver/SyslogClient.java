package com.github.freeacs.syslogserver;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SyslogClient {
  public static void send(String host, SyslogPacket receivedPacket, int defaultPort) {
    try {
      int port = defaultPort;
      String hostname = host;
      if (host.contains(":")) {
        hostname = host.substring(0, host.indexOf(':'));
        port = Integer.parseInt(host.substring(host.indexOf(':') + 1));
      }
      InetAddress address = InetAddress.getByName(hostname);
      DatagramSocket socket = new DatagramSocket();
      String msg = receivedPacket.getSyslogStr();
      if (!msg.contains("|||")) {
        msg += "|||" + receivedPacket.getAddress();
      }
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
