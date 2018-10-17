/*
 * This file is part of JSTUN.
 *
 * Copyright (c) 2005 Thomas King <king@t-king.de> - All rights
 * reserved.
 *
 * This software is licensed under either the GNU Public License (GPL),
 * or the Apache 2.0 license. Copies of both license agreements are
 * included in this distribution.
 */
package de.javawi.jstun.test;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

public class DiscoveryInfo {
  private InetAddress testIP;
  private boolean error;
  private int errorResponseCode;
  private String errorReason;
  private boolean openAccess;
  private boolean blockedUDP;
  private boolean fullCone;
  private boolean restrictedCone;
  private boolean portRestrictedCone;
  private boolean symmetric;
  private boolean symmetricUDPFirewall;
  private InetAddress publicIP;

  public DiscoveryInfo(InetAddress testIP) {
    this.testIP = testIP;
  }

  public boolean isError() {
    return error;
  }

  public void setError(int responseCode, String reason) {
    this.error = true;
    this.errorResponseCode = responseCode;
    this.errorReason = reason;
  }

  public boolean isOpenAccess() {
    return !error && openAccess;
  }

  public void setOpenAccess() {
    this.openAccess = true;
  }

  public boolean isBlockedUDP() {
    return !error && blockedUDP;
  }

  public void setBlockedUDP() {
    this.blockedUDP = true;
  }

  public boolean isFullCone() {
    return !error && fullCone;
  }

  public void setFullCone() {
    this.fullCone = true;
  }

  public boolean isPortRestrictedCone() {
    return !error && portRestrictedCone;
  }

  public void setPortRestrictedCone() {
    this.portRestrictedCone = true;
  }

  public boolean isRestrictedCone() {
    return !error && restrictedCone;
  }

  public void setRestrictedCone() {
    this.restrictedCone = true;
  }

  public boolean isSymmetric() {
    return !error && symmetric;
  }

  public void setSymmetric() {
    this.symmetric = true;
  }

  public boolean isSymmetricUDPFirewall() {
    return !error && symmetricUDPFirewall;
  }

  public void setSymmetricUDPFirewall() {
    this.symmetricUDPFirewall = true;
  }

  public InetAddress getPublicIP() {
    return publicIP;
  }

  public InetAddress getLocalIP() {
    return testIP;
  }

  public void setPublicIP(InetAddress publicIP) {
    this.publicIP = publicIP;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Network interface: ");
    try {
      sb.append(NetworkInterface.getByInetAddress(testIP).getName());
    } catch (SocketException se) {
      sb.append("unknown");
    }
    sb.append("\n");
    sb.append("Local IP address: ");
    sb.append(testIP.getHostAddress());
    sb.append("\n");
    if (error) {
      sb.append(errorReason).append(" - Responsecode: ").append(errorResponseCode);
      return sb.toString();
    }
    sb.append("Result: ");
    if (openAccess) {
      sb.append("Open access to the Internet.\n");
    }
    if (blockedUDP) {
      sb.append("Firewall blocks UDP.\n");
    }
    if (fullCone) {
      sb.append("Full Cone NAT handles connections.\n");
    }
    if (restrictedCone) {
      sb.append("Restricted Cone NAT handles connections.\n");
    }
    if (portRestrictedCone) {
      sb.append("Port restricted Cone NAT handles connections.\n");
    }
    if (symmetric) {
      sb.append("Symmetric Cone NAT handles connections.\n");
    }
    if (symmetricUDPFirewall) {
      sb.append("Symmetric UDP Firewall handles connections.\n");
    }
    if (!openAccess
        && !blockedUDP
        && !fullCone
        && !restrictedCone
        && !portRestrictedCone
        && !symmetric
        && !symmetricUDPFirewall) {
      sb.append("unkown\n");
    }
    sb.append("Public IP address: ");
    if (publicIP != null) {
      sb.append(publicIP.getHostAddress());
    } else {
      sb.append("unknown");
    }
    sb.append("\n");
    return sb.toString();
  }
}
