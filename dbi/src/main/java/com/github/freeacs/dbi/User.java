package com.github.freeacs.dbi;

import com.github.freeacs.dbi.crypto.Crypto;
import java.sql.SQLException;

public class User {
  private Integer id;
  private String username;
  private String secret;
  private String fullname;
  private String access;
  private Boolean admin;
  private Users users;
  private Permissions permissions;

  public User(User copyMe) {
    if (copyMe != null) {
      this.username = copyMe.getUsername();
      this.fullname = copyMe.getFullname();
      this.access = copyMe.getAccess();
      this.admin = copyMe.getAdmin();
      this.users = copyMe.getUsers();
      this.permissions = copyMe.getPermissions();
      this.id = copyMe.getId();
    }
  }

  public User(String username, String fullname, String modules, Boolean admin, Users users) {
    this.username = username;
    this.fullname = fullname;
    this.access = modules;
    this.admin = admin;
    this.users = users;
    if (users != null) {
      this.permissions = new Permissions(users.getConnectionProperties());
    }
  }

  public Integer getId() {
    return id;
  }

  protected void setId(Integer id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getSecret() {
    return secret;
  }

  public String toString() {
    return username;
  }

  public void setSecretHashed(String secret) {
    this.secret = secret;
  }

  public void setSecretClearText(String secret) {
    this.secret = Crypto.computeSHA1DigestAsHexUpperCase(secret);
  }

  public String getFullname() {
    return fullname;
  }

  public void setFullname(String fullname) {
    this.fullname = fullname;
  }

  public Permissions getPermissions() {
    if (permissions == null) {
      permissions = new Permissions(users.getConnectionProperties());
    }
    return permissions;
  }

  /**
   * Permission status: To be protected If admin, allow changes on all users except for the admin
   * user Else If requestedBy user is unittypeAdmin for the permission added, then allow Else deny
   *
   * @param permission
   * @throws SQLException
   */
  public void addOrChangePermission(Permission permission, User requestedBy) throws SQLException {
    if (isAdmin()) {
      throw new IllegalArgumentException(
          "Cannot add/change permissions for admin user, set admin-flag to 'false' to add/change permissions");
    } else if (requestedBy.isAdmin() || requestedBy.isUnittypeAdmin(permission.getUnittypeId())) {
      permissions.addOrChange(permission);
    } else {
      throw new IllegalArgumentException(
          "Not allowed to add/change permission for user " + requestedBy.getUsername());
    }
  }

  /**
   * Permission status: Will not be protected - if you can access the user (and therefore the
   * permissions), you are allowed to delete all kinds of permissions
   *
   * @param permission
   * @throws SQLException
   */
  public void deletePermission(Permission permission) throws SQLException {
    permissions.delete(permission);
  }

  public boolean isCorrectSecret(String suppliedSecret) {
    String hashedSuppliedSecret = Crypto.computeSHA1DigestAsHexUpperCase(suppliedSecret);
    return hashedSuppliedSecret.equals(secret);
  }

  public String getAccess() {
    return access;
  }

  public void setAccess(String access) {
    this.access = access;
  }

  public boolean isAdmin() {
    return username.equals(Users.USER_ADMIN) || Boolean.TRUE.equals(admin);
  }

  public boolean isUnittypeAdmin(Integer unittypeId) {
    return isAdmin() || permissions.getByUnittypeProfile(unittypeId, null) != null;
  }

  public boolean isProfileAdmin(Integer unittypeId, Integer profileId) {
    return isAdmin()
        || isUnittypeAdmin(unittypeId)
        || permissions.getByUnittypeProfile(unittypeId, profileId) != null;
  }

  public boolean getAdmin() {
    if (admin != null) {
      return admin;
    }
    return false;
  }

  public void setAdmin(Boolean admin) {
    this.admin = admin;
  }

  protected Users getUsers() {
    return users;
  }
}
