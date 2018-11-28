package com.github.freeacs.dbi;

import com.github.freeacs.dbi.util.ACSVersionCheck;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Permission status: Fully protected - apart from two methods which intentionally allows access
 * without looking at the permissions
 *
 * @author Morten
 */
public class Users {

  /**
   * We return a default root-user if it's not defined. Default password is freeacs
   *
   * @param name
   * @return
   */
  public static String USER_ADMIN = "admin";

  public static String ACCESS_ADMIN = "Admin";

  private static String ADMIN_DEFAULT_PASSWORD = "freeacs";

  private final DataSource dataSource;
  private Map<Integer, User> idMap = new HashMap<>();
  private Map<String, User> nameMap = new TreeMap<>();

  public Users(DataSource dataSource) throws SQLException {
    ACSVersionCheck.versionCheck(dataSource);
    this.dataSource = dataSource;
    readAllUsers();
  }

  /**
   * Permission status: Will not be protected - all clients of DBI can request any user they like
   * The idea is that it must be possible to retrieve one user to compare passwords in order to
   * login.
   *
   * @param id
   * @return User or null
   */
  public User getUnprotected(Integer id) {
    return idMap.get(id);
  }

  /**
   * Permission status: Fully protected
   *
   * @param name
   * @param requestedBy
   * @return
   */
  public User getProtected(String name, User requestedBy) {
    User user = getUnprotected(name);
    if (allowAccessTo(user, requestedBy)) {
      return user;
    } else {
      return null;
    }
  }

  /**
   * Permission status: Will not be protected - all clients of DBI can request any user they like
   * The idea is that it must be possible to retrieve one user to compare passwords in order to
   * login. Another common usage is for core-modules/backend-modules to be able to retrieve the
   * admin-user without any restrictions (to be able to access all of Fusion no matter what)
   *
   * @param name
   * @return User or null
   */
  public User getUnprotected(String name) {
    User user = nameMap.get(name);
    if (name.equals(USER_ADMIN) && user == null) {
      User adminUser = new User(USER_ADMIN, "Admin user", ACCESS_ADMIN, true, this);
      adminUser.setSecretClearText(ADMIN_DEFAULT_PASSWORD);
      return adminUser;
    } else {
      return user;
    }
  }

  /**
   * Permission status: Fully protected
   *
   * @param delete
   * @param requestedBy
   * @throws SQLException
   */
  public void delete(User delete, User requestedBy) throws SQLException {
    if (!allowAccessTo(delete, requestedBy)) {
      throw new IllegalArgumentException("Not allowed to delete user " + delete.getUsername());
    }
    if (delete.getUsername().equals(USER_ADMIN) && !requestedBy.getUsername().equals(USER_ADMIN)) {
      throw new IllegalArgumentException(
          "Not allowed to delete admin user without being logged in as admin. If admin password is lost, reset it by deleting the admin user from the freeacs.user_ table (default password is 'freeacs')");
    }
    Permission[] permissions = delete.getPermissions().getPermissions();
    for (Permission p : permissions) {
      delete.getPermissions().delete(p);
    }
    Connection c = null;
    PreparedStatement ps = null;
    try {
      c = dataSource.getConnection();
      DynamicStatement ds = new DynamicStatement();
      ds.addSqlAndArguments("DELETE FROM user_ WHERE id = ?", delete.getId());
      ps = ds.makePreparedStatement(c);
      ps.executeUpdate();
      nameMap.remove(delete.getUsername());
      idMap.remove(delete.getId());
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (c != null) {
        c.close();
      }
    }
  }

  /**
   * Permission status: Fully protected: If new user is added, allow it for admins and
   * unittypeAdmins. Copy all permissions and admin-flag from requestUser unless it's admin If a
   * user is changed, check to see if requestedBy-User can access it
   *
   * @param addOrChange
   * @param requestedBy
   * @throws SQLException
   */
  public void addOrChange(User addOrChange, User requestedBy) throws SQLException {
    boolean unittypeAdmin = false;
    for (Permission p : requestedBy.getPermissions().getPermissions()) {
      if (p.getProfileId() == null) {
        unittypeAdmin = true;
      }
    }
    if (!requestedBy.isAdmin() && !unittypeAdmin) {
      throw new IllegalArgumentException(
          "Not allowed to add or change user for this user (must be unittype admin or admin)");
    }
    if (addOrChange.getUsername().equals(USER_ADMIN)
        && !requestedBy.getUsername().equals(USER_ADMIN)) {
      throw new IllegalArgumentException(
          "Not allowed to change admin user without being logged in as admin. If admin password is lost, reset it by deleting the admin user from the freeacs.user_ table (default password is 'freeacs')");
    }
    if (addOrChange.isAdmin() && !requestedBy.isAdmin()) {
      throw new IllegalArgumentException(
          "Not allowed to make an admin user if you're not an admin yourself");
    }
    if (addOrChange.getUsername().equals(USER_ADMIN)) {
      addOrChange.setAccess(ACCESS_ADMIN); // quitely set access correctly
      addOrChange.setAdmin(true); // quitely set admin-flag
    }
    Connection c = null;
    PreparedStatement ps = null;
    try {
      c = dataSource.getConnection();
      DynamicStatement ds = new DynamicStatement();
      if (addOrChange.getId() == null) {
        ds.addSql("INSERT INTO user_ (username, secret, fullname, accesslist");
        ds.addArguments(
            addOrChange.getUsername(),
            addOrChange.getSecret(),
            addOrChange.getFullname(),
            addOrChange.getAccess());
        if (ACSVersionCheck.adminSupported) {
          int adminInt = 0;
          if (addOrChange.isAdmin()) {
            if (requestedBy.isAdmin()) {
              adminInt = 1;
            } else {
              throw new IllegalArgumentException("Not allowed to create an admin user");
            }
          }
          ds.addSqlAndArguments(", is_admin) VALUES (?,?,?,?,?)", adminInt);
        } else {
          ds.addSql(") VALUES (?,?,?,?)");
        }
        ps = ds.makePreparedStatement(c, "id");
        ps.executeUpdate();
        ResultSet gk = ps.getGeneratedKeys();
        if (gk.next()) {
          addOrChange.setId(gk.getInt(1));
        }
        // Copy permissions from requestedBy and use them for addOrChange user
        if (!requestedBy.isAdmin()) {
          for (Permission p : requestedBy.getPermissions().getPermissions()) {
            addOrChange.addOrChangePermission(
                new Permission(addOrChange, p.getUnittypeId(), p.getProfileId()), requestedBy);
          }
        }
        nameMap.put(addOrChange.getUsername(), addOrChange);
        idMap.put(addOrChange.getId(), addOrChange);
      } else if (allowAccessTo(addOrChange, requestedBy)) {
        ds.addSqlAndArguments(
            "UPDATE user_ SET username = ?, secret = ?, ",
            addOrChange.getUsername(),
            addOrChange.getSecret());
        ds.addSqlAndArguments(
            "fullname = ?, accesslist = ?", addOrChange.getFullname(), addOrChange.getAccess());
        if (ACSVersionCheck.adminSupported) {
          int adminInt = 0;
          if (addOrChange.isAdmin()) {
            adminInt = 1;
          }
          ds.addSqlAndArguments(", is_admin = ?", adminInt);
        }
        ds.addSqlAndArguments(" WHERE id = ?", addOrChange.getId());
        ps = ds.makePreparedStatement(c);
        int rowsUpdated = ps.executeUpdate();
        if (rowsUpdated > 0) {
          nameMap.put(addOrChange.getUsername(), addOrChange);
          idMap.put(addOrChange.getId(), addOrChange);
        }
      } else {
        throw new IllegalArgumentException(
            "Not allowed to modify user " + addOrChange.getUsername());
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (c != null) {
        c.close();
      }
    }
  }

  /**
   * Permission status: Fully protected Admin: return all users UnittypeAdmin: return all users with
   * permissions limited to same unittype(s) as requestUser ProfileAdmin only: return empty list
   *
   * @param requestedBy - the user asking for a list of Users
   * @return
   */
  public User[] getUsers(User requestedBy) {
    if (requestedBy.isAdmin()) {
      User[] users = new User[nameMap.size()];
      nameMap.values().toArray(users);
      return users;
    } else {
      List<User> permittedUsers = new ArrayList<>();
      // This loop will also check requestedBy itself and add it to permittedUsers (if it is
      // unittypeAdmin)
      for (User accessTo : nameMap.values()) {
        if (allowAccessTo(accessTo, requestedBy)) {
          permittedUsers.add(accessTo);
        }
      }
      return permittedUsers.toArray(new User[] {});
    }
  }

  /**
   * Check to see if requestedBy-user has access to accessTo-user. Access will be granted if 1.
   * requestedBy-user is admin 2. requestedBy-user is unittypeAdmin for all unittypes found in
   * accessTo-user's permissions
   *
   * @param requestedBy
   * @return
   */
  private boolean allowAccessTo(User accessTo, User requestedBy) {
    if (requestedBy == null || accessTo == null) {
      return false;
    }
    if (accessTo.getUsername().equals(requestedBy.getUsername()) || requestedBy.isAdmin()) {
      return true;
    }
    if (accessTo.isAdmin()
        || requestedBy.getPermissions().getPermissions().length == 0
        || "Admin".equals(accessTo.getAccess())) {
      return false;
    }
    boolean userPermitted = true;
    for (Permission checkUserPerm : accessTo.getPermissions().getPermissions()) {
      if (requestedBy.getPermissions().getByUnittypeProfile(checkUserPerm.getUnittypeId(), null)
          == null) {
        userPermitted = false;
        break;
      }
    }
    return userPermitted;
  }

  /**
   * Raw read from the database.
   *
   * @throws SQLException
   */
  private void readAllUsers() throws SQLException {
    Connection c = null;
    Statement s = null;
    try {
      c = dataSource.getConnection();
      s = c.createStatement();
      ResultSet rs = s.executeQuery("SELECT * FROM user_");
      Map<Integer, User> tmpIdMap = new HashMap<>();
      Map<String, User> tmpNameMap = new TreeMap<>();
      while (rs.next()) {
        Integer id = rs.getInt("id");
        String username = rs.getString("username");
        String secret = rs.getString("secret");
        String fullname = rs.getString("fullname");
        String access = rs.getString("accesslist");
        Boolean isAdmin = null;
        if (username.equals(USER_ADMIN)) {
          isAdmin = true;
        } else if (ACSVersionCheck.adminSupported) {
          isAdmin = rs.getInt("is_admin") == 1;
        }
        User user = new User(username, fullname, access, isAdmin, this);
        user.setSecretHashed(secret);
        user.setId(id);
        tmpIdMap.put(id, user);
        tmpNameMap.put(username, user);
      }
      rs = s.executeQuery("SELECT * FROM permission_");
      while (rs.next()) {
        Integer id = rs.getInt("id");
        Integer userId = rs.getInt("user_id");
        Integer unittypeId = rs.getInt("unit_type_id");
        Integer profileId = null;
        String profileIdStr = rs.getString("profile_id");
        if (profileIdStr != null) {
          profileId = Integer.valueOf(profileIdStr);
        }
        User user = tmpIdMap.get(userId);
        if (user != null) {
          Permissions permissions = user.getPermissions();
          Permission permission = new Permission(user, unittypeId, profileId);
          permission.setId(id);
          permissions.add(permission);
        } else {
          throw new SQLException("The user defined in permission table is not found in user table");
        }
      }

      if (tmpNameMap.get("admin") == null) {
        User adminUser = new User(USER_ADMIN, "Admin user", ACCESS_ADMIN, true, this);
        adminUser.setSecretClearText(ADMIN_DEFAULT_PASSWORD);
        tmpNameMap.put(USER_ADMIN, adminUser);
      }
      idMap = tmpIdMap;
      nameMap = tmpNameMap;
    } finally {
      if (s != null) {
        s.close();
      }
      if (c != null) {
        c.close();
      }
    }
  }

  protected DataSource getConnectionProperties() {
    return dataSource;
  }
}
