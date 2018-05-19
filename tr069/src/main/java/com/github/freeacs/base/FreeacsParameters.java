package com.github.freeacs.base;

import com.github.freeacs.tr069.xml.ParameterValueStruct;

import java.util.HashMap;
import java.util.Map;

public class FreeacsParameters {

	private Map<String, ParameterValueStruct> freeacsParams = new HashMap<String, ParameterValueStruct>();

	public String getValue(String param) {
		ParameterValueStruct pvs = freeacsParams.get(param);
		if (pvs != null && pvs.getValue() != null)
			return pvs.getValue();
		else
			return null;
	}

	public void putPvs(String param, ParameterValueStruct pvs) {
		freeacsParams.put(param, pvs);
	}

	public Map<String, ParameterValueStruct> getFreeacsParams() {
		return freeacsParams;
	}

}
