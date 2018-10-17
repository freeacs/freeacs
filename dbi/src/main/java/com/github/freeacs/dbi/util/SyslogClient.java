package com.github.freeacs.dbi.util;

import com.github.freeacs.dbi.Identity;
import com.github.freeacs.dbi.Syslog;
import com.github.freeacs.dbi.SyslogConstants;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyslogClient {
  private static SimpleDateFormat sdf = new SimpleDateFormat("MMM d HH:mm:ss", Locale.US);

  public static String SYSLOG_SERVER_HOST = "localhost";

  private static Logger logger = LoggerFactory.getLogger(SyslogClient.class);

  public static void info(
      String unitId, String content, int facility, String facilityVersion, String user) {
    send(SyslogConstants.SEVERITY_INFO, unitId, content, facility, facilityVersion, user);
  }

  public static void info(String unitId, String content, Syslog syslog) {
    send(SyslogConstants.SEVERITY_INFO, unitId, content, syslog);
  }

  public static void notice(
      String unitId, String content, int facility, String facilityVersion, String user) {
    send(SyslogConstants.SEVERITY_NOTICE, unitId, content, facility, facilityVersion, user);
  }

  public static void notice(String unitId, String content, Syslog syslog) {
    send(SyslogConstants.SEVERITY_NOTICE, unitId, content, syslog);
  }

  public static void warn(
      String unitId, String content, int facility, String facilityVersion, String user) {
    send(SyslogConstants.SEVERITY_WARNING, unitId, content, facility, facilityVersion, user);
  }

  public static void warn(String unitId, String content, Syslog syslog) {
    send(SyslogConstants.SEVERITY_WARNING, unitId, content, syslog);
  }

  public static void error(
      String unitId, String content, int facility, String facilityVersion, String user) {
    send(SyslogConstants.SEVERITY_ERROR, unitId, content, facility, facilityVersion, user);
  }

  public static void error(String unitId, String content, Syslog syslog) {
    send(SyslogConstants.SEVERITY_ERROR, unitId, content, syslog);
  }

  private static void send(int severity, String unitId, String content, Syslog syslog) {
    if (unitId == null) {
      logger.warn(
          "SyslogClient.send() was not executed - unitId was not set - a message was not sent to the syslog server");
      return;
    }
    int facility = syslog.getIdentity().getFacility();
    String facilityVersion = syslog.getIdentity().getFacilityVersion();
    String user = syslog.getIdentity().getUser().getUsername();
    send(severity, unitId, content, facility, facilityVersion, user);
  }

  private static void send(
      int severity,
      String unitId,
      String content,
      int facility,
      String facilityVersion,
      String user) {
    String msg =
        makeMessage(severity, new Date(), null, unitId, content, facility, facilityVersion, user);
    try {
      send(msg);
    } catch (Throwable t) {
      logger.warn("SyslogClient.send() failed - a message was not sent to the syslog server", t);
    }
  }

  public static void send(String msg) throws IOException {
    DatagramSocket socket = new DatagramSocket();
    byte[] message = msg.getBytes(StandardCharsets.UTF_8);
    DatagramPacket packet = new DatagramPacket(message, message.length);
    InetAddress address = InetAddress.getByName(SYSLOG_SERVER_HOST);
    packet.setPort(9116);
    packet.setAddress(address);
    socket.send(packet);
    socket.close();
  }

  /** Syslog Message maker methods. */
  public static String makeMessage(
      int severity, Date date, String ipAddress, String unitId, String content, Syslog syslog) {
    Identity id = syslog.getIdentity();
    return makeMessage(
        severity,
        date,
        ipAddress,
        unitId,
        content,
        id.getFacility(),
        id.getFacilityVersion(),
        id.getUser().getUsername());
  }

  public static String makeMessage(
      int severity,
      Date date,
      String ipAddress,
      String unitId,
      String content,
      int facility,
      String facilityVersion,
      String user) {
    String tmp = "";
    if (user != null) {
      tmp += "USER:" + user + " ";
    }
    if (facilityVersion != null) {
      tmp += "FCV:" + facilityVersion + " ";
    }
    if (!"".equals(tmp)) {
      content = tmp + content;
    }
    return makeMessage(severity, date, ipAddress, unitId, content, facility);
  }

  private static String makeMessage(
      int severity, Date date, String ipAddress, String unitId, String content, int facility) {
    int PRI = 8 * facility + severity;
    String tmsStr = sdf.format(date);
    StringBuilder sb = new StringBuilder();
    sb.append("<").append(PRI).append(">").append(tmsStr).append(" ");
    if (ipAddress != null) {
      sb.append(ipAddress);
    } else {
      sb.append("server");
    }
    sb.append(" UNITID [").append(unitId).append("]: ").append(content);
    return sb.toString();
  }
}
