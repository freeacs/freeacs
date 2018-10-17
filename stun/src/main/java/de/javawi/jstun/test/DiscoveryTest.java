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

import de.javawi.jstun.attribute.ChangeRequest;
import de.javawi.jstun.attribute.ChangedAddress;
import de.javawi.jstun.attribute.ErrorCode;
import de.javawi.jstun.attribute.MappedAddress;
import de.javawi.jstun.attribute.MessageAttribute;
import de.javawi.jstun.attribute.MessageAttributeException;
import de.javawi.jstun.attribute.MessageAttributeParsingException;
import de.javawi.jstun.header.MessageHeader;
import de.javawi.jstun.header.MessageHeaderParsingException;
import de.javawi.jstun.util.UtilityException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscoveryTest {
  private static Logger LOGGER = LoggerFactory.getLogger(DiscoveryTest.class);
  InetAddress iaddress;
  String stunServer;
  int port;
  /** Ms. */
  int timeoutInitValue = 300;

  MappedAddress ma;
  ChangedAddress ca;
  boolean nodeNatted = true;
  DatagramSocket socketTest1;
  DiscoveryInfo di;

  public DiscoveryTest(InetAddress iaddress, String stunServer, int port) {
    this.iaddress = iaddress;
    this.stunServer = stunServer;
    this.port = port;
  }

  public DiscoveryInfo test()
      throws UtilityException, SocketException, UnknownHostException, IOException,
          MessageAttributeParsingException, MessageAttributeException,
          MessageHeaderParsingException {
    ma = null;
    ca = null;
    nodeNatted = true;
    socketTest1 = null;
    di = new DiscoveryInfo(iaddress);

    if (test1() && test2() && test1Redo()) {
      test3();
    }

    socketTest1.close();

    return di;
  }

  private boolean test1()
      throws UtilityException, SocketException, UnknownHostException, IOException,
          MessageAttributeParsingException, MessageHeaderParsingException {
    int timeSinceFirstTransmission = 0;
    int timeout = timeoutInitValue;
    do {
      try {
        // Test 1 including response
        socketTest1 = new DatagramSocket(new InetSocketAddress(iaddress, 0));
        socketTest1.setReuseAddress(true);
        socketTest1.connect(InetAddress.getByName(stunServer), port);
        socketTest1.setSoTimeout(timeout);

        MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
        sendMH.generateTransactionID();

        ChangeRequest changeRequest = new ChangeRequest();
        sendMH.addMessageAttribute(changeRequest);

        byte[] data = sendMH.getBytes();
        DatagramPacket send = new DatagramPacket(data, data.length);
        socketTest1.send(send);
        LOGGER.debug("Test 1: Binding Request sent.");

        MessageHeader receiveMH = new MessageHeader();
        while (!receiveMH.equalTransactionID(sendMH)) {
          DatagramPacket receive = new DatagramPacket(new byte[200], 200);
          socketTest1.receive(receive);
          receiveMH = MessageHeader.parseHeader(receive.getData());
          receiveMH.parseAttributes(receive.getData());
        }

        ma =
            (MappedAddress)
                receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.MappedAddress);
        ca =
            (ChangedAddress)
                receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ChangedAddress);
        ErrorCode ec =
            (ErrorCode)
                receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
        if (ec != null) {
          di.setError(ec.getResponseCode(), ec.getReason());
          LOGGER.debug("Message header contains an Errorcode message attribute.");
          return false;
        }
        if (ma == null || ca == null) {
          di.setError(
              700,
              "The server is sending an incomplete response (Mapped Address and Changed Address message attributes are missing). The client should not retry.");
          LOGGER.debug(
              "Response does not contain a Mapped Address or Changed Address message attribute.");
          return false;
        } else {
          di.setPublicIP(ma.getAddress().getInetAddress());
          if (ma.getPort() == socketTest1.getLocalPort()
              && ma.getAddress().getInetAddress().equals(socketTest1.getLocalAddress())) {
            LOGGER.debug("Node is not natted.");
            nodeNatted = false;
          } else {
            LOGGER.debug("Node is natted.");
          }
          return true;
        }
      } catch (SocketTimeoutException ste) {
        if (timeSinceFirstTransmission < 7900) {
          LOGGER.debug("Test 1: Socket timeout while receiving the response.");
          timeSinceFirstTransmission += timeout;
          int timeoutAddValue = timeSinceFirstTransmission * 2;
          if (timeoutAddValue > 1600) {
            timeoutAddValue = 1600;
          }
          timeout = timeoutAddValue;
        } else {
          // node is not capable of udp communication
          LOGGER.debug(
              "Test 1: Socket timeout while receiving the response. Maximum retry limit exceed. Give up.");
          di.setBlockedUDP();
          LOGGER.debug("Node is not capable of UDP communication.");
          return false;
        }
      }
    } while (true);
  }

  private boolean test2()
      throws UtilityException, SocketException, UnknownHostException, IOException,
          MessageAttributeParsingException, MessageAttributeException,
          MessageHeaderParsingException {
    int timeSinceFirstTransmission = 0;
    int timeout = timeoutInitValue;
    do {
      try {
        // Test 2 including response
        DatagramSocket sendSocket = new DatagramSocket(new InetSocketAddress(iaddress, 0));
        sendSocket.connect(InetAddress.getByName(stunServer), port);
        sendSocket.setSoTimeout(timeout);

        MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
        sendMH.generateTransactionID();

        ChangeRequest changeRequest = new ChangeRequest();
        changeRequest.setChangeIP();
        changeRequest.setChangePort();
        sendMH.addMessageAttribute(changeRequest);

        byte[] data = sendMH.getBytes();
        DatagramPacket send = new DatagramPacket(data, data.length);
        sendSocket.send(send);
        LOGGER.debug("Test 2: Binding Request sent.");

        int localPort = sendSocket.getLocalPort();
        InetAddress localAddress = sendSocket.getLocalAddress();

        sendSocket.close();

        DatagramSocket receiveSocket = new DatagramSocket(localPort, localAddress);
        receiveSocket.connect(ca.getAddress().getInetAddress(), ca.getPort());
        receiveSocket.setSoTimeout(timeout);

        MessageHeader receiveMH = new MessageHeader();
        while (!receiveMH.equalTransactionID(sendMH)) {
          DatagramPacket receive = new DatagramPacket(new byte[200], 200);
          receiveSocket.receive(receive);
          receiveMH = MessageHeader.parseHeader(receive.getData());
          receiveMH.parseAttributes(receive.getData());
        }
        ErrorCode ec =
            (ErrorCode)
                receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
        if (ec != null) {
          di.setError(ec.getResponseCode(), ec.getReason());
          LOGGER.debug("Message header contains an Errorcode message attribute.");
          return false;
        }
        if (!nodeNatted) {
          di.setOpenAccess();
          LOGGER.debug(
              "Node has open access to the Internet (or, at least the node is behind a full-cone NAT without translation).");
        } else {
          di.setFullCone();
          LOGGER.debug("Node is behind a full-cone NAT.");
        }
        return false;
      } catch (SocketTimeoutException ste) {
        if (timeSinceFirstTransmission < 7900) {
          LOGGER.debug("Test 2: Socket timeout while receiving the response.");
          timeSinceFirstTransmission += timeout;
          int timeoutAddValue = timeSinceFirstTransmission * 2;
          if (timeoutAddValue > 1600) {
            timeoutAddValue = 1600;
          }
          timeout = timeoutAddValue;
        } else {
          LOGGER.debug(
              "Test 2: Socket timeout while receiving the response. Maximum retry limit exceed. Give up.");
          if (!nodeNatted) {
            di.setSymmetricUDPFirewall();
            LOGGER.debug("Node is behind a symmetric UDP firewall.");
            return false;
          } else {
            // not is natted
            // redo test 1 with address and port as offered in the changed-address message attribute
            return true;
          }
        }
      }
    } while (true);
  }

  private boolean test1Redo()
      throws UtilityException, SocketException, UnknownHostException, IOException,
          MessageAttributeParsingException, MessageHeaderParsingException {
    int timeSinceFirstTransmission = 0;
    int timeout = timeoutInitValue;
    do {
      // redo test 1 with address and port as offered in the changed-address message attribute
      try {
        // Test 1 with changed port and address values
        socketTest1.connect(ca.getAddress().getInetAddress(), ca.getPort());
        socketTest1.setSoTimeout(timeout);

        MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
        sendMH.generateTransactionID();

        ChangeRequest changeRequest = new ChangeRequest();
        sendMH.addMessageAttribute(changeRequest);

        byte[] data = sendMH.getBytes();
        DatagramPacket send = new DatagramPacket(data, data.length);
        socketTest1.send(send);
        LOGGER.debug("Test 1 redo with changed address: Binding Request sent.");

        MessageHeader receiveMH = new MessageHeader();
        while (!receiveMH.equalTransactionID(sendMH)) {
          DatagramPacket receive = new DatagramPacket(new byte[200], 200);
          socketTest1.receive(receive);
          receiveMH = MessageHeader.parseHeader(receive.getData());
          receiveMH.parseAttributes(receive.getData());
        }
        MappedAddress ma2 =
            (MappedAddress)
                receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.MappedAddress);
        ErrorCode ec =
            (ErrorCode)
                receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
        if (ec != null) {
          di.setError(ec.getResponseCode(), ec.getReason());
          LOGGER.debug("Message header contains an Errorcode message attribute.");
          return false;
        }
        if (ma2 == null) {
          di.setError(
              700,
              "The server is sending an incomplete response (Mapped Address message attribute is missing). The client should not retry.");
          LOGGER.debug("Response does not contain a Mapped Address message attribute.");
          return false;
        } else if (ma.getPort() != ma2.getPort()
            || !ma.getAddress().getInetAddress().equals(ma2.getAddress().getInetAddress())) {
          di.setSymmetric();
          LOGGER.debug("Node is behind a symmetric NAT.");
          return false;
        }
        return true;
      } catch (SocketTimeoutException ste2) {
        if (timeSinceFirstTransmission < 7900) {
          LOGGER.debug(
              "Test 1 redo with changed address: Socket timeout while receiving the response.");
          timeSinceFirstTransmission += timeout;
          int timeoutAddValue = timeSinceFirstTransmission * 2;
          if (timeoutAddValue > 1600) {
            timeoutAddValue = 1600;
          }
          timeout = timeoutAddValue;
        } else {
          LOGGER.debug(
              "Test 1 redo with changed address: Socket timeout while receiving the response.  Maximum retry limit exceed. Give up.");
          return false;
        }
      }
    } while (true);
  }

  private void test3()
      throws UtilityException, SocketException, UnknownHostException, IOException,
          MessageAttributeParsingException, MessageAttributeException,
          MessageHeaderParsingException {
    int timeSinceFirstTransmission = 0;
    int timeout = timeoutInitValue;
    do {
      try {
        // Test 3 including response
        DatagramSocket sendSocket = new DatagramSocket(new InetSocketAddress(iaddress, 0));
        sendSocket.connect(InetAddress.getByName(stunServer), port);
        sendSocket.setSoTimeout(timeout);

        MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
        sendMH.generateTransactionID();

        ChangeRequest changeRequest = new ChangeRequest();
        changeRequest.setChangePort();
        sendMH.addMessageAttribute(changeRequest);

        byte[] data = sendMH.getBytes();
        DatagramPacket send = new DatagramPacket(data, data.length);
        sendSocket.send(send);
        LOGGER.debug("Test 3: Binding Request sent.");

        int localPort = sendSocket.getLocalPort();
        InetAddress localAddress = sendSocket.getLocalAddress();

        sendSocket.close();

        DatagramSocket receiveSocket = new DatagramSocket(localPort, localAddress);
        receiveSocket.connect(InetAddress.getByName(stunServer), ca.getPort());
        receiveSocket.setSoTimeout(timeout);

        MessageHeader receiveMH = new MessageHeader();
        while (!receiveMH.equalTransactionID(sendMH)) {
          DatagramPacket receive = new DatagramPacket(new byte[200], 200);
          receiveSocket.receive(receive);
          receiveMH = MessageHeader.parseHeader(receive.getData());
          receiveMH.parseAttributes(receive.getData());
        }
        ErrorCode ec =
            (ErrorCode)
                receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
        if (ec != null) {
          di.setError(ec.getResponseCode(), ec.getReason());
          LOGGER.debug("Message header contains an Errorcode message attribute.");
          return;
        }
        if (nodeNatted) {
          di.setRestrictedCone();
          LOGGER.debug("Node is behind a restricted NAT.");
          return;
        }
      } catch (SocketTimeoutException ste) {
        if (timeSinceFirstTransmission < 7900) {
          LOGGER.debug("Test 3: Socket timeout while receiving the response.");
          timeSinceFirstTransmission += timeout;
          int timeoutAddValue = timeSinceFirstTransmission * 2;
          if (timeoutAddValue > 1600) {
            timeoutAddValue = 1600;
          }
          timeout = timeoutAddValue;
        } else {
          LOGGER.debug(
              "Test 3: Socket timeout while receiving the response. Maximum retry limit exceed. Give up.");
          di.setPortRestrictedCone();
          LOGGER.debug("Node is behind a port restricted NAT.");
          return;
        }
      }
    } while (true);
  }
}
