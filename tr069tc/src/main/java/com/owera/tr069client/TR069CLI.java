package com.owera.tr069client;

public class TR069CLI {

	public static final String CLI_OPT_1 = "-t, --threads \t\t number of parallel TR069 sessions to step up\n";

	public static final String CLI_OPT_2 = "-m, --minutes \t\t minutes to run\n";

	public static final String CLI_OPT_3 = "-s, --steps \t\t number of steps\n";

	public static final String CLI_OPT_4 = "-u, --url \t\t url of the provisiong server\n";

	public static final String CLI_OPT_5 = "-r, --range \t\t range of units n-m to be provisioned\n";

	public static final String CLI_OPT_6 = "-b, --bitrate \t\t specify in Kbit, set to \"unlim\" for unlimited bitrate\n";

	public static final String CLI_OPT_7 = "-d, --download \t\t number of sec (0-sec randomized) to download\n";

	public static final String CLI_OPT_8 = "-f, --failures \t\t specify number of OK provisioning pr failure\n";

	public static final String CLI_OPT_9 = "-h, --hangups \t\t specify number of OK provisioning pr hangup\n";

	public static final String CLI_OPT_10 = "-i  --swver \t\t specify initial software version, default is 1\n";

	public static final String TR069_CLIENT_VERSION = "1.5.0";

	public static void main(String[] args) {

		if (args.length == 0) {
			System.out.println("Error: Please enter the CLI parameters required to run the test client\n");
			printHelp();
			System.exit(1);
		}

		Arguments arguments = null;

		try {
			arguments = new CLIArguments(args);
			System.out.println(arguments.toString());
		} catch (NumberFormatException ex) {
			System.out.println("Error: Invalid CLI parameter encountered\n");
			printHelp();
			System.exit(1);
		}

		TR069ClientFactory.setRange(arguments.getRange());
		TR069ClientFactory.setInitSwVer(arguments.getInitialSoftwareVersion());
		Thread t = new Thread(new TestCenter(arguments));
		t.start();
	}

	public static void printHelp() {

		StringBuilder cliGuide = new StringBuilder(100);
		cliGuide.append("Owera-TR069 Test Client v. " + TR069_CLIENT_VERSION + ":\n\n");
		cliGuide.append("CLI Options:\n\n");
		cliGuide.append(CLI_OPT_1);
		cliGuide.append(CLI_OPT_2);
		cliGuide.append(CLI_OPT_3);
		cliGuide.append(CLI_OPT_4);
		cliGuide.append(CLI_OPT_5);
		cliGuide.append(CLI_OPT_6);
		cliGuide.append(CLI_OPT_7);
		cliGuide.append(CLI_OPT_8);
		cliGuide.append(CLI_OPT_9);
		cliGuide.append(CLI_OPT_10);
		System.out.println(cliGuide.toString());
	}
}
