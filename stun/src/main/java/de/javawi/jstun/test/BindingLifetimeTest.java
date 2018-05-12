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

import de.javawi.jstun.attribute.*;
import de.javawi.jstun.header.MessageHeader;
import de.javawi.jstun.header.MessageHeaderParsingException;
import de.javawi.jstun.util.UtilityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

public class BindingLifetimeTest {
	private static Logger LOGGER = LoggerFactory.getLogger(BindingLifetimeTest.class);
	String stunServer;
	int port;
	int timeout = 300; //ms
	MappedAddress ma;
	Timer timer;
	DatagramSocket initialSocket;

	// start value for binary search - should be carefully choosen
	int upperBinarySearchLifetime = 345000; // ms
	int lowerBinarySearchLifetime = 0;
	int binarySearchLifetime = (upperBinarySearchLifetime + lowerBinarySearchLifetime) / 2;

	// lifetime value
	int lifetime = -1; // -1 means undefined.
	boolean completed = false;

	public BindingLifetimeTest(String stunServer, int port) {
		super();
		this.stunServer = stunServer;
		this.port = port;
		timer = new Timer(true);
	}

	public void test() throws UtilityException, SocketException, UnknownHostException, IOException, MessageAttributeParsingException, MessageAttributeException, MessageHeaderParsingException {
		initialSocket = new DatagramSocket();
		initialSocket.connect(InetAddress.getByName(stunServer), port);
		initialSocket.setSoTimeout(timeout);

		if (bindingCommunicationInitialSocket()) {
			return;
		}
		BindingLifetimeTask task = new BindingLifetimeTask();
		timer.schedule(task, binarySearchLifetime);
		LOGGER.debug("Timer scheduled initially: " + binarySearchLifetime + ".");
	}

	private boolean bindingCommunicationInitialSocket() throws UtilityException, IOException, MessageHeaderParsingException, MessageAttributeParsingException {
		MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
		sendMH.generateTransactionID();
		ChangeRequest changeRequest = new ChangeRequest();
		sendMH.addMessageAttribute(changeRequest);
		byte[] data = sendMH.getBytes();

		DatagramPacket send = new DatagramPacket(data, data.length, InetAddress.getByName(stunServer), port);
		initialSocket.send(send);
		LOGGER.debug("Binding Request sent.");

		MessageHeader receiveMH = new MessageHeader();
		while (!(receiveMH.equalTransactionID(sendMH))) {
			DatagramPacket receive = new DatagramPacket(new byte[200], 200);
			initialSocket.receive(receive);
			receiveMH = MessageHeader.parseHeader(receive.getData());
			receiveMH.parseAttributes(receive.getData());
		}
		ma = (MappedAddress) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.MappedAddress);
		ErrorCode ec = (ErrorCode) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
		if (ec != null) {
			LOGGER.debug("Message header contains an Errorcode message attribute.");
			return true;
		}
		if (ma == null) {
			LOGGER.debug("Response does not contain a Mapped Address message attribute.");
			return true;
		}
		return false;
	}

	public int getLifetime() {
		return lifetime;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setUpperBinarySearchLifetime(int upperBinarySearchLifetime) {
		this.upperBinarySearchLifetime = upperBinarySearchLifetime;
		binarySearchLifetime = (upperBinarySearchLifetime + lowerBinarySearchLifetime) / 2;
	}

	class BindingLifetimeTask extends TimerTask {

		public BindingLifetimeTask() {
			super();
		}

		public void run() {
			try {
				lifetimeQuery();
			} catch (Exception e) {
				LOGGER.debug("Unhandled Exception. BindLifetimeTasks stopped.");
				e.printStackTrace();
			}
		}

		public void lifetimeQuery() throws UtilityException, MessageAttributeException, MessageHeaderParsingException, MessageAttributeParsingException, IOException {
			try {
				DatagramSocket socket = new DatagramSocket();
				socket.connect(InetAddress.getByName(stunServer), port);
				socket.setSoTimeout(timeout);

				MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
				sendMH.generateTransactionID();
				ChangeRequest changeRequest = new ChangeRequest();
				ResponseAddress responseAddress = new ResponseAddress();
				responseAddress.setAddress(ma.getAddress());
				responseAddress.setPort(ma.getPort());
				sendMH.addMessageAttribute(changeRequest);
				sendMH.addMessageAttribute(responseAddress);
				byte[] data = sendMH.getBytes();

				DatagramPacket send = new DatagramPacket(data, data.length, InetAddress.getByName(stunServer), port);
				socket.send(send);
				LOGGER.debug("Binding Request sent.");

				MessageHeader receiveMH = new MessageHeader();
				while (!(receiveMH.equalTransactionID(sendMH))) {
					DatagramPacket receive = new DatagramPacket(new byte[200], 200);
					initialSocket.receive(receive);
					receiveMH = MessageHeader.parseHeader(receive.getData());
					receiveMH.parseAttributes(receive.getData());
				}
				ErrorCode ec = (ErrorCode) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
				if (ec != null) {
					LOGGER.debug("Message header contains errorcode message attribute.");
					return;
				}
				LOGGER.debug("Binding Response received.");
				if (upperBinarySearchLifetime == (lowerBinarySearchLifetime + 1)) {
					LOGGER.debug("BindingLifetimeTest completed. UDP binding lifetime: " + binarySearchLifetime + ".");
					completed = true;
					return;
				}
				lifetime = binarySearchLifetime;
				LOGGER.debug("Lifetime update: " + lifetime + ".");
				lowerBinarySearchLifetime = binarySearchLifetime;
				binarySearchLifetime = (upperBinarySearchLifetime + lowerBinarySearchLifetime) / 2;
				if (binarySearchLifetime > 0) {
					BindingLifetimeTask task = new BindingLifetimeTask();
					timer.schedule(task, binarySearchLifetime);
					LOGGER.debug("Timer scheduled: " + binarySearchLifetime + ".");
				} else {
					completed = true;
				}
			} catch (SocketTimeoutException ste) {
				LOGGER.debug("Read operation at query socket timeout.");
				if (upperBinarySearchLifetime == (lowerBinarySearchLifetime + 1)) {
					LOGGER.debug("BindingLifetimeTest completed. UDP binding lifetime: " + binarySearchLifetime + ".");
					completed = true;
					return;
				}
				upperBinarySearchLifetime = binarySearchLifetime;
				binarySearchLifetime = (upperBinarySearchLifetime + lowerBinarySearchLifetime) / 2;
				if (binarySearchLifetime > 0) {
					if (bindingCommunicationInitialSocket()) {
						return;
					}
					BindingLifetimeTask task = new BindingLifetimeTask();
					timer.schedule(task, binarySearchLifetime);
					LOGGER.debug("Timer scheduled: " + binarySearchLifetime + ".");
				} else {
					completed = true;
				}
			}
		}
	}
}
