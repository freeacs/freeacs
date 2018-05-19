package com.github.freeacs.tr069.methods;

import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.xml.Body;

public class DOreq extends Body {

	private static final String START = "\t\t<cwmp:Download xmlns:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n";

	private static final String COMMAND_KEY_START = "\t\t\t<CommandKey>";

	//	private static final String COMMAND = "Download_To_CPE";

	private static final String COMMAND_KEY_END = "</CommandKey>\n";

	private static final String FILE_TYPE_START = "\t\t\t<FileType>";

	public static final String FILE_TYPE_FIRMWARE = "1 Firmware Upgrade Image";

	public static final String FILE_TYPE_CONFIG = "3 Vendor Configuration File";

	private static final String FILE_TYPE_END = "</FileType>\n";

	private static final String URL_START = "\t\t\t<URL>";

	private static final String URL_END = "</URL>\n";

	private static final String USERNAME_START = "\t\t\t<Username>";

	private static final String USERNAME_END = "</Username>\n";

	private static final String PASSWORD_START = "\t\t\t<Password>";

	private static final String PASSWORD_END = "</Password>\n";

	private static final String DELAY_SECONDS_START = "\t\t\t<DelaySeconds>";

	private static final String DELAY_SECONDS = "0";

	private static final String DELAY_SECONDS_END = "</DelaySeconds>\n";

	private static final String SUCCESS_URL = "\t\t\t<SuccessURL></SuccessURL>\n";

	private static final String FAILURE_URL = "\t\t\t<FailureURL></FailureURL>\n";

	private static final String END = "\t\t</cwmp:Download>\n";

	private String url;
	private String type;
	private int filesize;
	private String targetFilename;
	private String commandKey;
	private String username;
	private String password;

	public DOreq(String url, String type, String targetFilename, int filesize, String commandKey, String username, String password) {
		this.url = url;
		this.type = type;
		this.filesize = filesize;
		this.targetFilename = targetFilename;
		this.commandKey = commandKey;
		this.username = username;
		this.password = password;
	}

	@Override
	public String toXmlImpl() {

		StringBuilder sb = new StringBuilder(3);
		sb.append(START);
		sb.append(COMMAND_KEY_START);
		if (commandKey != null)
			sb.append(commandKey);
		else
			sb.append("Download_To_CPE");
		sb.append(COMMAND_KEY_END);
		sb.append(FILE_TYPE_START);
		sb.append(type);
		sb.append(FILE_TYPE_END);
		sb.append(URL_START);
		sb.append(url);
		sb.append(URL_END);
		
		sb.append(USERNAME_START);
		if (Properties.FILE_AUTH_USED)
		  sb.append(username);
		sb.append(USERNAME_END);
		sb.append(PASSWORD_START);
		if (Properties.FILE_AUTH_USED)
		  sb.append(password);
		sb.append(PASSWORD_END);
		sb.append("\t\t\t<FileSize>" + filesize + "</FileSize>\n");
		if (targetFilename != null)
			sb.append("\t\t\t<TargetFileName>" + targetFilename + "</TargetFileName>\n");
		else
			sb.append("\t\t\t<TargetFileName></TargetFileName>\n");
		sb.append(DELAY_SECONDS_START);
		sb.append(DELAY_SECONDS);
		sb.append(DELAY_SECONDS_END);
		sb.append(SUCCESS_URL);
		sb.append(FAILURE_URL);
		sb.append(END);
		return sb.toString();
	}
}
