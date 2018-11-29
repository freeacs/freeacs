package com.github.freeacs.tr069;

import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.http.HTTPRequestResponseData;

public class CommandKey {
  private String cpeKey;
  private String serverKey;

  public void setCpeKey(String cpeKey) {
    this.cpeKey = cpeKey;
  }

  public void setServerKey(HTTPRequestResponseData reqRes) {
    this.serverKey =
        reqRes.getSessionData().getUnit().getParameterValue(SystemParameters.JOB_CURRENT_KEY);
  }

  public boolean isEqual() {
    return serverKey == null
        || "".equals(serverKey.trim())
        || (cpeKey != null && cpeKey.equals(serverKey));
  }
}
