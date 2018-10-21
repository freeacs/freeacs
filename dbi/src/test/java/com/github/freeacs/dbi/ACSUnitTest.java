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
    ACSUnit acsUnit = new ACSUnit(dataSource, acs, syslog);

    // When:
    createUnitAndVerify(acsUnit, unitId);
  }

  @Test
  public void deleteUnit() throws SQLException {
    // Given:
    String unitId = "YuddoJb7sssB";
    ACSUnit acsUnit = new ACSUnit(dataSource, acs, syslog);
    Unit unit = createUnitAndVerify(acsUnit, unitId);

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
    ACSUnit acsUnit = new ACSUnit(dataSource, acs, syslog);
    createUnitAndVerify(acsUnit, unitId);

    // When:
    String otherUnittypeName = "Other unittype";
    Unittype unittype = TestUtils.createUnittype(otherUnittypeName, acs);
    Profile profile = unittype.getProfiles().getByName("Default");
    acsUnit.moveUnits(Collections.singletonList(unitId), profile);

    // Then:
    Unit unit = acsUnit.getUnitById(unitId);
    assertNotNull(unit);
    assertEquals("Test unittype", unit.getUnittype().getName());
    assertEquals("Default", unit.getProfile().getName());
  }

  @Test
  public void moveUnitToAnotherProfileWithinSameUnittypeShouldWWork() throws SQLException {
    // Given:
    String unitId = "YuddoJb7sssB";
    ACSUnit acsUnit = new ACSUnit(dataSource, acs, syslog);
    createUnitAndVerify(acsUnit, unitId);

    // When:
    Unittype unittype = acs.getUnittypes().getByName("Test unittype");
    Profile profile = new Profile("Test profile", unittype);
    unittype.getProfiles().addOrChangeProfile(profile, acs);
    acsUnit.moveUnits(Collections.singletonList(unitId), profile);

    // Then:
    Unit unit = acsUnit.getUnitById(unitId);
    assertNotNull(unit);
    assertEquals("Test unittype", unit.getUnittype().getName());
    assertEquals("Test profile", unit.getProfile().getName());
  }

  @Test
  public void createParameters() throws SQLException {
    // Given:
    String unitId = "YuddoJb7sssB";
    ACSUnit acsUnit = new ACSUnit(dataSource, acs, syslog);
    createUnitAndVerify(acsUnit, unitId);

    // When:
    Unit unit = acsUnit.getUnitById(unitId);
    Unittype unittype = acs.getUnittype(unit.getUnittype().getId());
    UnittypeParameter utpSecret =
        new UnittypeParameter(
            unittype, "System.X_FREEACS-COM.Secret", new UnittypeParameterFlag("XC"));
    UnittypeParameter utpComment =
        new UnittypeParameter(
            unittype, "System.X_FREEACS-COM.Comment", new UnittypeParameterFlag("X"));
    unittype
        .getUnittypeParameters()
        .addOrChangeUnittypeParameters(Arrays.asList(utpSecret, utpComment), acs);
    List<UnitParameter> params =
        Arrays.asList(
            new UnitParameter(utpSecret, unitId, "Secret", unit.getProfile()),
            new UnitParameter(utpComment, unitId, "Comment", unit.getProfile()));
    acsUnit.addOrChangeUnitParameters(params, unit.getProfile());

    // Then:
    unit = acsUnit.getUnitById(unitId);
    assertEquals(2, unit.getUnitParameters().size());
    assertEquals(
        "Comment", unit.getUnitParameters().get("System.X_FREEACS-COM.Comment").getValue());
    assertEquals("Secret", unit.getUnitParameters().get("System.X_FREEACS-COM.Secret").getValue());
  }

  private Unit createUnitAndVerify(ACSUnit acsUnit, String unitId) throws SQLException {
    // Given:
    String unitTypeName = "Test unittype";
    Unittype unittype = TestUtils.createUnittype(unitTypeName, acs);
    Profile profile = unittype.getProfiles().getByName("Default");

    // When:
    acsUnit.addUnits(Collections.singletonList(unitId), profile);

    // Then:
    Unit unit = acsUnit.getUnitById(unitId);
    assertEquals(unitId, unit.getId());
    assertEquals("Default", unit.getProfile().getName());
    assertEquals("Test unittype", unit.getUnittype().getName());

    return unit;
  }
}
