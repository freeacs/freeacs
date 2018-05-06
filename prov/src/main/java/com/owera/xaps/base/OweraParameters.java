package com.owera.xaps.base;

import java.util.HashMap;
import java.util.Map;

import com.owera.xaps.tr069.xml.ParameterValueStruct;

public class OweraParameters {

	private Map<String, ParameterValueStruct> oweraParams = new HashMap<String, ParameterValueStruct>();

	public String getValue(String param) {
		ParameterValueStruct pvs = oweraParams.get(param);
		if (pvs != null && pvs.getValue() != null)
			return pvs.getValue();
		else
			return null;
	}

	public ParameterValueStruct getPvs(String param) {
		return oweraParams.get(param);
	}

	public void putPvs(String param, ParameterValueStruct pvs) {
		oweraParams.put(param, pvs);
	}

	public Map<String, ParameterValueStruct> getOweraParams() {
		return oweraParams;
	}

}
