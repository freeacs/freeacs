package com.github.freeacs.ws.impl;

import com.github.freeacs.dbi.ProfileParameter;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.ws.*;


/**
 * Converts from XAPS objects to WS objects
 * @author Morten
 *
 */
public class ConvertACS2WS {

	public static Unittype convert(com.github.freeacs.dbi.Unittype ut) {
		if (ut == null)
			return null;
		String name = ut.getName();
		//		String matcherId = ut.getMatcherId();
		String vendor = ut.getVendor();
		String description = ut.getDescription();
		String protocol = ut.getProtocol().toString();
		UnittypeParameter[] utParams = ut.getUnittypeParameters().getUnittypeParameters();
		Parameter[] parameterArray = new Parameter[utParams.length];
		int i = 0;
		for (UnittypeParameter utp : utParams) {
			Parameter p = new Parameter(utp.getName(), utp.getFlag().getFlag(), null);
			parameterArray[i++] = p;
		}
		ParameterList parameters = new ParameterList(new ArrayOfParameter(parameterArray));
		return new Unittype(name, null, vendor, description, protocol, parameters);
	}

	public static Profile convert(com.github.freeacs.dbi.Profile p) {
		if (p == null)
			return null;
		String name = p.getName();
		ProfileParameter[] pParams = p.getProfileParameters().getProfileParameters();
		Parameter[] parameterArray = new Parameter[pParams.length];
		int i = 0;
		for (ProfileParameter pp : pParams) {
			Parameter param = new Parameter(pp.getUnittypeParameter().getName(), pp.getValue(), null);
			parameterArray[i++] = param;
		}
		ParameterList parameters = new ParameterList(new ArrayOfParameter(parameterArray));
		return new Profile(name, parameters);
	}
}
