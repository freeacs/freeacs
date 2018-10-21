package com.github.freeacs.dbi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class ACSUnitTest extends BaseDBITest {

  @Test
  public void createUnit() throws SQLException {
    // Given:
    String unitId = "YuddoJb7sssB";
    String unitTypeName = "Test unittype";
    String profileName = "Default";
    ACSUnit acsUnit = new ACSUnit(dataSource, acs, syslog);

    // When:
    TestUtils.createUnitAndVerify(acsUnit, unitId, acs, unitTypeName, profileName);
  }

  @Test
  public void deleteUnit() throws SQLException {
    // Given:
    String unitId = "YuddoJb7sssB";
    String unitTypeName = "Test unittype";
    String profileName = "Default";
    ACSUnit acsUnit = new ACSUnit(dataSource, acs, syslog);
    Unit unit = TestUtils.createUnitAndVerify(acsUnit, unitId, acs, unitTypeName, profileName);

    // When:
    acsUnit.deleteUnit(unit);

    // Then:
    unit = acsUnit.getUnitById(unitId);
    assertNull(unit);
  }

  @Test
  public void moveUnitToAnotherUnittypeShouldNotWork() throws SQLException {
    // Given:
    String unitId = "YuddoJb7sssB";
    String unitTypeName = "Test unittype";
    String profileName = "Default";
    String otherUnittype = "Other unittype";
    ACSUnit acsUnit = new ACSUnit(dataSource, acs, syslog);
    TestUtils.createUnitAndVerify(acsUnit, unitId, acs, unitTypeName, profileName);

    // When:
    Unittype unittype = TestUtils.createUnittype(otherUnittype, acs);
    Profile profile = unittype.getProfiles().getByName(profileName);
    acsUnit.moveUnits(Collections.singletonList(unitId), profile);

    // Then:
    Unit unit = acsUnit.getUnitById(unitId);
    assertNotNull(unit);
    assertEquals(unitTypeName, unit.getUnittype().getName());
    assertEquals(profileName, unit.getProfile().getName());
  }

  @Test
  public void moveUnitToAnotherProfileWithinSameUnittypeShouldWWork() throws SQLException {
    // Given:
    String unitId = "YuddoJb7sssB";
    String unitTypeName = "Test unittype";
    String defaultProfile = "Default";
    String newProfile = "Test profile";
    ACSUnit acsUnit = new ACSUnit(dataSource, acs, syslog);
    TestUtils.createUnitAndVerify(acsUnit, unitId, acs, unitTypeName, defaultProfile);

    // When:
    Unittype unittype = acs.getUnittypes().getByName(unitTypeName);
    Profile profile = new Profile(newProfile, unittype);
    unittype.getProfiles().addOrChangeProfile(profile, acs);
    acsUnit.moveUnits(Collections.singletonList(unitId), profile);

    // Then:
    Unit unit = acsUnit.getUnitById(unitId);
    assertNotNull(unit);
    assertEquals(unitTypeName, unit.getUnittype().getName());
    assertEquals(newProfile, unit.getProfile().getName());
  }

  @Test
  public void createParameters() throws SQLException {
    // Given:
    String unitId = "YuddoJb7sssB";
    String secretParam = "System.X_FREEACS-COM.Secret";
    String secretValue = "Secret";
    String commentParam = "System.X_FREEACS-COM.Comment";
    String commentValue = "Comment";
    UnittypeParameterFlag flag = new UnittypeParameterFlag("X");
    String unitTypeName = "Test unittype";
    String profileName = "Default";
    ACSUnit acsUnit = new ACSUnit(dataSource, acs, syslog);
    TestUtils.createUnitAndVerify(acsUnit, unitId, acs, unitTypeName, profileName);

    // When:
    Unit unit = acsUnit.getUnitById(unitId);
    Unittype unittype = acs.getUnittype(unit.getUnittype().getId());
    UnittypeParameter utpSecret = new UnittypeParameter(unittype, secretParam, flag);
    UnittypeParameter utpComment = new UnittypeParameter(unittype, commentParam, flag);
    unittype
        .getUnittypeParameters()
        .addOrChangeUnittypeParameters(Arrays.asList(utpSecret, utpComment), acs);
    List<UnitParameter> params =
        Arrays.asList(
            new UnitParameter(utpSecret, unitId, secretValue, unit.getProfile()),
            new UnitParameter(utpComment, unitId, commentValue, unit.getProfile()));
    acsUnit.addOrChangeUnitParameters(params, unit.getProfile());

    // Then:
    unit = acsUnit.getUnitById(unitId);
    assertEquals(2, unit.getUnitParameters().size());
    assertEquals(commentValue, unit.getUnitParameters().get(commentParam).getValue());
    assertEquals(secretValue, unit.getUnitParameters().get(secretParam).getValue());
  }
}
