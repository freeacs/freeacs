package com.github.freeacs.dbi;

public class UnitParameter {
	private String unitId;

	private Profile profile;

	private Parameter parameter;

	//	private String sessionValue;

	public UnitParameter(UnittypeParameter unittypeParameter, String unitId, String value, Profile profile) {
		this.parameter = new Parameter(unittypeParameter, value);
		this.unitId = unitId;
		this.profile = profile;
	}

	public UnitParameter(Parameter parameter, String unitId, Profile profile) {
		this.parameter = parameter;
		this.unitId = unitId;
		this.profile = profile;
	}

	//	public UnitParameter(Parameter parameter, String unitId, Profile profile) {
	//		this(parameter, unitId, profile);
	//		//		this.sessionValue = sessionValue;
	//	}

	//	public UnitParameter(UnittypeParameter unittypeParameter, String unitId, String value, Profile profile) {
	//		this(unittypeParameter, unitId, value, profile);
	//		//		this.sessionValue = sessionValue;
	//	}

	public String getUnitId() {
		return unitId;
	}

	@Override
	public String toString() {
		return "UP: " + parameter + " [" + unitId + "]";
	}

	public Profile getProfile() {
		return profile;
	}

	public Parameter getParameter() {
		return parameter;
	}

	public void setParameter(Parameter parameter) {
		this.parameter = parameter;
	}

	public String getValue() {
		if (parameter != null)
			return parameter.getValue();
		return null;
	}

	public void setValue(String value) {
		if (parameter != null)
			parameter.setValue(value);
	}

	//	public String getSessionValue() {
	//		return sessionValue;
	//	}
	//
	//	public void setSessionValue(String sessionValue) {
	//		this.sessionValue = sessionValue;
	//	}

}
