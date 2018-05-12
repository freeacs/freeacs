package com.github.freeacs.stun;

import java.util.HashMap;
import java.util.Map;

/**
 * These parameters holds a special meaning for the TR-069 Server. The
 * server MUST know the value of these CPE-parameters, regardless of what
 * configuration is found in the database. If the database only asks for
 * parameter X and Y, but none of these parameters, the server will inject
 * these parameters in the GetParameterValueRequest to the CPE. 
 * 
 * The reason for this special interest is that these parameters controls 
 * the following important features of the server:
 * - download (config/firmware)
 * - periodic inform interval (spread/static)
 * - line authentication (has the subscriber moved the CPE?)
 * 
 * 
 * 
 * @author Morten
 *
 */
public class CPEParameters {
	// The keyroot of the CPE, must be set by the constuctor;
	private String keyRoot;
	// The config version of the CPE
	public String CONFIG_VERSION;
	// The software/firmware version of the CPE
	public String SOFTWARE_VERSION;
	// The periodic inform interval on the CPE
	public String PERIODIC_INFORM_INTERVAL;
	// The connection url (for kick, ip-address)
	public String CONNECTION_URL;
	// The connection username (for kick, using authentication)
	public String CONNECTION_USERNAME;
	// The connection password (for kick, using authentication)
	public String CONNECTION_PASSWORD;
	// The UDP Connection URL (for kick through NAT)
	public String UDP_CONNECTION_URL;

	//	// The phone number
	//	public String PHONE_NUMBER;
	//	// The voice service enabled parameter
	//	public String VOICE_ENABLED;

	public CPEParameters(String keyRoot) {
		this.keyRoot = keyRoot;
		CONFIG_VERSION = keyRoot + "DeviceInfo.VendorConfigFile.1.Version";
		SOFTWARE_VERSION = keyRoot + "DeviceInfo.SoftwareVersion";
		PERIODIC_INFORM_INTERVAL = keyRoot + "ManagementServer.PeriodicInformInterval";
		CONNECTION_URL = keyRoot + "ManagementServer.ConnectionRequestURL";
		CONNECTION_PASSWORD = keyRoot + "ManagementServer.ConnectionRequestPassword";
		CONNECTION_USERNAME = keyRoot + "ManagementServer.ConnectionRequestUsername";
		// Optional - only for TR-111
		UDP_CONNECTION_URL = keyRoot + "ManagementServer.UDPConnectionRequestAddress";
		//		PHONE_NUMBER = keyRoot + "Services.VoiceService.1.VoiceProfile.1.Line.1.SIP.URI";
		//		VOICE_ENABLED = keyRoot + "Services.VoiceService.1.VoiceProfile.1.Enable";
		cpeParams.put(CONFIG_VERSION, null);
		cpeParams.put(SOFTWARE_VERSION, null);
		cpeParams.put(PERIODIC_INFORM_INTERVAL, null);
		cpeParams.put(CONNECTION_URL, null);
		cpeParams.put(CONNECTION_USERNAME, null);
		cpeParams.put(CONNECTION_PASSWORD, null);
		//		cpeParams.put(PHONE_NUMBER, null);
		//		cpeParams.put(VOICE_ENABLED, null);
	}

	public Map<String, ParameterValueStruct> cpeParams = new HashMap<String, ParameterValueStruct>();

	public String getValue(String param) {
		ParameterValueStruct pvs = cpeParams.get(param);
		if (pvs != null && pvs.getValue() != null)
			return pvs.getValue();
		else
			return null;
	}

	public ParameterValueStruct getPvs(String param) {
		return cpeParams.get(param);
	}

	public void putPvs(String param, ParameterValueStruct pvs) {
		cpeParams.put(param, pvs);
	}

	public String getKeyRoot() {
		return keyRoot;
	}

}
