package com.github.freeacs.dbi;

import static org.junit.Assert.*;

import org.junit.Test;

import java.sql.SQLException;

public class PermissionsTest extends BaseDBITest{

  Unittype unittype;
  Profile profile;
  User user;

  @Test
  public void delete() throws SQLException {
    this.addTestData();

    Permission newPermission = new Permission(user, unittype.getId(), profile.getId());
    Users users = new Users(dataSource);

    Permissions perms = new Permissions(acs.getDataSource());
    perms.addOrChange(newPermission);

    Permission createdPerm = perms.getByUnittypeProfile(unittype.getId(), profile.getId());

    perms.delete(createdPerm);

    createdPerm = perms.getByUnittypeProfile(unittype.getId(), profile.getId());

    assertNull(createdPerm);

  }

  @Test
  public void add() throws SQLException {

    this.addTestData();

    Permission newPermission = new Permission(user, unittype.getId(), profile.getId());
    Users users = new Users(dataSource);

    Permissions perms = new Permissions(acs.getDataSource());
    perms.addOrChange(newPermission);

    Permission createdPerm = perms.getByUnittypeProfile(unittype.getId(), profile.getId());

    assertNotNull(createdPerm);
    assertEquals(newPermission.getUnittypeId(), createdPerm.getUnittypeId());

  }

  @Test
  public void change() throws SQLException
  {
    this.addTestData();

    Permission newPermission = new Permission(user, unittype.getId(), profile.getId());

    Permissions perms = new Permissions(acs.getDataSource());
    perms.addOrChange(newPermission);

    Permission createdPerm = perms.getByUnittypeProfile(unittype.getId(), profile.getId());

    assertEquals(createdPerm.getUser().getId(), user.getId());

    User otherUser = TestUtils.createUser(
            "OtherTestUser","Other Test User", "s3cr3tPassw0rd",false, acs);

    Permission editPermission = new Permission(otherUser, unittype.getId(), profile.getId());
    editPermission.setId(createdPerm.getId());

    perms.addOrChange(editPermission);

    Permission editedPerm = perms.getByUnittypeProfile(unittype.getId(), profile.getId());

    assertEquals(editedPerm.getUser().getId(),otherUser.getId());

  }

  public void addTestData() throws SQLException
  {
    unittype = TestUtils.createUnittype("TestUnitType", acs);

    profile = TestUtils.createProfile("TestProfile", unittype, acs);
    user = TestUtils.createUser(
            "TestUser","Test User", "s3cr3tPassw0rd",false, acs);

  }
}
