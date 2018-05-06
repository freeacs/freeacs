package com.owera.xaps.spp.response;

import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map.Entry;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.owera.xaps.dbi.Unit;

public class SPA implements ProvisioningResponse {

	private String paramsSent = "";

	public byte[] getEmptyResponse() {
		return "<flat-profile></flat-profile>\n".getBytes();
	}

	public byte[] getConfigResponse(Unit u, long periodicInterval) {
		String response = "<flat-profile>\n";
		for (Entry<String, String> entry : u.getParameters().entrySet()) {
			if (entry.getKey().startsWith("System."))
				continue;
			if (entry.getKey().startsWith("TelnetDevice."))
				continue;
			String parameterName = entry.getKey().replaceAll("\\.", "_");
			if (parameterName.equals("Resync_Periodic"))
				continue;
			if (parameterName.equals("Upgrade_Rule"))
				continue;
			@SuppressWarnings("deprecation")
			String value = URLDecoder.decode(entry.getValue());

			//					txt += parameterName + " \"" + entry.getValue() + "\" ;\n";
			response += "<" + parameterName + ">" + value + "</" + parameterName + ">\n";
			paramsSent += parameterName + "=" + value + ", ";
		}
		response += "<Resync_Periodic>" + periodicInterval + "</Resync_Periodic>";
		paramsSent += "Resync_Periodic=" + periodicInterval;
		response += "</flat-profile>\n";
		return response.getBytes();
	}

	public byte[] getUpgradeResponse(String upgradeURL) {
		String response = "<flat-profile>\n";
		response += "<Upgrade_Rule>" + upgradeURL + "</Upgrade_Rule>\n";
		paramsSent += "Upgrade_Rule=" + upgradeURL;
		response += "</flat-profile>\n";
		return response.getBytes();
	}

	public byte[] getDelayResponse(long periodicInterval) {
		String response = "<flat-profile>\n";
		response += "<Resync_Periodic>" + periodicInterval + "</Resync_Periodic>";
		paramsSent += "Resync_Periodic=" + periodicInterval;
		response += "</flat-profile>\n";
		return response.getBytes();
	}

	public byte[] getRebootResponse() {
		String response = "<flat-profile>\n";
		response += "<Resync_Periodic>30</Resync_Periodic>";
		paramsSent += "Resync_Periodic=30";
		response += "</flat-profile>\n";
		return response.getBytes();
	}

	public String getParameterSent() {
		return paramsSent;
	}

	public String getContentType() {
		return "text/xml";
	}

	private static MessageDigest md5 = null;

	private byte[] add(byte[] b1, byte[] b2) {
		byte[] b = new byte[b1.length + b2.length];
		System.arraycopy(b1, 0, b, 0, b1.length);
		System.arraycopy(b2, 0, b, b1.length, b2.length);
		return b;
	}

	private byte[] hash(byte[] prevHash, byte[] password, byte[] salt) throws NoSuchAlgorithmException {
		if (prevHash != null && prevHash.length > 0)
			return md5.digest(add(add(prevHash, password), salt));
		else
			return md5.digest(add(password, salt));
	}

	public byte[] encrypt(byte[] message, String passwordStr) throws Exception {
		md5 = MessageDigest.getInstance("MD5");
		byte[] password = passwordStr.getBytes("UTF-8");
		byte[] salt = { (byte) 0xc7, (byte) 0x73, (byte) 0x21, (byte) 0x8c, (byte) 0x7e, (byte) 0xc8, (byte) 0xee, (byte) 0x99 };
		byte[] hash0 = null;
		byte[] hash1 = hash(hash0, password, salt);
		byte[] hash2 = hash(hash1, password, salt);
		byte[] hash3 = hash(hash2, password, salt);

		byte[] key = add(hash1, hash2);
		SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
		IvParameterSpec ivParameterSpec = new IvParameterSpec(hash3);

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivParameterSpec);
		byte[] encrypted = cipher.doFinal(message);
		return add(add("Salted__".getBytes("UTF-8"), salt), encrypted);
	}

}
