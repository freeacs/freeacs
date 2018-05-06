package com.owera.xaps.spp.response;

import com.owera.xaps.dbi.Unit;

public interface ProvisioningResponse {
	public byte[] getEmptyResponse();

	public byte[] getConfigResponse(Unit u, long periodicInterval);

	public byte[] getUpgradeResponse(String upgradeURL);

	public byte[] getDelayResponse(long periodicInterval);

	public byte[] getRebootResponse();

	public String getParameterSent();

	public String getContentType();

	public byte[] encrypt(byte[] message, String password) throws Exception;
}
