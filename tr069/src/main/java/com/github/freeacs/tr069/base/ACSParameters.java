package com.github.freeacs.tr069.base;

import com.github.freeacs.tr069.xml.ParameterValueStruct;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ACSParameters {
  private final Map<String, ParameterValueStruct> acsParams = new HashMap<>();

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

}
