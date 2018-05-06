package com.owera.tr069client;

import java.util.SortedMap;
import java.util.TreeMap;

public class TR069ClientFactory {

	private static int serialNumber = -1;

	private static int low;

	private static int high;

	private static int numberOfUnits;

	//	private static int rounds = 1;

	private static SortedMap<Long, TR069Client> map = new TreeMap<Long, TR069Client>();

	public static int putCounter = 0;

	private static String initSwVer = null;

	private static synchronized TR069Client getTR069Client() {
		Long tms = null;
		if (map.size() > 0)
			tms = map.firstKey();
		if (serialNumber <= high) {
			long now = System.currentTimeMillis();
			if (tms == null || (tms > now)) {
				serialNumber++;
				return new TR069Client(serialNumber - 1, initSwVer);
			}
		}
		while (tms == null) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (map.size() > 0)
				tms = map.firstKey();
		}
		TR069Client tc = map.get(tms);
		map.remove(tms);
		return tc;
	}

	//	private static synchronized TR069Client getNextTR069ClientFirstRound() {
	//		if (rounds == 1 && serialNumber <= high) {
	//			serialNumber++;
	//			return new TR069Client(serialNumber - 1);
	//		}
	//		return null;
	//	}

	public static TR069Client makeTR069Client() {
		//		if (rounds == 1 && serialNumber <= high) {
		//			TR069Client tmp = getNextTR069ClientFirstRound();
		//			if (tmp != null)
		//				return tmp;
		//		}
		//		if (rounds == 1)
		//			rounds++;
		TR069Client tr069Client = getTR069Client();
		long diff = tr069Client.getNextConnectTms() - System.currentTimeMillis();
		if (diff >= 0) {
			if (diff == 0)
				diff = 1;
			try {
				Thread.sleep(diff);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return tr069Client;

	}

	public static synchronized void finishedSession(TR069Client tr069Client) {
		if (tr069Client.getNextConnectTms() == -1) // used for hangup simulation
			return;
		putCounter++;
		while (map.get(tr069Client.getNextConnectTms()) != null) {
			tr069Client.setNextConnectTms(tr069Client.getNextConnectTms() + 1);
		}
		map.put(tr069Client.getNextConnectTms(), tr069Client);
	}

	public static void setRange(String range) {
		int dashPos = range.indexOf("-");
		low = Integer.parseInt(range.substring(0, dashPos));
		high = Integer.parseInt(range.substring(dashPos + 1));
		numberOfUnits = high - low;
		serialNumber = low;
		if (high < low)
			throw new IllegalArgumentException("The range must be n-m, where n < m");
	}

	//	public static int getRounds() {
	//		return rounds;
	//	}

	public static int getLow() {
		return low;
	}

	public static int getNumberOfUnits() {
		return numberOfUnits;
	}

	public static String getInitSwVer() {
		return initSwVer;
	}

	public static void setInitSwVer(String initSwVer) {
		TR069ClientFactory.initSwVer = initSwVer;
	}

	//	public static void setRounds(int rounds) {
	//		TR069ClientFactory.rounds = rounds;
	//	}
}
