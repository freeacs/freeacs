package com.github.freeacs.dbi;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GroupParametersTest extends BaseDBITest {

  @Test
  public void addOrChangeGroupParameter() throws SQLException {
    // Given:
    String unittypeName = "Test unittype 1";
    String groupName = "Group name";
    String profileName = "Default";
    String utpParamName = "Test.Test";
    String value = "Hei";
    List<TestUtils.Param> params =
        Collections.singletonList(new TestUtils.Param(utpParamName, "X"));
    Unittype unittype = TestUtils.createUnittype(unittypeName, acs);
    TestUtils.createUnittypeParameters(unittype, params, acs);
    Group group = TestUtils.createGroupAndVerify(groupName, profileName, unittype, acs);

    // When:
    GroupParameter groupParameter =
        TestUtils.createGroupParameterAndVerify(utpParamName, value, unittype, group, acs);

    // Then:
    GroupParameter byId = group.getGroupParameters().getById(groupParameter.getId());
    Assertions.assertEquals(value, byId.getParameter().getValue());
  }

  @Test
  public void deleteGroupParameter() throws SQLException {
    // Given:
    String unittypeName = "Test unittype 2";
    String groupName = "Group name";
    String profileName = "Default";
    String utpParamName = "Test.Test";
    String value = "Hei";
    List<TestUtils.Param> params =
        Collections.singletonList(new TestUtils.Param(utpParamName, "X"));
    Unittype unittype = TestUtils.createUnittype(unittypeName, acs);
    TestUtils.createUnittypeParameters(unittype, params, acs);
    Group group = TestUtils.createGroupAndVerify(groupName, profileName, unittype, acs);
    GroupParameter groupParameter =
        TestUtils.createGroupParameterAndVerify(utpParamName, value, unittype, group, acs);

    // When:
    group.getGroupParameters().deleteGroupParameter(groupParameter, acs);

    // Then:
    Assertions.assertEquals(0, group.getGroupParameters().getGroupParameters().length);
  }
}
