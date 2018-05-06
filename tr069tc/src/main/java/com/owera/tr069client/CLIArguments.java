package com.owera.tr069client;

public class CLIArguments implements Arguments {

	private int numberOfThreadsPrStep;

	private int minutesToRunPrStep;

	private int numberOfSteps;

	private String provUrl;

	private int bitRate = 120000; // 120kbps

	private int download = 0;

	private boolean authenticate;

	private String[] savedArgs;

	private String range;

	private int hangupEvery;

	private int failureEvery;

	private String initialSoftwareVersion;

	public Arguments clone() {
		return new CLIArguments(savedArgs);
	}

	public CLIArguments(String[] args) {

		savedArgs = args;
		for (int i = 0; i < args.length; i = i + 2) {
			if (args[i].equals("--threads") || args[i].equals("-t"))
				numberOfThreadsPrStep = Integer.parseInt(args[i + 1]);
			else if (args[i].equals("--minutes") || args[i].equals("-m"))
				minutesToRunPrStep = Integer.parseInt(args[i + 1]);
			else if (args[i].equals("--steps") || args[i].equals("-s"))
				numberOfSteps = Integer.parseInt(args[i + 1]);
			else if (args[i].equals("--url") || args[i].equals("-u"))
				provUrl = args[i + 1];
			else if (args[i].equals("--range") || args[i].equals("-r"))
				range = args[i + 1];
			else if (args[i].equals("--bitrate") || args[i].equals("-b")) {
				String bitRateStr = args[i + 1];
				if (bitRateStr.equalsIgnoreCase("unlim"))
					bitRate = Integer.MAX_VALUE;
				else
					bitRate = Integer.parseInt(bitRateStr) * 1000;
			} else if (args[i].equals("--download") || args[i].equals("-d")) {
				download = Integer.parseInt(args[i + 1]);
			} else if (args[i].equalsIgnoreCase("--failures") || args[i].equals("-f"))
				failureEvery = Integer.parseInt(args[i + 1]);
			else if (args[i].equalsIgnoreCase("--hangups") || args[i].equals("-h"))
				hangupEvery = Integer.parseInt(args[i + 1]);
			else if (args[i].equalsIgnoreCase("--swver") || args[i].equals("-i"))
				initialSoftwareVersion = args[i + 1];
		}
	}

	public int getMinutesToRunPrStep() {
		return minutesToRunPrStep;
	}

	public void setMinutesToRun(int minutesToRunPrStep) {
		this.minutesToRunPrStep = minutesToRunPrStep;
	}

	public int getNumberOfSteps() {
		return numberOfSteps;
	}

	public void setNumberOfSteps(int numberOfSteps) {
		this.numberOfSteps = numberOfSteps;
	}

	public void setBitRate(int bitRate) {
		this.bitRate = bitRate;
	}

	public String getProvUrl() {
		return provUrl;
	}

	public void setProvUrl(String provUrl) {
		this.provUrl = provUrl;
	}

	public int getBitRate() {
		return bitRate;
	}

	public int getNumberOfThreadsPrStep() {
		return numberOfThreadsPrStep;
	}

	public void setNumberOfThreadsPrStep(int numberOfThreadsPrStep) {
		this.numberOfThreadsPrStep = numberOfThreadsPrStep;
	}

	public int getDownload() {
		return download;
	}

	public void setAuthenticate(boolean authenticate) {
		this.authenticate = authenticate;
	}

	public boolean isAuthenticate() {
		return authenticate;
	}

	public String toString() {

		StringBuilder args = new StringBuilder(100);
		args.append("\n===========================================================================\n");
		args.append("Test Client Configuration Parameters:\n\n");
		args.append(String.format("%1$22s : %2$-10d\n", "threads pr step", numberOfThreadsPrStep));
		args.append(String.format("%1$22s : %2$-10d\n", "minutes pr step", minutesToRunPrStep));
		args.append(String.format("%1$22s : %2$-10d\n", "steps", numberOfSteps));
		args.append(String.format("%1$22s : %2$-50s\n", "url", provUrl));
		args.append(String.format("%1$22s : %2$-50s\n", "range", range));
		args.append(String.format("%1$22s : %2$-10d\n", "bitrate (Kbit/s)", bitRate / 1000));
		args.append(String.format("%1$22s : %2$-10d\n", "wait-time (s)", download));
		args.append(String.format("%1$22s : %2$-10d\n", "hangup every nth time", hangupEvery));
		args.append(String.format("%1$22s : %2$-10d\n", "failure every nth time", failureEvery));
		args.append("\n===========================================================================\n");
		return args.toString();
	}

	public String getRange() {
		return range;
	}

	public int getHangupEvery() {
		return hangupEvery;
	}

	public void setHangupEvery(int hangupEvery) {
		this.hangupEvery = hangupEvery;
	}

	public int getFailureEvery() {
		return failureEvery;
	}

	public void setFailureEvery(int failureEvery) {
		this.failureEvery = failureEvery;
	}

	public String getInitialSoftwareVersion() {
		return initialSoftwareVersion;
	}

}
