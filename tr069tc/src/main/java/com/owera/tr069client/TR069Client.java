package com.owera.tr069client;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

public class TR069Client {

	public static String TYPE_IN_EM = "IN_EM";
	public static String TYPE_IN_TC_EM = "IN_EM_TC";

	private long nextConnectTms;
	private String nextConnectType = TYPE_IN_EM;
	private int serialNumber;
	private String serialNumberStr;
	private String parameterKey;
	private String softwareVersion = "1";
	private String ip;
	private long startupTime = System.currentTimeMillis();

	private static Logger logger = Logger.getLogger(Session.class);

	public static Map<String, GPValue> defaultParams = new TreeMap<String, GPValue>();
	static {
		defaultParams.put("InternetGatewayDevice.DeviceInfo.HardwareVersion", new GPValue("1.0"));
		defaultParams.put("InternetGatewayDevice.DeviceInfo.VendorConfigFile.1.Version", new GPValue("1.0"));
		defaultParams.put("InternetGatewayDevice.DeviceInfo.ProvisioningCode", new GPValue("N/A"));
		defaultParams.put("InternetGatewayDevice.ManagementServer.ParameterKey", new GPValue("N/A"));
		defaultParams.put("InternetGatewayDevice.ManagementServer.PeriodicInformEnable", new GPValue("1"));
		defaultParams.put("InternetGatewayDevice.ManagementServer.PeriodicInformInterval", new GPValue("60"));
		defaultParams.put("InternetGatewayDevice.ManagementServer.ConnectionRequestPassword", new GPValue("pass"));
		defaultParams.put("InternetGatewayDevice.ManagementServer.ConnectionRequestUsername", new GPValue("user"));
		defaultParams.put("InternetGatewayDevice.ManagementServer.UpgradesManaged", new GPValue("1"));
		defaultParams.put("InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.Enable", new GPValue("1"));
		defaultParams.put("InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.Line.1.CallingFeatures.CallerIDName", new GPValue("N/A"));
		defaultParams.put("InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.Line.1.PhyReferenceList", new GPValue("N/A"));
		defaultParams.put("InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.Line.1.SIP.AuthPassword", new GPValue("pass"));
		defaultParams.put("InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.Line.1.SIP.AuthUserName", new GPValue("user"));
		defaultParams.put("InternetGatewayDevice.Services.VoiceService.1.VoiceProfile.1.Line.1.SIP.URI", new GPValue("N/A"));
		defaultParams.put("InternetGatewayDevice.DeviceInfo.MemoryStatus.Free", new GPValue(0, 5000));
		defaultParams.put("InternetGatewayDevice.DeviceInfo.MemoryStatus.Total", new GPValue(5000, 10000));
		defaultParams.put("InternetGatewayDevice.DeviceInfo.ProcessStatus.CPUUsage", new GPValue(0, 100));
		defaultParams.put("InternetGatewayDevice.DeviceInfo.ProcessStatus.ProcessNumberOfEntries", new GPValue(1, 10));
		defaultParams.put("InternetGatewayDevice.DeviceInfo.TemperatureStatus.TemperatureSensor.1.MaxValue", new GPValue(50, 60));
		defaultParams.put("InternetGatewayDevice.DeviceInfo.TemperatureStatus.TemperatureSensor.1.Value", new GPValue(10, 50));
	}

	private Map<String, String> params = new TreeMap<String, String>();

	private static String softwareVersionName = "InternetGatewayDevice.DeviceInfo.SoftwareVersion";
	private static String uptimeName = "InternetGatewayDevice.DeviceInfo.UpTime";
	private static String connrequrlName = "InternetGatewayDevice.ManagementServer.ConnectionRequestURL";
	private static String serialNumberName = "InternetGatewayDevice.DeviceInfo.SerialNumber";

	// Formatted as hh:mm:dd
	private String getUptime() {
		long uptime = System.currentTimeMillis() - startupTime;
		return "" + uptime / 1000;
		//		long hours = uptime / (3600 * 1000);
		//		uptime -= hours * 3600 * 1000;
		//		long minutes = uptime / (60 * 1000);
		//		String min = "" + minutes;
		//		if (minutes < 10)
		//			min = "0" + min;
		//		uptime -= minutes * 60 * 1000;
		//		long seconds = uptime / 1000;
		//		String sec = "" + seconds;
		//		if (seconds < 10)
		//			sec = "0" + seconds;
		//		return hours + ":" + min + ":" + sec;
	}

	public void parseDOReq(String req) {
		TagValue tv = null;
		int startPos = 0;
		while ((tv = getTagValue(req, "URL", startPos)) != null) {
			startPos = tv.getEndPos();
			String url = tv.getValue();
			int filePos = url.indexOf("/SOFTWARE/") + 10;
			int nextSlash = url.indexOf("/", filePos);
			softwareVersion = url.substring(filePos, nextSlash);
			if (logger.isDebugEnabled())
				logger.debug("DORes: Software is upgraded to " + softwareVersion);

		}
	}

	public void parseGPVReq(String req) {
		TagValue tv = null;
		int startPos = 0;
		while ((tv = getTagValue(req, "string", startPos)) != null) {
			startPos = tv.getEndPos();
			String paramName = tv.getValue();
			if (paramName.equals(connrequrlName)) {
				if (logger.isDebugEnabled())
					logger.debug("GPVRes: Dynamic param: " + paramName + " = " + getIp());
				params.put(paramName, getIp());
				continue;
			} else if (paramName.equals(softwareVersionName)) {
				if (logger.isDebugEnabled())
					logger.debug("GPVRes: Dynamic param: " + paramName + " = " + getSoftwareVersion());
				params.put(paramName, getSoftwareVersion());
				continue;
			} else if (paramName.equals(uptimeName)) {
				if (logger.isDebugEnabled())
					logger.debug("GPVRes: Dynamic param: " + paramName + " = " + getUptime());
				params.put(paramName, getUptime());
				continue;
			} else if (paramName.equals(serialNumberName)) {
				if (logger.isDebugEnabled())
					logger.debug("GPVRes: Dynamic param: " + paramName + " = " + getSerialNumberStr());
				params.put(paramName, getSerialNumberStr());
			}
			String paramValue = params.get(paramName);
			if (paramValue == null) {
				GPValue defaultValue = defaultParams.get(paramName);
				if (defaultValue == null) {
					if (logger.isDebugEnabled())
						logger.debug("GPVRes: Non-default param: " + paramName + " = N/A, never asked for before.");
					params.put(paramName, "N/A");
				} else {
					if (logger.isDebugEnabled())
						logger.debug("GPVRes: Default param: " + paramName + " = " + defaultValue.getValue());
					params.put(paramName, defaultValue.getValue());
				}
			} else {
				params.put(paramName, paramValue);
				if (logger.isDebugEnabled()) {
					logger.debug("GPVRes: Non-default param: " + paramName + " = " + paramValue);
				}
			}
		}
	}

	public void parseSPVReq(String req, Arguments args) {

		TagValue tv = null;
		int startPos = 0;
		while ((tv = getTagValue(req, "Name", startPos)) != null) {
			startPos = tv.getEndPos();
			String parameterName = tv.getValue();
			tv = getTagValue(req, "Value", startPos);
			startPos = tv.getEndPos();
			String value = tv.getValue();
			GPValue defaultValue = defaultParams.get(parameterName);
			if (args.getFailureEvery() == 0 || serialNumber % args.getFailureEvery() != 0) {
				if (defaultValue == null) {
					if (logger.isDebugEnabled())
						logger.debug("SPVRes: " + parameterName + " = " + value + " (added)");
					params.put(parameterName, value);
				} else {
					if (!defaultValue.getValue().equals(value)) {
						if (parameterName.endsWith("PeriodicInformInterval")) {
							Long provIntervalLong = Long.parseLong(value);
							nextConnectTms = System.currentTimeMillis() + provIntervalLong * 1000;
							if (logger.isDebugEnabled())
								logger.debug("SPVRes: " + parameterName + " = " + value + " => Next connect at " + new Date(nextConnectTms));
						} else {
							if (logger.isDebugEnabled())
								logger.debug("SPVRes: " + parameterName + " = " + value + " (changed)");
						}
						params.put(parameterName, value);
					}
				}
			} else if (defaultValue != null && !defaultValue.equals(value) && parameterName.endsWith("PeriodicInformInterval")) {
				Long provIntervalLong = Long.parseLong(value);
				nextConnectTms = System.currentTimeMillis() + provIntervalLong * 1000;
				if (logger.isDebugEnabled())
					logger.debug("SPVRes-FA: " + parameterName + " = " + value + " => Next connect at " + new Date(nextConnectTms));
			}
		}
		if (args.getFailureEvery() == 0 || serialNumber % args.getFailureEvery() != 0)
			parameterKey = getTagValue(req, "ParameterKey", startPos).getValue();

	}

	private static TagValue getTagValue(String xml, String tag, int startPos) {
		int tagPosStart = xml.indexOf("<" + tag, startPos);
		if (tagPosStart > -1) {
			int valuePosStart = xml.indexOf(">", tagPosStart) + 1;
			int valuePosEnd = xml.indexOf("</" + tag + ">", valuePosStart);
			return new TagValue(xml.substring(valuePosStart, valuePosEnd), valuePosEnd + tag.length() + 3);
		}
		return null;
	}

	public TR069Client(int serialNumber, String initalSwVersion) {
		if (initalSwVersion != null)
			this.softwareVersion = initalSwVersion;
		this.serialNumber = serialNumber;
		this.serialNumberStr = String.format("%012d", serialNumber);
		this.ip = getIPAddress(serialNumber);
	}

	public long getNextConnectTms() {
		return nextConnectTms;
	}

	public String getNextConnectType() {
		return nextConnectType;
	}

	public void setNextConnectType(String nextConnectType) {
		this.nextConnectType = nextConnectType;
	}

	public int getSerialNumber() {
		return serialNumber;
	}

	public String getParameterKey() {
		if (parameterKey == null)
			return "";
		return parameterKey;
	}

	private static String getIPAddress(int serialNumber) {
		if (serialNumber < 256)
			return "80.0.0." + serialNumber;
		else if (serialNumber < 256 * 256) {
			int remainder = serialNumber % 256;
			int dividend = serialNumber / 256;
			return "80.0." + dividend + "." + remainder;
		} else {
			int firstRem = serialNumber % 65536;
			int firstDiv = serialNumber / 65536;
			int secRem = firstRem % 256;
			int secDiv = firstRem / 256;
			return "80." + firstDiv + "." + secDiv + "." + secRem;
		}
	}

	public void setNextConnectTms(long nextConnectTms) {
		this.nextConnectTms = nextConnectTms;
	}

	public String getSoftwareVersion() {
		return softwareVersion;
	}

	public String getIp() {
		return ip;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public String getSerialNumberStr() {
		return serialNumberStr;
	}

	public String toString() {
		return serialNumberStr;
	}

}
