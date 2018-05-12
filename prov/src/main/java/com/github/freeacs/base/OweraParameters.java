package com.github.freeacs.base;

import com.github.freeacs.tr069.xml.ParameterValueStruct;

import java.util.HashMap;
import java.util.Map;

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
