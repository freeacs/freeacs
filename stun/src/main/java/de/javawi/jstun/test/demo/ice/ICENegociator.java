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

package de.javawi.jstun.test.demo.ice;

import de.javawi.jstun.attribute.MessageAttributeException;
import de.javawi.jstun.header.MessageHeaderParsingException;
import de.javawi.jstun.test.DiscoveryInfo;
import de.javawi.jstun.test.DiscoveryTest;
import de.javawi.jstun.test.demo.ice.Candidate.CandidateType;
import de.javawi.jstun.util.Address;
import de.javawi.jstun.util.UtilityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

public class ICENegociator {
	private static Logger LOGGER = LoggerFactory.getLogger(ICENegociator.class);
	// type preference must be an integer from 0 (=lowest) to 126 (=highest) (inclusive)
	private final static int LOCAL_PREFERENCE = 0;
	private final static int SERVER_REFLEXIVE_PREFERENCE = 42;
	private final static int PEER_REFLEXIVE_PREFERENCE = 84;
	private final static int RELAYED_PREFERENCE = 126;

	// component id
	private short componentId;
	private String stunServer = "jstun.javawi.de";
	private int stunPort = 3478;

	// candidates
	HashSet<Candidate> candidates;

	public ICENegociator(short componentId) {
		this.componentId = componentId;
		candidates = new HashSet<Candidate>();
	}

	public ICENegociator(String stunServer, int stunPort, short componentId) {
		this.stunServer = stunServer;
		this.stunPort = stunPort;
		this.componentId = componentId;
		candidates = new HashSet<Candidate>();
	}

	/*
	 * This method gathers candidate addresses as described in draft-ietf-mmusic-ice-12.txt Chapter 2.1
	 * Unfortunately, only the candidates of the direct attached network interfaces and server reflexive
	 * addreses are gathered. So far, no support for relayed candidates is available (because I am not
	 * aware of any STUN relay server).
	 */
	public void gatherCandidateAddresses() {
		try {
			candidates = new HashSet<Candidate>();
			Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
			while (ifaces.hasMoreElements()) {
				NetworkInterface iface = ifaces.nextElement();
				Enumeration<InetAddress> iaddresses = iface.getInetAddresses();
				while (iaddresses.hasMoreElements()) {
					InetAddress iaddress = iaddresses.nextElement();
					if (!iaddress.isLoopbackAddress() && !iaddress.isLinkLocalAddress()) {
						try {
							// add host candidate
							Candidate local = new Candidate(new Address(iaddress.getAddress()), componentId);
							candidates.add(local);
							// add server reflexive address
							DiscoveryTest test = new DiscoveryTest(iaddress, stunServer, stunPort);
							DiscoveryInfo di = test.test();
							if (di.getPublicIP() != null) {
								Candidate cand = new Candidate(new Address(di.getPublicIP().getAddress()), CandidateType.ServerReflexive, componentId, local);
								cand.setComponentId(componentId);
								candidates.add(cand);
							}
						} catch (MessageHeaderParsingException mhpe) {
							LOGGER.debug("MessageHeaderParsingException while gathering candidate addresses.");
							mhpe.printStackTrace();
						} catch (MessageAttributeException mae) {
							LOGGER.debug("MessageAttributeException while gathering candidate addresses.");
							mae.printStackTrace();
						} catch (UtilityException ue) {
							LOGGER.debug("UtilityException while gathering candidate addresses.");
							ue.printStackTrace();
						} catch (UnknownHostException uhe) {
							LOGGER.debug("MessageHeaderParsingException while gathering candidate addresses.");
							uhe.printStackTrace();
						} catch (SocketException se) {
							LOGGER.debug("SocketException while gathering candidate addresses.");
							se.printStackTrace();
						} catch (IOException ioe) {
							LOGGER.debug("IOException while gathering candidate addresses.");
							ioe.printStackTrace();
						}
					}
				}
			}
		} catch (SocketException se) {
			LOGGER.debug("SocketException while enumatering over the network cards.");
			se.printStackTrace();
		}
	}

	public void prioritizeCandidates() {
		// count number of candidate types
		int numberLocal = 0;
		int numberServerReflexive = 0;
		int numberPeerReflexive = 0;
		int numberRelayed = 0;
		// count number of candidates of a particular type
		Iterator<Candidate> iterCandidates = candidates.iterator();
		while (iterCandidates.hasNext()) {
			Candidate cand = iterCandidates.next();
			CandidateType type = cand.getCandidateType();
			if (type == CandidateType.Local)
				numberLocal++;
			else if (type == CandidateType.ServerReflexive)
				numberServerReflexive++;
			else if (type == CandidateType.PeerReflexive)
				numberPeerReflexive++;
			else if (type == CandidateType.Relayed)
				numberRelayed++;
		}
		// assign priorities
		iterCandidates = candidates.iterator();
		while (iterCandidates.hasNext()) {
			int typeValue = 0;
			int localValue = 0;
			int componentValue = 0;
			Candidate cand = iterCandidates.next();
			CandidateType type = cand.getCandidateType();
			if (type == CandidateType.Local) {
				typeValue = LOCAL_PREFERENCE;
				localValue = numberLocal--;
			} else if (type == CandidateType.ServerReflexive) {
				typeValue = SERVER_REFLEXIVE_PREFERENCE;
				localValue = numberServerReflexive--;
			} else if (type == CandidateType.PeerReflexive) {
				typeValue = PEER_REFLEXIVE_PREFERENCE;
				localValue = numberPeerReflexive--;
			} else if (type == CandidateType.Relayed) {
				typeValue = RELAYED_PREFERENCE;
				localValue = numberRelayed--;
			}
			componentValue = cand.getComponentId();
			int priority = ((2 ^ 24) * typeValue) + ((2 ^ 8) * localValue) + componentValue;
			cand.setPriority(priority);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Candidate> getSortedCandidates() {
		Vector<Candidate> sortedCandidates = new Vector<Candidate>(candidates);
		Collections.sort(sortedCandidates);
		return sortedCandidates;
	}

	@SuppressWarnings("unused")
	public static void main(String args[]) {
		ICENegociator cc = new ICENegociator((short) 1);
		// gather candidates
		cc.gatherCandidateAddresses();
		// priorize candidates
		cc.prioritizeCandidates();
		// get SortedCandidates
		List<Candidate> sortedCandidates = cc.getSortedCandidates();

		// sent sorted candidate addresses to peer over SDP
		// received sorted candidate addresses of peer over SDP
	}
}
