package com.github.freeacs.dbi;

import com.github.freeacs.common.util.AbstractMySqlIntegrationTest;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class UsersTest extends BaseDBITest {

  private User adminUser;

  @Test
  public void testItCanAddUser() throws SQLException {
    this.addTestUser("testuser1");
    Users users = new Users(AbstractMySqlIntegrationTest.getDataSource());
    User addedUser = users.getUnprotected("testuser1");
    Assertions.assertEquals("testuser1", addedUser.getUsername());
    Assertions.assertEquals("Test User 1", addedUser.getFullname());
    Assertions.assertTrue(addedUser.isAdmin());
  }

  @Test
  public void testItCanEditUser() throws SQLException {
    this.addTestUser("testuser2");
    Users users = new Users(AbstractMySqlIntegrationTest.getDataSource());
    User userToEdit = users.getUnprotected("testuser2");
    userToEdit.setUsername("testuserafteredit");
    userToEdit.setAdmin(false);
    userToEdit.setFullname("Test Edited User");

    users.addOrChange(userToEdit,this.adminUser);

    users = new Users(AbstractMySqlIntegrationTest.getDataSource());

    User editedUser = users.getUnprotected("testuserafteredit");

    Assertions.assertEquals("testuserafteredit", editedUser.getUsername());
    Assertions.assertEquals("Test Edited User", editedUser.getFullname());
    Assertions.assertFalse(editedUser.isAdmin());

  }

  @Test
  public void testItCanDeleteUser() throws SQLException {
    this.addTestUser("testuser3");
    Users users = new Users(AbstractMySqlIntegrationTest.getDataSource());
    User toDelete = users.getUnprotected("testuser3");
    users.delete(toDelete,this.adminUser);

    users = new Users(AbstractMySqlIntegrationTest.getDataSource());

    User deleted = users.getUnprotected("testuser3");

    Assertions.assertNull(deleted);
  }

  private void addTestUser(String testuser1) throws SQLException {
    Users users = new Users(AbstractMySqlIntegrationTest.getDataSource());
    this.adminUser = users.getUnprotected("admin");
    User newUser = new User(testuser1,"Test User 1","",true, users);
    newUser.setSecretClearText("s3cr3p4ssw0rd");
    users.addOrChange(newUser,adminUser);
  }
}
