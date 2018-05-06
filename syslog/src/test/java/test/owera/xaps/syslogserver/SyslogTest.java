package test.owera.xaps.syslogserver;

import java.util.Random;

public class SyslogTest {

	private static Random random = new Random(System.nanoTime());

	public static void main(String[] args) {
		if (args == null || args.length == 0)
			args = new String[] { "localhost", "1", "0", "true"};
		try {
			String hostname = args[0];
			int numberOfVoipCalls = Integer.parseInt(args[1]);
			int numberOfRegClients = Integer.parseInt(args[2]);
			boolean realtime = "true".equals(args[3]);

			for (int i = 0; i < numberOfRegClients; i++) {
				Thread t = new Thread(new SipRegClient(String.format("%012d", i), hostname, realtime));
				t.start();
			}
			for (int i = 0; i < numberOfVoipCalls; i++) {
				Thread t = new Thread(new VoipCallClient(String.format("%012d", i), hostname, realtime));
				t.start();
				Thread.sleep(random.nextInt(1000));
			}
		} catch (Throwable t) {
			System.err.println("An error occurred: " + t);
		}
	}

}
