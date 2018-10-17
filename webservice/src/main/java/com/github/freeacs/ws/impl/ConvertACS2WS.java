package com.github.freeacs.ws.impl;

import com.github.freeacs.dbi.ProfileParameter;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.ws.xml.ArrayOfParameter;
import com.github.freeacs.ws.xml.ObjectFactory;
import com.github.freeacs.ws.xml.Parameter;
import com.github.freeacs.ws.xml.ParameterList;
import com.github.freeacs.ws.xml.Profile;
import com.github.freeacs.ws.xml.Unittype;
import java.util.Arrays;

/**
 * Converts from XAPS objects to WS objects.
 *
 * @author Morten
 */
public class ConvertACS2WS {
  private static final ObjectFactory factory = new ObjectFactory();

  public static Unittype convert(com.github.freeacs.dbi.Unittype ut) {
    if (ut == null) {
      return null;
    }
    String name = ut.getName();
    //		String matcherId = ut.getMatcherId();
    String vendor = ut.getVendor();
    String description = ut.getDescription();
    String protocol = ut.getProtocol().toString();
    UnittypeParameter[] utParams = ut.getUnittypeParameters().getUnittypeParameters();
    Parameter[] parameterArray = new Parameter[utParams.length];
    int i = 0;
    for (UnittypeParameter utp : utParams) {
      Parameter p = new Parameter();
      p.setName(utp.getName());
      p.setFlags(factory.createParameterFlags(utp.getFlag().getFlag()));
      parameterArray[i++] = p;
    }
    ParameterList parameters = factory.createParameterList();
    ArrayOfParameter arrayOfParameter = factory.createArrayOfParameter();
    arrayOfParameter.getItem().addAll(Arrays.asList(parameterArray));
    parameters.setParameterArray(arrayOfParameter);
    Unittype unittype = new Unittype();
    unittype.setName(name);
    unittype.setVendor(factory.createUnittypeVendor(vendor));
    unittype.setDescription(factory.createUnittypeDescription(description));
    unittype.setProtocol(factory.createUnittypeProtocol(protocol));
    unittype.setParameters(factory.createUnittypeParameters(parameters));
    return unittype;
  }

  public static Profile convert(com.github.freeacs.dbi.Profile p) {
    if (p == null) {
      return null;
    }
    String name = p.getName();
    ProfileParameter[] pParams = p.getProfileParameters().getProfileParameters();
    Parameter[] parameterArray = new Parameter[pParams.length];
    int i = 0;
    for (ProfileParameter pp : pParams) {
      Parameter param = new Parameter();
      param.setName(pp.getUnittypeParameter().getName());
      param.setValue(factory.createParameterValue(pp.getValue()));
      parameterArray[i++] = param;
    }
    ParameterList parameters = factory.createParameterList();
    ArrayOfParameter arrayOfParameter = factory.createArrayOfParameter();
    arrayOfParameter.getItem().addAll(Arrays.asList(parameterArray));
    parameters.setParameterArray(arrayOfParameter);
    Profile profile = new Profile();
    profile.setName(name);
    profile.setParameters(factory.createProfileParameters(parameters));
    return profile;
  }
}
