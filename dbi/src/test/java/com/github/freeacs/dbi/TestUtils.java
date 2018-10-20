package com.github.freeacs.dbi;

import java.sql.SQLException;
import java.util.HashMap;
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

  public static Unittype createUnittypeWithParams(String name, List<Param> params, ACS acs)
      throws SQLException {
    Unittype unittype = createUnittype(name, acs);
    UnittypeParameters unittypeParameters =
        new UnittypeParameters(new HashMap<>(), new HashMap<>(), unittype);
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
    Unittypes unittypes = new Unittypes(new HashMap<>(), new HashMap<>());
    unittypes.addOrChangeUnittype(unittype, acs);
    return unittype;
  }

  public static Profile createProfile(String name, String unittypeName, ACS acs)
      throws SQLException {
    Unittype unittype = createUnittype(unittypeName, acs);
    Profile profile = new Profile(name, unittype);
    Profiles profiles = unittype.getProfiles();
    profiles.addOrChangeProfile(profile, acs);
    return profile;
  }
}
