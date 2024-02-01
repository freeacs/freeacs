package com.github.freeacs.dbi;

import com.github.freeacs.common.util.AbstractMySqlIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class PermissionsTest extends BaseDBITest{

  Unittype unittype;
  Profile profile;
  User user;

  @Test
  public void delete() throws SQLException {
    this.addTestData("TestUnitType 1", "TestProfile 1", "TestUser 1");

    Permission newPermission = new Permission(user, unittype.getId(), profile.getId());

    Permissions perms = new Permissions(acs.getDataSource());
    perms.addOrChange(newPermission);

    Permission createdPerm = perms.getByUnittypeProfile(unittype.getId(), profile.getId());

    perms.delete(createdPerm);

    createdPerm = perms.getByUnittypeProfile(unittype.getId(), profile.getId());

    Assertions.assertNull(createdPerm);

  }

  @Test
  public void add() throws SQLException {

    this.addTestData("TestUnitType 2", "TestProfile 2", "TestUser 2");

    Permission newPermission = new Permission(user, unittype.getId(), profile.getId());
    Users users = new Users(AbstractMySqlIntegrationTest.getDataSource());

    Permissions perms = new Permissions(acs.getDataSource());
    perms.addOrChange(newPermission);

    Permission createdPerm = perms.getByUnittypeProfile(unittype.getId(), profile.getId());

    Assertions.assertNotNull(createdPerm);
    Assertions.assertEquals(newPermission.getUnittypeId(), createdPerm.getUnittypeId());

  }

  @Test
  public void change() throws SQLException
  {
    this.addTestData("TestUnitType 3", "TestProfile 3", "TestUser 3");

    Permission newPermission = new Permission(user, unittype.getId(), profile.getId());

    Permissions perms = new Permissions(acs.getDataSource());
    perms.addOrChange(newPermission);

    Permission createdPerm = perms.getByUnittypeProfile(unittype.getId(), profile.getId());

    Assertions.assertEquals(createdPerm.getUser().getId(), user.getId());

    User otherUser = TestUtils.createUser(
            "OtherTestUser","Other Test User", "s3cr3tPassw0rd",false, acs);

    Permission editPermission = new Permission(otherUser, unittype.getId(), profile.getId());
    editPermission.setId(createdPerm.getId());

    perms.addOrChange(editPermission);

    Permission editedPerm = perms.getByUnittypeProfile(unittype.getId(), profile.getId());

    Assertions.assertEquals(editedPerm.getUser().getId(), otherUser.getId());

  }

  public void addTestData(String unitType, String testProfile, String testUser) throws SQLException
  {
    unittype = TestUtils.createUnittype(unitType, acs);

    profile = TestUtils.createProfile(testProfile, unittype, acs);
    user = TestUtils.createUser(
            testUser,"Test User 4", "s3cr3tPassw0rd",false, acs);

  }
}
