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
package de.javawi.jstun.test.demo;

import com.github.freeacs.common.util.Sleep;
import com.github.freeacs.common.util.TimestampMap;
import com.github.freeacs.stun.MessageStack;
import de.javawi.jstun.attribute.ChangeRequest;
import de.javawi.jstun.attribute.ChangedAddress;
import de.javawi.jstun.attribute.ConnectionRequestBinding;
import de.javawi.jstun.attribute.MappedAddress;
import de.javawi.jstun.attribute.MessageAttributeException;
import de.javawi.jstun.attribute.MessageAttributeInterface.MessageAttributeType;
import de.javawi.jstun.attribute.ResponseAddress;
import de.javawi.jstun.attribute.SourceAddress;
import de.javawi.jstun.attribute.UnknownAttribute;
import de.javawi.jstun.attribute.UnknownMessageAttributeException;
import de.javawi.jstun.header.MessageHeader;
import de.javawi.jstun.header.MessageHeaderInterface.MessageHeaderType;
import de.javawi.jstun.util.Address;
import de.javawi.jstun.util.UtilityException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a STUN server as described in RFC 3489. The server requires a machine that
 * is dual-homed to be functional.
 *
 * <p>CHANGE OF ORGINAL CODE BY OWERA:
 *
 * <p>This class has been modified to suit the purpose of xAPS Stun Server. The modification is
 * simply that the server will check with MessageStack to see if any STUN client should be notified.
 */
public class StunServer {
  private static boolean started;
  private static Logger logger = LoggerFactory.getLogger(StunServer.class);
  private static Counter counter = new Counter();
  private List<DatagramSocket> sockets;
  private static TimestampMap activeStunClients = new TimestampMap();

  /**
   * Inner class to handle incoming packets and react accordingly. I decided not to start a thread
   * for every received Binding Request, because the time required to receive a Binding Request,
   * parse it, generate a Binding Response and send it varies only between 2 and 4 milliseconds.
   * This amount of time is small enough so that no extra thread is needed for incoming Binding
   * Request.
   */
  class StunServerReceiverThread extends Thread {
    private DatagramSocket receiverSocket;
    private DatagramSocket changedPort;
    private DatagramSocket changedIP;
    private DatagramSocket changedPortIP;
    private Long tms;

    private boolean primaryPortIP;

    StunServerReceiverThread(DatagramSocket datagramSocket, boolean primaryPortIP) {
      this.primaryPortIP = primaryPortIP;
      this.receiverSocket = datagramSocket;
      for (DatagramSocket socket : sockets) {
        if (socket.getLocalPort() != receiverSocket.getLocalPort()
            && socket.getLocalAddress().equals(receiverSocket.getLocalAddress())) {
          changedPort = socket;
        }
        if (socket.getLocalPort() == receiverSocket.getLocalPort()
            && !socket.getLocalAddress().equals(receiverSocket.getLocalAddress())) {
          changedIP = socket;
        }
        if (socket.getLocalPort() != receiverSocket.getLocalPort()
            && !socket.getLocalAddress().equals(receiverSocket.getLocalAddress())) {
          changedPortIP = socket;
        }
      }
    }

    private DatagramPacket receiveAndKick() throws IOException {
      long startNs = System.nanoTime();
      DatagramPacket receive = new DatagramPacket(new byte[200], 200);
      receiverSocket.setSoTimeout(1000);
      if (primaryPortIP) {
        try {
          receiverSocket.receive(receive);
          if (System.currentTimeMillis() - tms > 1000) {
            tms = System.currentTimeMillis();
            DatagramPacket packet;
            while ((packet = MessageStack.pop()) != null) {
              counter.incKick();
              receiverSocket.send(packet);
            }
          }
        } catch (SocketTimeoutException ste) {
          DatagramPacket packet;
          while ((packet = MessageStack.pop()) != null) {
            receiverSocket.send(packet);
          }
          receive = null;
        }
      } else {
        try {
          receiverSocket.receive(receive);
        } catch (SocketTimeoutException ste) {
          receive = null;
        }
      }
      counter.incReceiveTime(System.nanoTime() - startNs);
      return receive;
    }

    private void processConnectionRequestBinding(
        ConnectionRequestBinding crb,
        MessageHeader sendMH,
        ResponseAddress ra,
        DatagramPacket receive)
        throws UtilityException, MessageAttributeException, IOException {
      counter.incRequestBindingConnection();
      // LOGGER.debug("Change port and ip received in Change Request attribute");
      // Source address attribute
      SourceAddress sa = new SourceAddress();
      sa.setAddress(new Address(receiverSocket.getLocalAddress().getAddress()));
      sa.setPort(receiverSocket.getLocalPort());
      sendMH.addMessageAttribute(sa);
      byte[] data = sendMH.getBytes();
      DatagramPacket send = new DatagramPacket(data, data.length);
      setDatagramAddress(ra, receive, send);
      if (logger.isDebugEnabled()) {
        logger.debug(
            "Connection binding request from "
                + send.getAddress().getHostAddress()
                + ":"
                + send.getPort());
      }

      receiverSocket.send(send);

      if (logger.isDebugEnabled()) {
        logger.debug(
            receiverSocket.getLocalAddress().getHostAddress()
                + ":"
                + receiverSocket.getLocalPort()
                + " send Binding Response to "
                + send.getAddress().getHostAddress()
                + ":"
                + send.getPort());
      }
    }

    private void processChangeRequest(
        ChangeRequest cr, MessageHeader sendMH, ResponseAddress ra, DatagramPacket receive)
        throws UtilityException, MessageAttributeException, IOException {
      if (cr.isChangePort() && !cr.isChangeIP()) {
        counter.incRequestBindingPortChange();
        if (logger.isDebugEnabled()) {
          logger.debug("Change port received in Change Request attribute");
        }
        // Source address attribute
        SourceAddress sa = new SourceAddress();
        sa.setAddress(new Address(changedPort.getLocalAddress().getAddress()));
        sa.setPort(changedPort.getLocalPort());
        sendMH.addMessageAttribute(sa);
        byte[] data = sendMH.getBytes();
        DatagramPacket send = new DatagramPacket(data, data.length);
        setDatagramAddress(ra, receive, send);
        changedPort.send(send);
        if (logger.isDebugEnabled()) {
          logger.debug(
              changedPort.getLocalAddress().getHostAddress()
                  + ":"
                  + changedPort.getLocalPort()
                  + " send Binding Response to "
                  + send.getAddress().getHostAddress()
                  + ":"
                  + send.getPort());
        }
      } else if (!cr.isChangePort() && cr.isChangeIP()) {
        counter.incRequestBindingIPChange();
        if (logger.isDebugEnabled()) {
          logger.debug("Change ip received in Change Request attribute");
        }
        // Source address attribute
        SourceAddress sa = new SourceAddress();
        sa.setAddress(new Address(changedIP.getLocalAddress().getAddress()));
        sa.setPort(changedIP.getLocalPort());
        sendMH.addMessageAttribute(sa);
        byte[] data = sendMH.getBytes();
        DatagramPacket send = new DatagramPacket(data, data.length);
        setDatagramAddress(ra, receive, send);
        changedIP.send(send);
        logger.debug(
            changedIP.getLocalAddress().getHostAddress()
                + ":"
                + changedIP.getLocalPort()
                + " send Binding Response to "
                + send.getAddress().getHostAddress()
                + ":"
                + send.getPort());
      } else if (!cr.isChangePort() && !cr.isChangeIP()) {
        counter.incRequestBindingNoChange();
        if (logger.isDebugEnabled()) {
          logger.debug("Nothing received in Change Request attribute");
        }
        // Source address attribute
        SourceAddress sa = new SourceAddress();
        sa.setAddress(new Address(receiverSocket.getLocalAddress().getAddress()));
        sa.setPort(receiverSocket.getLocalPort());
        sendMH.addMessageAttribute(sa);
        byte[] data = sendMH.getBytes();
        DatagramPacket send = new DatagramPacket(data, data.length);
        setDatagramAddress(ra, receive, send);
        receiverSocket.send(send);
        if (logger.isDebugEnabled()) {
          logger.debug(
              receiverSocket.getLocalAddress().getHostAddress()
                  + ":"
                  + receiverSocket.getLocalPort()
                  + " send Binding Response to "
                  + send.getAddress().getHostAddress()
                  + ":"
                  + send.getPort());
        }
      } else if (cr.isChangePort() && cr.isChangeIP()) {
        counter.incRequestBindingIPPortChange();
        if (logger.isDebugEnabled()) {
          logger.debug("Change port and ip received in Change Request attribute");
        }
        // Source address attribute
        SourceAddress sa = new SourceAddress();
        sa.setAddress(new Address(changedPortIP.getLocalAddress().getAddress()));
        sa.setPort(changedPortIP.getLocalPort());
        sendMH.addMessageAttribute(sa);
        byte[] data = sendMH.getBytes();
        DatagramPacket send = new DatagramPacket(data, data.length);
        setDatagramAddress(ra, receive, send);
        changedPortIP.send(send);
        if (logger.isDebugEnabled()) {
          logger.debug(
              changedPortIP.getLocalAddress().getHostAddress()
                  + ":"
                  + changedPortIP.getLocalPort()
                  + " send Binding Response to "
                  + send.getAddress().getHostAddress()
                  + ":"
                  + send.getPort());
        }
      }
    }

    private void setDatagramAddress(ResponseAddress ra, DatagramPacket receive, DatagramPacket send)
        throws UtilityException, UnknownHostException {
      if (ra != null) {
        send.setPort(ra.getPort());
        send.setAddress(ra.getAddress().getInetAddress());
      } else {
        send.setPort(receive.getPort());
        send.setAddress(receive.getAddress());
      }
    }

    public void run() {
      tms = System.currentTimeMillis();
      do {
        started = true;
        try {
          if (Sleep.isTerminated()) {
            logger.info("A Stun receiver thread shuts down");
            return;
          }
          DatagramPacket receive = receiveAndKick();
          if (receive == null) {
            counter.incIdle();
            continue; // No datagram to process
          }
          long processStart = System.currentTimeMillis();
          counter.incRequest();
          activeStunClients.putSync(
              receive.getAddress().getHostAddress() + ":" + receive.getPort(),
              System.currentTimeMillis());
          if (logger.isDebugEnabled()) {
            logger.debug(
                receiverSocket.getLocalAddress().getHostAddress()
                    + ":"
                    + receiverSocket.getLocalPort()
                    + " datagram received from "
                    + receive.getAddress().getHostAddress()
                    + ":"
                    + receive.getPort());
          }
          MessageHeader receiveMH = MessageHeader.parseHeader(receive.getData());
          try {
            receiveMH.parseAttributes(receive.getData());
            if (receiveMH.getType() == MessageHeaderType.BindingRequest) {
              counter.incRequestBinding();
              if (logger.isDebugEnabled()) {
                logger.debug(
                    receiverSocket.getLocalAddress().getHostAddress()
                        + ":"
                        + receiverSocket.getLocalPort()
                        + " Binding Request received from "
                        + receive.getAddress().getHostAddress()
                        + ":"
                        + receive.getPort());
              }
              ResponseAddress ra =
                  (ResponseAddress)
                      receiveMH.getMessageAttribute(MessageAttributeType.ResponseAddress);

              MessageHeader sendMH = new MessageHeader(MessageHeaderType.BindingResponse);
              sendMH.setTransactionID(receiveMH.getTransactionID());

              // Mapped address attribute
              MappedAddress ma = new MappedAddress();
              ma.setAddress(new Address(receive.getAddress().getAddress()));
              ma.setPort(receive.getPort());
              sendMH.addMessageAttribute(ma);
              // Changed address attribute
              ChangedAddress ca = new ChangedAddress();
              ca.setAddress(new Address(changedPortIP.getLocalAddress().getAddress()));
              ca.setPort(changedPortIP.getLocalPort());
              sendMH.addMessageAttribute(ca);
              ChangeRequest cr =
                  (ChangeRequest) receiveMH.getMessageAttribute(MessageAttributeType.ChangeRequest);
              ConnectionRequestBinding crb =
                  (ConnectionRequestBinding)
                      receiveMH.getMessageAttribute(MessageAttributeType.ConnectionRequestBinding);
              if (crb != null) {
                processConnectionRequestBinding(crb, sendMH, ra, receive);
              } else if (cr != null) {
                processChangeRequest(cr, sendMH, ra, receive);
              } else {
                counter.incMessageAttributeChangeRequest();
                throw new MessageAttributeException("Message attribute change request is not set.");
              }
            }
          } catch (UnknownMessageAttributeException umae) {
            counter.incRequestUnknown();
            umae.printStackTrace();
            // Generate Binding error response
            MessageHeader sendMH = new MessageHeader(MessageHeaderType.BindingErrorResponse);
            sendMH.setTransactionID(receiveMH.getTransactionID());

            // Unknown attributes
            UnknownAttribute ua = new UnknownAttribute();
            ua.addAttribute(umae.getType());
            sendMH.addMessageAttribute(ua);

            byte[] data = sendMH.getBytes();
            DatagramPacket send = new DatagramPacket(data, data.length);
            send.setPort(receive.getPort());
            send.setAddress(receive.getAddress());
            receiverSocket.send(send);
            if (logger.isDebugEnabled()) {
              logger.debug(
                  changedPortIP.getLocalAddress().getHostAddress()
                      + ":"
                      + changedPortIP.getLocalPort()
                      + " send Binding Error Response to "
                      + send.getAddress().getHostAddress()
                      + ":"
                      + send.getPort());
            }
          }
          counter.incProcessTimeMs(System.currentTimeMillis() - processStart);
        } catch (Throwable t) {
          counter.incError();
          logger.error("Error occurred in ReceiverThread:", t);
        }
      } while (true);
    }
  }

  public StunServer(int primaryPort, InetAddress primary, int secondaryPort, InetAddress secondary)
      throws SocketException {
    logger.info("Primary port: {}, Primary address: {}, Secondary port: {}, Secondary address: {}", primaryPort, primary.toString(), secondaryPort, secondary.toString());
    sockets = new Vector<>();
    sockets.add(new DatagramSocket(primaryPort, primary));
    sockets.add(new DatagramSocket(secondaryPort, primary));
    if (!"0.0.0.0".equals(primary.getHostAddress())
        || !"0.0.0.0".equals(secondary.getHostAddress())) {
      sockets.add(new DatagramSocket(primaryPort, secondary));
      sockets.add(new DatagramSocket(secondaryPort, secondary));
    } else {
      logger.info(
          "Not adding sockets for secondary interface 0.0.0.0, since primary interface is also 0.0.0.0");
    }
    if ("0.0.0.0".equals(secondary.getHostAddress())) {
      logger.info(
          "STUN Server has started, secondary interface uses to 0.0.0.0 - not optimal for full STUN functionality");
    } else {
      logger.info("STUN Server has started, all interfaces are operational");
    }
  }

  public void start() throws SocketException {
    int counter = 0;
    for (DatagramSocket socket : sockets) {
      socket.setReceiveBufferSize(2000);
      StunServerReceiverThread ssrt;
      ssrt = new StunServerReceiverThread(socket, counter == 0);
      ssrt.setName("StunServerReceiverThread-" + counter);
      counter++;
      ssrt.start();
    }
  }

  public void shutdown() {
    for (DatagramSocket socket : sockets) {
      logger.info("Close down a socket");
      socket.disconnect();
      socket.close();
    }
  }

  public static boolean isStarted() {
    return started;
  }

  public static Counter getCounter() {
    return counter;
  }

  public static TimestampMap getActiveStunClients() {
    return activeStunClients;
  }
}
