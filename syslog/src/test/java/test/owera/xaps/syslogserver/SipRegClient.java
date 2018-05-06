package test.owera.xaps.syslogserver;

import java.util.Random;

public class SipRegClient extends SyslogClient implements Runnable {

	private static Random random = new Random(System.nanoTime());

	private String mac;
	private String hostname;
	private long startTms;

	public SipRegClient(String mac, String hostname, boolean realtime) {
		this.mac = mac;
		this.hostname = hostname;
		this.realtime = realtime;
		this.initiallyRealtime = realtime;
		if (!realtime)
			startTms = System.currentTimeMillis() - 7l * 24l * 3600l * 1000l;
		else
			startTms = System.currentTimeMillis();
	}

	@Override
	public void run() {
		long tms = startTms;
		int state = random.nextInt(75) + 25;
		while (true) {
			try {
				if (tms > System.currentTimeMillis())
					realtime = true;
				if (!initiallyRealtime && realtime)
					return;
				String msg = null;
				if (Integer.parseInt(mac) < 12 || state >= 50)
					msg = makeMessage(realtime, tms, "NOTICE", mac, "ua0: reg ok jarlandre@owera.com: 200 OK (1 bindings)");
				else
					msg = makeMessage(realtime, tms, "ERROR", mac, "ua0: reg failed @: 903 DNS Error (0 bindings)");
				send(msg, hostname);
				state += random.nextInt(5) + 3;
				if (state > 100) {
					if (random.nextInt(10) < 1)
						state = 30;
					else
						state = 100;
				}
				if (state < 0)
					state = 0;
				tms = sleep(tms, 2 * 60000);
			} catch (Throwable t) {
				System.err.println("Error occured: " + t);
			}
		}
	}

}
