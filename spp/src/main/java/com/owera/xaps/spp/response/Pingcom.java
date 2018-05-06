package com.owera.xaps.spp.response;

import java.util.Map.Entry;

import com.owera.xaps.dbi.Unit;

/*
 * Pingcom only supports TFTP provisioning, not HTTP. (It does support TR-069 and OPP as well, but
 * that is of course not handled here.
 */
public class Pingcom implements ProvisioningResponse {

	private String paramsSent = "";

	public byte[] getEmptyResponse() {
		return "".getBytes();
	}

	public byte[] getConfigResponse(Unit u, long periodicInterval) {
		String response = "";
		for (Entry<String, String> entry : u.getParameters().entrySet()) {
			String parameterName = entry.getKey();
			if (parameterName.startsWith("System."))
				continue;
			if (parameterName.startsWith("TelnetDevice."))
				continue;
			if (parameterName.indexOf("DeviceInfo.PeriodicInformInterval") > -1)
				continue;
			if (parameterName.equals("InternetGatewayDevice.DeviceInfo.SoftwareVersion"))
				continue;
			response += parameterName + "=" + entry.getValue() + "\n";
			paramsSent += parameterName + "=" + entry.getValue() + ", ";
		}
		response += "InternetGatewayDevice.DeviceInfo.PeriodicInformInterval=" + periodicInterval + "\n";
		paramsSent += "InternetGatewayDevice.DeviceInfo.PeriodicInformInterval=" + periodicInterval;
		return response.getBytes();
	}

	public byte[] getUpgradeResponse(String upgradeURL) {
		String response = "";
		response += "InternetGatewayDevice.DeviceInfo.SoftwareVersion=" + upgradeURL + "\n";
		paramsSent += "InternetGatewayDevice.DeviceInfo.SoftwareVersion=" + upgradeURL;
		return response.getBytes();
	}

	public byte[] getDelayResponse(long periodicInterval) {
		String response = "";
		response += "InternetGatewayDevice.DeviceInfo.PeriodicInformInterval=" + periodicInterval + "\n";
		paramsSent += "InternetGatewayDevice.DeviceInfo.PeriodicInformInterval=" + periodicInterval;
		return response.getBytes();
	}

	public byte[] getRebootResponse() {
		String response = "";
		response += "InternetGatewayDevice.DeviceInfo.PeriodicInformInterval=-1\n";
		paramsSent += "InternetGatewayDevice.DeviceInfo.PeriodicInformInterval=-1";
		return response.getBytes();
	}

	public String getParameterSent() {
		return paramsSent;
	}

	public String getContentType() {
		return null;
	}

	public byte[] encrypt(byte[] message, String password) throws Exception {
		return null;
	}

}
