package test.owera.xaps.syslogserver;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import com.owera.common.db.ConnectionProperties;
import com.owera.common.util.PropertyReader;
import com.owera.xaps.syslogserver.Properties;

public class SyslogClient {

	protected static Random random = new Random();

	private static SimpleDateFormat deviceTmsFormat = new SimpleDateFormat("MMM dd HH:mm:ss", new Locale("EN"));

	protected boolean realtime = false;
	protected boolean initiallyRealtime = false;
	protected int counter = 0;

	public static ConnectionProperties getConnectionProperties(String type) {
		ConnectionProperties props = new ConnectionProperties();
		PropertyReader pr = new PropertyReader("xaps-syslog.properties");
		String db = pr.getProperty(type);
		String equalDb = pr.getProperty(db);
		if (equalDb != null)
			db = equalDb;
		props.setUrl(db.substring(db.indexOf("@") + 1));
		props.setUser(db.substring(0, db.indexOf("/")));
		props.setPassword(db.substring(db.indexOf("/") + 1, db.indexOf("@")));
		props.setMaxAge(600000);
		props.setMaxConn(Properties.getMaxSyslogdbThreads() * 3 + 3);
		if (props.getUrl().contains("mysql"))
			props.setDriver("com.mysql.jdbc.Driver");
		if (props.getUrl().contains("oracle"))
			props.setDriver("oracle.jdbc.driver.OracleDriver");
		return props;
	}

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

	protected static String makeMessage(boolean realtime, long timestamp, String severity, String mac, String msg) {
		String deviceTms = "Jan 1 00:00:00";
		if (!realtime)
			deviceTms = deviceTmsFormat.format(new Date(timestamp));
		if (severity.equals("DEBUG"))
			return "<135>" + deviceTms + " cpe [" + mac + "]: " + msg;
		if (severity.equals("INFO"))
			return "<134>" + deviceTms + " cpe [" + mac + "]: " + msg;
		if (severity.equals("NOTICE"))
			return "<133>" + deviceTms + " cpe [" + mac + "]: " + msg;
		if (severity.equals("WARNING"))
			return "<132>" + deviceTms + " cpe [" + mac + "]: " + msg;
		if (severity.equals("ERROR"))
			return "<131>" + deviceTms + " cpe [" + mac + "]: " + msg;
		return "<135>" + deviceTms + " cpe [" + mac + "]: " + msg;
	}

	enum TestType {
		RANDOM_MAC, VALID_MAC, VALID_MAC_WITH_SWVER, SINGLE_MSG;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		try {
			// Controlling the test
			TestType testType = TestType.SINGLE_MSG;
			int MESSAGES_PR_MAC = 1000;

			System.out.println("SyslogClient starts");
			List<String> macs = new ArrayList<String>();

//			if (testType == TestType.VALID_MAC || testType == TestType.VALID_MAC_WITH_SWVER) {
//				XAPSInitializer xapsInit = new XAPSInitializer("localhost");
//				XAPS xaps = xapsInit.getXaps();
//				ConnectionProperties xapsCp = getConnectionProperties("db.xaps");
//				Connection c = ConnectionProvider.getConnection(xapsCp);
//				for (Unittype ut : xaps.getUnittypes().getUnittypes()) {
//					Integer macUtpId = ut.getUnittypeParameters().getByName(SystemParameters.SERIAL_NUMBER).getId();
//					Integer swUtpId = ut.getUnittypeParameters().getByName(SystemParameters.SOFTWARE_VERSION).getId();
//					Statement s = c.createStatement();
//					ResultSet rs = s.executeQuery("SELECT value, unit_id FROM unit_param WHERE unit_type_param_id = " + macUtpId);
//					while (rs.next()) {
//						String mac = rs.getString(1);
//						String unitId = rs.getString(2);
//						if (mac == null || mac.trim().equals(""))
//							continue;
//						if (testType == TestType.VALID_MAC) {
//							macs.add(mac);
//						} else {
//							s = c.createStatement();
//							rs = s.executeQuery("SELECT value FROM unit_param WHERE unit_type_param_id = " + swUtpId + " AND unit_id = '" + unitId + "'");
//							if (rs.next()) {
//								String swv = rs.getString(1);
//								if (swv == null || swv.trim().equals(""))
//									continue;
//								macs.add(mac);
//							}
//						}
//					}
//				}
//			} else if (testType == TestType.RANDOM_MAC) {
				for (int i = 0; i < 1000000; i++)
					macs.add(String.format("%012x", i));

			//			}

			String hostname = "localhost";
			InetAddress address = InetAddress.getByName(hostname);
			DatagramSocket socket = new DatagramSocket();
			if (testType == TestType.SINGLE_MSG) {
				String msg = makeSingleMessage();
				byte[] message = msg.getBytes();
				DatagramPacket packet = new DatagramPacket(message, message.length);
				packet.setPort(9116);
				packet.setAddress(address);
				socket.send(packet);				
			} else {
				System.out.println(macs.size() + " MAC generated. " + (MESSAGES_PR_MAC * macs.size()) + " messages will be generated, approx 2000 msg/sec");
				for (int i = 0; i < macs.size(); i++) {
					for (int j = 0; j < MESSAGES_PR_MAC; j++) {
						//					String msg = makeSyslogMessage(i);

						String msg = makeMessage(macs.get(i));
						byte[] message = msg.getBytes();
						DatagramPacket packet = new DatagramPacket(message, message.length);
						packet.setPort(9116);
						packet.setAddress(address);
						socket.send(packet);

						msg = makeDNSMessage(macs.get(i));
						message = msg.getBytes();
						packet = new DatagramPacket(message, message.length);
						packet.setPort(9116);
						packet.setAddress(address);
						socket.send(packet);

						Thread.sleep(1);

					}
				}
			}
			System.out.println("SyslogClient ends");
			socket.close();
		} catch (Throwable t) {
			System.err.println("An error occured: " + t);
		}

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
		//		String unitId = "000000-TR069TestClient-" + String.format("%012d", serialNumber);
		StringBuilder sb = new StringBuilder();
		sb.append("<" + (20 * 6 + severity) + ">");
		sb.append(timestamp);
		sb.append(" ");
		if (hostname != null)
			sb.append(hostname + " ");
		sb.append("syslogclient");
		sb.append("[" + mac + "]:");
		if (random.nextBoolean())
			sb.append("A message");
		else
			sb.append("Another message");
		return sb.toString();
	}

	protected long sleep(long tms, long sleeptime) throws InterruptedException {
		if (realtime)
			Thread.sleep(sleeptime);
		if (tms > System.currentTimeMillis())
			realtime = true;
		tms += sleeptime;
		return tms;
	}

}
