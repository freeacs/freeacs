package com.github.freeacs.tr069.base;

import com.github.freeacs.tr069.xml.ParameterValueStruct;
import java.util.HashMap;
import java.util.Map;

public class ACSParameters {
  private Map<String, ParameterValueStruct> acsParams = new HashMap<>();

  public String getValue(String param) {
    ParameterValueStruct pvs = acsParams.get(param);
    if (pvs != null && pvs.getValue() != null) {
      return pvs.getValue();
    } else {
      return null;
    }
  }

  public void putPvs(String param, ParameterValueStruct pvs) {
    acsParams.put(param, pvs);
  }

  public Map<String, ParameterValueStruct> getAcsParams() {
    return acsParams;
  }
}
