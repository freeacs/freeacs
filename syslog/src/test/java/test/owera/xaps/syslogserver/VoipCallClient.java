package test.owera.xaps.syslogserver;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class VoipCallClient extends SyslogClient implements Runnable {

	private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	private String mac;
	private String hostname;
	private long startTms;

	public VoipCallClient(String mac, String hostname, boolean realtime) {
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
		while (true) {
			try {
				if (tms > System.currentTimeMillis())
					realtime = true;
				if (!initiallyRealtime && realtime)
					return;
				String msg = makeMessage(realtime, tms, "NOTICE", mac, "ua0: session connected");
				tms += 1000;
				send(msg, hostname);
				int numberOfMOS = random.nextInt(20) + 1;
				int MOS = random.nextInt(136) + 300;
				int MOSSum = 0;
				boolean problemCall = random.nextInt(20) < 2;

				for (int i = 0; i < numberOfMOS; i++) {
					msg = makeMessage(realtime, tms, "NOTICE", mac, "MOS Report: Channel 0: MOS: " + MOS);
					send(msg, hostname);
					MOSSum += MOS;
					MOS += random.nextInt(10) - 4;
					if (problemCall && i == (numberOfMOS - 4))
						MOS -= 150;
					if (MOS > 436)
						MOS = 436;
					if (MOS < 100)
						MOS = 100;
					tms = sleep(tms, 5000);
				}
				int callMs = 5000 * numberOfMOS;
				Calendar c = Calendar.getInstance();
				c.set(Calendar.HOUR_OF_DAY, 0);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.SECOND, 0);
				c.set(Calendar.MILLISECOND, 0);
				c.add(Calendar.MILLISECOND, callMs);

				String callLength = sdf.format(c.getTime());

				int MOSAvg = MOSSum / numberOfMOS;
				// MOSAvg = 436 -> loss = 0
				// MOSAvg = 100 -> loss = 100
				int lossPercent = (int) ((436f - (float) MOSAvg) / 3.36f);
				int jitterAvg = random.nextInt(lossPercent / 10 + 1);
				int jitterMax = random.nextInt(lossPercent + 1);

				if (jitterMax <= jitterAvg)
					jitterMax = jitterAvg + 1;

				msg = makeMessage(realtime, tms, "NOTICE", mac, "QoS report for channel 0: MOS: " + MOSAvg + " Jitter (Avg: " + jitterAvg + "ms Max: " + jitterMax + "ms), Audio (Valid: " + callLength
						+ " Concealed: 571ms), loss " + lossPercent + "%");
				send(msg, hostname);
				if (!realtime)
					tms += (3600 * 1000 * 6) + random.nextInt(3600 * 1000 * 3);
			} catch (Throwable t) {
				System.err.println("Error occured: " + t);
			}
		}
	}
}
