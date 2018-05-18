package com.github.freeacs.shell.transform;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyncStingWithUVTC {

	// Unix tool usage: uuid [-v version] [-m] [-n count] [-1] [-F format] [-o filename] [namespace name]
	// uuid -v 5 -F STR ns:URL opp://owera.com/\;mac=00:21:94:00:15:0e

	//	 00219400151D
	public static String shortMAC(String MAC) {
		MAC = MAC.toUpperCase();
		MAC = MAC.replace(":", "");
		if (MAC.length() != 12)
			return null;
		return MAC;
	}

	// 00:21:94:00:15:1D
	public static String longMAC(String MAC) {
		MAC = shortMAC(MAC);
		if (MAC == null)
			return null;
		String fixmac = "";
		for (int i = 0; i < 6; i++)
			fixmac += MAC.substring(i * 2, i * 2 + 2) + ":";
		MAC = fixmac.substring(0, 12 + 5);
		if (MAC.length() != 17)
			return null;
		return MAC;
	}

	// e5a73610dc845ede9ec47c2399c23a07
	//	public static String shortUUID(String UUID) {
	//		UUID = UUID.toLowerCase();
	//		UUID = UUID.replace("-", "");
	//		if (UUID.length() != 32)
	//			return null;
	//		return UUID;
	//	}

	// e5a73610-dc84-5ede-9ec4-7c2399c23a07
	//	public static String longUUID(String UUID) {
	//		UUID = shortUUID(UUID);
	//		if (UUID == null)
	//			return null;
	//		UUID = UUID.substring(0, 8) + "-" + UUID.substring(8, 12) + "-" + UUID.substring(12, 16) + "-" + UUID.substring(16, 20) + "-" + UUID.substring(20, 32);
	//		if (UUID.length() != 36)
	//			return null;
	//		return UUID;
	//	}

	public static String MAC2UUID(String MAC) throws Exception {
		MAC = longMAC(MAC);
		MAC = MAC.toLowerCase(); // Note!!!  UUID is generated from lowercase MAC
		String name = "opp://owera.com/;mac=" + MAC;
		UUID uuid = UUID.nameUUIDFromBytes(name.getBytes());
		String uuid_str = uuid.toString();
		// Replace with version 5
		uuid_str = uuid_str.substring(0, 14) + '5' + uuid_str.substring(15, 36);
		return uuid_str;

	}

	public static void test_conversion(String MAC) throws Exception {
		System.out.printf("%-20s -> %s\n", MAC, MAC2UUID(MAC));
	}

	private static Map<String, String> getUUID2UUIDMap() throws Exception {
		FileReader fr = new FileReader("uvtc/units/3078.u");
		BufferedReader br = new BufferedReader(fr);
		String line = null;
		Map<String, String> map = new HashMap<String, String>();
		while ((line = br.readLine()) != null) {
			String[] args = line.split("\\s+");
			map.put(args[0], args[3]);
		}
		return map;

	}

	private static Map<String, String> getMAC2UUIDMap() throws Exception {
		FileReader fr = new FileReader("uvtc/units/3078.u");
		BufferedReader br = new BufferedReader(fr);
		String line = null;
		Map<String, String> map = new HashMap<String, String>();
		while ((line = br.readLine()) != null) {
			String[] args = line.split("\\s+");
			map.put(args[1], args[0]);
		}
		return map;
	}

	// #!xapsshell [NPA201E] [Line1o2] [004deece-e82b-a441-bc0e-38b92803048e]>
	private static Pattern contextPattern = Pattern.compile("#!xapsshell \\[(\\w+)\\] \\[(\\w+)\\] \\[(.*)\\]>");

	public static void main(String[] args) throws Exception {
		Map<String, String> MAC2UUIDMap = getMAC2UUIDMap();
		Map<String, String> UUID2UUIDMap = getUUID2UUIDMap();
		FileReader fr = new FileReader("../xapsshell/ALL/UNIT_PARAMS");
		BufferedReader br = new BufferedReader(fr);
		FileWriter fw = new FileWriter("../xapsshell/uvtc/unitparams/sting2uvtc.up");
		String line = null;
		String MAC = null;
		String MOTOTECH_UUID = null;
		StringBuilder sb = new StringBuilder();
		Set<String> duplicatesUsed = new HashSet<String>();
		while ((line = br.readLine()) != null) {
			Matcher matcher = contextPattern.matcher(line);
			if (matcher.matches()) {
				if (MAC != null && MAC2UUIDMap.get(MAC) != null) {
					String newUUID = MAC2UUIDMap.get(MAC);
					fw.write("#!xapsshell [NPA201E] [Default] [" + newUUID + "]>\n");
					fw.write(sb.toString());
					sb = new StringBuilder();
					MAC = null;
				} else if (MOTOTECH_UUID != null) {
					String MAC_UUID = null;
					if (duplicatesUsed.contains(MOTOTECH_UUID)) {
						System.out.println("Duplicate already used: " + MOTOTECH_UUID);
					} else {
						duplicatesUsed.add(MOTOTECH_UUID);
						for (Entry<String, String> entry : UUID2UUIDMap.entrySet()) {
							if (entry.getValue().equals(MOTOTECH_UUID)) {
								MAC_UUID = entry.getKey();
								fw.write("#!xapsshell [NPA201E] [Default] [" + MAC_UUID + "]>\n");
								for (Entry<String, String> entry2 : MAC2UUIDMap.entrySet()) {
									if (entry2.getValue().equals(MAC_UUID)) {
										MAC = entry2.getKey();
										break;
									}
								}
								fw.write("InternetGatewayDevice.DeviceInfo.SerialNumber                              " + MAC + "\n");
								fw.write(sb.toString());
							}
						}
					}
					sb = new StringBuilder();
				}
				MOTOTECH_UUID = matcher.group(3);
			} else if (line.startsWith("Device.DeviceInfo.SoftwareVersion")) {
				continue;
			} else if (line.startsWith("Device.DeviceInfo.SerialNumber")) {
				MAC = line.substring(60).trim();
				if (MAC2UUIDMap.get(MAC) != null)
					sb.append("InternetGateway" + line + "\n");
			} else {
				line = line.replace("VoiceService", "Services.VoiceService");
				line = line.replace("SIP.X_FREEACS-COM_DisplayName", "CallingFeatures.CallerIDName");
				line = line.replace("SIP.X_FREEACS-COM_UserName", "SIP.URI");
				if (!line.startsWith("Device.X_FREEACS-COM"))
					line = line.replace("Device.", "InternetGatewayDevice.");
				else {
					line = line.replace("Device.", "System.");
					if (line.startsWith("System.X_FREEACS-COM.Protocol"))
						continue;
					if (line.startsWith("System.X_FREEACS-COM.OPP.Connector.FDSecret.Unit"))
						continue;
					if (line.startsWith("System.X_FREEACS-COM.ProvisioningMode"))
						continue;
					if (line.startsWith("System.X_FREEACS-COM.ProvisioningState"))
						continue;
					if (line.startsWith("System.X_FREEACS-COM.DesiredSoftwareVersion"))
						continue;
					if (line.startsWith("System.X_FREEACS-COM.OPP.Reporter.LogLevel"))
						continue;
					//					System.out.println(line);
				}
				sb.append(line + "\n");
			}
		}
		if (MAC != null) {
			fw.write("#!xapsshell [NPA201E] [Default] [" + MAC2UUIDMap.get(MAC) + "]>\n");
			fw.write(sb.toString().substring(0, sb.toString().length() - 1));
			sb = new StringBuilder();
			MAC = null;
		} else if (MOTOTECH_UUID != null) {
			int duplicateMacCounter = 0;
			String MAC_UUID = null;
			for (Entry<String, String> entry : UUID2UUIDMap.entrySet()) {
				if (entry.getValue().equals(MOTOTECH_UUID)) {
					MAC_UUID = entry.getKey();
					duplicateMacCounter++;
				}
			}
			if (duplicateMacCounter < 2) {
				fw.write("#!xapsshell [NPA201E] [Default] [" + MAC_UUID + "]>\n");
				for (Entry<String, String> entry : MAC2UUIDMap.entrySet()) {
					if (entry.getValue().equals(MAC_UUID)) {
						MAC = entry.getKey();
						break;
					}
				}
				fw.write("InternetGatewayDevice.DeviceInfo.SerialNumber                              " + MAC + "\n");
				fw.write(sb.toString().substring(0, sb.toString().length() - 1));
				sb = new StringBuilder();
			}
		}
		fw.flush();
		fw.close();
	}

	public static void main2(String[] args) throws Exception {

		test_conversion("00:21:94:00:15:1c");
		test_conversion("00:21:94:00:15:1C");
		test_conversion("00219400151c");
		test_conversion("00219400151C");
		test_conversion("0021940054FC");
		test_conversion("0021940075F6");
	}

}