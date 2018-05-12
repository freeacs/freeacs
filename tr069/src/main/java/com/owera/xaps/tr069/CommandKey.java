package com.owera.xaps.tr069;

import com.github.freeacs.dbi.util.SystemParameters;

public class CommandKey {
	private String cpeKey;
	private String serverKey;

	public String getCpeKey() {
		return cpeKey;
	}

	public void setCpeKey(String cpeKey) {
		this.cpeKey = cpeKey;
	}

	public String getServerKey() {
		return serverKey;
	}

	public void setServerKey(HTTPReqResData reqRes)  {
		this.serverKey = reqRes.getSessionData().getUnit().getParameterValue(SystemParameters.JOB_CURRENT_KEY);
	}

	public boolean isEqual() {
		if (serverKey == null || serverKey.trim().equals(""))
			return true; // no command key specified  - only used for TR069_SCRIPT download
		else if (cpeKey != null && cpeKey.equals(serverKey))
			return true;
		return false;
	}

}
