package com.owera.xaps.base;

import com.owera.common.util.CleanupNotifier;
import com.owera.xaps.dbi.Users;
import com.owera.xaps.dbi.util.ProvisioningMessage;
import com.owera.xaps.dbi.util.ProvisioningMessage.ErrorResponsibility;
import com.owera.xaps.dbi.util.ProvisioningMessage.ProvOutput;
import com.owera.xaps.dbi.util.ProvisioningMessage.ProvStatus;
import com.owera.xaps.dbi.util.SyslogClient;

public class SessionDataCacheCleanup implements CleanupNotifier {

	private SessionDataI sessionData;
	private String unitKey;

	public SessionDataCacheCleanup(String unitKey, SessionDataI sessionData) {
		this.unitKey = unitKey;
		this.sessionData = sessionData;
	}

	@Override
	public void execute() {
		Log.error(SessionDataCacheCleanup.class, "SessionData for " + sessionData.getUnitId() + " (key:" + unitKey
				+ ") was removed from cache after timeout - indicate a session which did not terminate correctly");

		ProvisioningMessage pm = sessionData.getProvisioningMessage();
		if (pm != null) { // only available if run in a TR-069 server (not in SPP-server)
			pm.setErrorMessage("TR-069 session was aborted - most probably because the client did not respond");
			pm.setErrorResponsibility(ErrorResponsibility.CLIENT);
			pm.setProvStatus(ProvStatus.ERROR);
			if (pm.getProvOutput() == null)
				pm.setProvOutput(ProvOutput.EMPTY);
			if (sessionData.getStartupTmsForSession() != null)
				pm.setSessionLength((int) (System.currentTimeMillis() - sessionData.getStartupTmsForSession()));
			try {
				SyslogClient.send(pm.syslogMsg(16, null, Users.USER_ADMIN));
			} catch (Throwable t) {
				Log.error(SessionDataCacheCleanup.class, "Could not send/make syslog-message in SessionDataCacheCleanup");
			}
		}

	}
}
