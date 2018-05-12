package com.github.freeacs.dbi;

public class JobFlag {
	public enum JobType {
		CONFIG, KICK, RESET, RESTART, SHELL, SOFTWARE, TELNET, TR069_SCRIPT;

		public boolean requireFile() {
			if (this == SHELL || this == SOFTWARE || this == TELNET || this == TR069_SCRIPT)
				return true;
			return false;
		}

		public FileType getCorrelatedFileType() {
			if (this == SHELL)
				return FileType.SHELL_SCRIPT;
			else if (this == SOFTWARE)
				return FileType.SOFTWARE;
			else if (this == TELNET)
				return FileType.TELNET_SCRIPT;
			else if (this == TR069_SCRIPT)
				return FileType.TR069_SCRIPT;
			return null;
		}

	}

	public enum JobServiceWindow {
		DISRUPTIVE, REGULAR;
	}

	// Type
	//	public static String CONFIG = "CONFIG";
	//	public static String SCRIPT = "SCRIPT";
	//	public static String SOFTWARE = "SOFTWARE";
	//	public static String RESTART = "RESTART";
	//	public static String KICK = "KICK";
	//	public static String TELNET = "TELNET";
	//	public static String SHELL = "SHELL";
	//	public static String RESET = "RESET";

	// ServiceWindow
	//	public static String DISRUPTIVE = "DISRUPTIVE";
	//	public static String REGULAR = "REGULAR";

	//	private static String[] typeFlagArr = new String[] { CONFIG, SCRIPT, SOFTWARE, RESTART, KICK, TELNET, SHELL, RESET };
	//	private static String[] swFlagArr = new String[] { DISRUPTIVE, REGULAR };

	private JobType type;
	private JobServiceWindow serviceWindow;

	public JobFlag(String flagStr) {
		String typeStr = flagStr.split("\\|")[0];
		try {
			type = JobType.valueOf(typeStr);
		} catch (Throwable t) { // Convert from old jobtype
			if (typeStr.equals("SCRIPT"))
				type = JobType.TR069_SCRIPT;
		}
		serviceWindow = JobServiceWindow.valueOf(flagStr.split("\\|")[1]);
	}

	public JobFlag(JobType jobType, JobServiceWindow jobServiceWindow) {
		type = jobType;
		serviceWindow = jobServiceWindow;
	}

	public JobType getType() {
		return type;
	}

	public void setType(JobType type) {
		this.type = type;
	}

	public JobServiceWindow getServiceWindow() {
		return serviceWindow;
	}

	public void setServiceWindow(JobServiceWindow serviceWindow) {
		this.serviceWindow = serviceWindow;
	}

	public String toString() {
		return type + "|" + serviceWindow;
	}

}
