package com.github.freeacs.tr069;

import com.github.freeacs.tr069.xml.ParameterValueStruct;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class InformParameters {
  /** The connection udp-url (for kick, ip-address). */
  public String UDP_CONNECTION_URL;

  public Map<String, ParameterValueStruct> cpeParams = new HashMap<>();

  public InformParameters(String keyRoot) {
    UDP_CONNECTION_URL = keyRoot + "ManagementServer.UDPConnectionRequestAddress";
    cpeParams.put(UDP_CONNECTION_URL, null);
  }

  public String getValue(String param) {
    ParameterValueStruct pvs = cpeParams.get(param);
    if (pvs != null && pvs.getValue() != null) {
      return pvs.getValue();
    } else {
      return null;
    }
  }
}
