package com.github.freeacs.dbi;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;

public class UsersTest extends BaseDBITest {

  private User adminUser;

  @Test
  public void testItCanAddUser() throws SQLException {
    this.addTestUser();
    Users users = new Users(dataSource);
    User addedUser = users.getUnprotected("testuser1");
    Assert.assertEquals("testuser1", addedUser.getUsername());
    Assert.assertEquals("Test User 1", addedUser.getFullname());
    Assert.assertTrue(addedUser.isAdmin());
  }

  @Test
  public void testItCanEditUser() throws SQLException {
    this.addTestUser();
    Users users = new Users(dataSource);
    User userToEdit = users.getUnprotected("testuser1");
    userToEdit.setUsername("testuserafteredit");
    userToEdit.setAdmin(false);
    userToEdit.setFullname("Test Edited User");

    users.addOrChange(userToEdit,this.adminUser);

    users = new Users(dataSource);

    User editedUser = users.getUnprotected("testuserafteredit");

    Assert.assertEquals("testuserafteredit",editedUser.getUsername());
    Assert.assertEquals("Test Edited User",editedUser.getFullname());
    Assert.assertFalse(editedUser.isAdmin());

  }

  @Test
  public void testItCanDeleteUser() throws SQLException {
    this.addTestUser();
    Users users = new Users(dataSource);
    User toDelete = users.getUnprotected("testuser1");
    users.delete(toDelete,this.adminUser);

    users = new Users(dataSource);

    User deleted = users.getUnprotected("testuser1");

    Assert.assertNull(deleted);
  }

  private void addTestUser() throws SQLException {
    Users users = new Users(dataSource);
    this.adminUser = users.getUnprotected("admin");
    User newUser = new User("testuser1","Test User 1","",true, users);
    newUser.setSecretClearText("s3cr3p4ssw0rd");
    users.addOrChange(newUser,adminUser);
  }
}
