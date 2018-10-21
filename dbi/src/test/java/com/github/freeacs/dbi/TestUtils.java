package com.github.freeacs.dbi;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class TestUtils {
  public static class Param {
    final String name;
    final String flag;

    public Param(String name, String flag) {
      this.name = name;
      this.flag = flag;
    }
  }

  public static Unittype createUnittypeWithParams(Unittype unittype, List<Param> params, ACS acs)
      throws SQLException {
    UnittypeParameters unittypeParameters = unittype.getUnittypeParameters();
    List<UnittypeParameter> parametersToCreate =
        params
            .stream()
            .map(
                param ->
                    new UnittypeParameter(
                        unittype, param.name, new UnittypeParameterFlag(param.flag)))
            .collect(Collectors.toList());
    unittypeParameters.addOrChangeUnittypeParameters(parametersToCreate, acs);
    unittype.setUnittypeParameters(unittypeParameters);
    return unittype;
  }

  public static Unittype createUnittype(String name, ACS acs) throws SQLException {
    Unittype unittype = new Unittype(name, name, name, Unittype.ProvisioningProtocol.TR069);
    acs.getUnittypes().addOrChangeUnittype(unittype, acs);
    return unittype;
  }

  public static Profile createProfile(String name, Unittype unittype, ACS acs) throws SQLException {
    Profile profile = new Profile(name, unittype);
    Profiles profiles = unittype.getProfiles();
    profiles.addOrChangeProfile(profile, acs);
    return profile;
  }
}
