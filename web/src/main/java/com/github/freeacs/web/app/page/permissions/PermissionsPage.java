package com.github.freeacs.web.app.page.permissions;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Permission;
import com.github.freeacs.dbi.Permissions;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.User;
import com.github.freeacs.dbi.Users;
import com.github.freeacs.web.Page;
import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.InputDataIntegrity;
import com.github.freeacs.web.app.input.InputDataRetriever;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.util.ACSLoader;
import com.github.freeacs.web.app.util.SessionCache;
import com.github.freeacs.web.app.util.SessionData;
import com.github.freeacs.web.app.util.WebConstants;
import com.github.freeacs.web.security.WebUser;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;

/**
 * Manages users and their permissions.
 *
 * @author Jarl Andre Hubenthal
 */
@SuppressWarnings("rawtypes")
public class PermissionsPage extends AbstractWebPage {
  /** The user. */
  private User user;

  /** The users. */
  private Users users;

  /** The xaps. */
  private ACS acs;

  /** The input data. */
  private PermissionsData inputData;

  /** The session id. */
  private String sessionId;

  public void process(
      ParameterParser req,
      Output outputHandler,
      DataSource xapsDataSource,
      DataSource syslogDataSource)
      throws Exception {
    inputData = (PermissionsData) InputDataRetriever.parseInto(new PermissionsData(), req);

    sessionId = req.getSession().getId();

    InputDataIntegrity.loadAndStoreSession(req, outputHandler, inputData, inputData.getUnittype());
    SessionData sessionData = SessionCache.getSessionData(sessionId);
    WebUser loggedInUser = sessionData.getUser();

    acs = ACSLoader.getXAPS(sessionId, xapsDataSource, syslogDataSource);
    if (acs == null) {
      outputHandler.setRedirectTarget(WebConstants.LOGIN_URI);
      return;
    }

    String template = null;

    Map<String, Object> root = outputHandler.getTemplateMap();

    users = new Users(xapsDataSource);

    if (inputData.getUser().getString() != null) {
      user = users.getProtected(inputData.getUser().getString(), loggedInUser);
    }

    String button = inputData.getDetailSubmit().getString();
    if (button != null) {
      if ("Update user".equals(button) && user != null) {
        String username = inputData.getUsername().getString();
        String fullname = inputData.getFullname().getString();
        String password = inputData.getPassword().getString();
        if (isValidString(username)) {
          user.setUsername(username);
        }
        if (isValidString(fullname)) {
          user.setFullname(fullname);
        }
        if (isValidString(password)) {
          user.setSecretClearText(password);
        }
        user.setAdmin(inputData.getAdmin().getBoolean());
        String configure = req.getParameter("configure");
        if ("true".equals(configure)) {
          user.setAccess(getModules());
        } else if (user.isAdmin()) {
          user.setAccess(Users.ACCESS_ADMIN);
        }
        users.addOrChange(user, loggedInUser);
        setPermissions(user, true);
        root.put("message", "Successfully updated user");
        root.put("submitted", true);
      } else if ("Create new user".equals(button)) {
        String username = inputData.getUsername().getString();
        String fullname = inputData.getFullname().getString();
        String password = inputData.getPassword().getString();
        boolean isAdmin = inputData.getAdmin().getBoolean();
        String modules = getModules();
        if (username != null && fullname != null && password != null && modules != null) {
          User userExists = users.getUnprotected(username);
          if (userExists == null || "admin".equals(username)) {
            User newUser = new User(username, fullname, modules, isAdmin, users);
            newUser.setSecretClearText(password);
            users.addOrChange(newUser, loggedInUser);
            setPermissions(newUser, false);
            inputData.getUsername().setValue(null);
            inputData.getPassword().setValue(null);
            inputData.getFullname().setValue(null);
            inputData.getWebAccess().setValue(null);
            inputData.getShellAccess().setValue(null);
            inputData.getPermission().setValue(null);
            inputData.getAdmin().setValue(false);
            if (newUser.getId() != null) {
              req.getHttpServletRequest()
                  .getSession()
                  .setAttribute("message", "Successfully added user \"" + username + "\"");
              outputHandler.setDirectToPage(Page.PERMISSIONS, "async=true&header=true");
              return;
            }
          } else if (userExists.getId() != null) {
            root.put("message", "Username \"" + username + "\" already exists.");
          }
        } else {
          root.put("message", "Fill in all fields");
        }
        root.put("submitted", true);
      }
    }

    if (req.getHttpServletRequest().getSession().getAttribute("message") != null) {
      root.put("message", req.getHttpServletRequest().getSession().getAttribute("message"));
      req.getHttpServletRequest().getSession().removeAttribute("message");
    }

    if (inputData.getCmd().hasValue("delete") && user != null) {
      users.delete(user, loggedInUser);
      user = null;
    }

    root.put("async", req.getParameter("async"));

    if (user != null) {
      template = "/permissions/permissionsdetailspage.ftl";
      root.put("user", user);
      root.put("permissions", user.getPermissions().getPermissions());
      root.put("usr_pages", getWebAccess());
    } else if (inputData.getCmd().hasValue("create")) {
      template = "/permissions/permissionscreatepage.ftl";
      root.put("usr_pages", inputData.getWebAccess().getStringArray());
    } else {
      template = "/permissions/permissionsoverviewpage.ftl";
      root.put("users", users.getUsers(loggedInUser));
      root.put("ACCESS_ADMIN", Users.ACCESS_ADMIN);
    }

    root.put("username", inputData.getUsername().getString());
    root.put("password", inputData.getPassword().getString());
    root.put("admin", inputData.getAdmin().getString());
    root.put("fullname", inputData.getFullname().getString());
    root.put("all_pages", getAllPermissiblePages());

    if (root.get("permissions") == null) {
      root.put("permissions", getTemporaryPermissions());
    } else {
      Permission[] userPerms = (Permission[]) root.get("permissions");
      List<Permission> userPermsList = new ArrayList<>(Arrays.asList(userPerms));
      Permission[] tempPerms = getTemporaryPermissions();
      if (tempPerms != null) {
        for (Permission perm : tempPerms) {
          boolean found = false;
          for (Permission userPerm : userPermsList) {
            if (isPermissionsEqual(perm, userPerm)) {
              found = true;
              break;
            }
          }
          if (!found) {
            userPermsList.add(perm);
          }
        }
      }
      root.put("permissions", userPermsList);
    }
    root.put("getunittypename", new GetUnittypeName());
    root.put("getprofilename", new GetProfileName());
    root.put("unittypes", acs.getUnittypes().getUnittypes());
    String utString = inputData.getUnittype().getString();
    if (utString != null) {
      root.put("unittype", utString);
      Unittype unittype = acs.getUnittype(utString);
      if (unittype != null) {
        root.put(
            "profiles", getAllowedProfiles(sessionId, unittype, xapsDataSource, syslogDataSource));
        root.put("profile", inputData.getProfile().getString());
      }
    }

    outputHandler.setTemplatePath(template);
  }

  /**
   * Gets the modules.
   *
   * @return the modules
   */
  private String getModules() {
    String[] pages = inputData.getWebAccess().getStringArray();

    String modules = "";
    if (pages != null && pages.length > 0) {
      modules = "WEB[" + StringUtils.join(pages, ",") + "]";
    }

    if ("".equals(modules)) {
      modules = Users.ACCESS_ADMIN;
    }

    return modules;
  }

  /**
   * Gets the web access.
   *
   * @return the web access
   */
  private String[] getWebAccess() {
    String access = user.getAccess() != null ? user.getAccess().split(";")[0] : "";
    if (access.startsWith("WEB[") && access.endsWith("]")) {
      access = access.substring(access.indexOf('[') + 1);
      access = access.substring(0, access.length() - 1);
      String[] arr = access.split(",");
      List<Page> pages = Page.getPageValuesFromList(Arrays.asList(arr));
      List<String> friendly = new ArrayList<>();
      for (Page p : pages) {
        for (Entry<String, Page> entry : Page.getPermissiblePageMap().entrySet()) {
          if (entry.getValue() == p) {
            friendly.add(entry.getKey());
          }
        }
      }
      return friendly.toArray(new String[] {});
    }
    return new String[] {};
  }

  /** The Class PagePermission. */
  public class PagePermission {
    /**
     * Gets the key.
     *
     * @return the key
     */
    public String getKey() {
      return key;
    }

    /**
     * Sets the key.
     *
     * @param key the new key
     */
    public void setKey(String key) {
      this.key = key;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue() {
      return value;
    }

    /**
     * Sets the value.
     *
     * @param value the new value
     */
    public void setValue(String value) {
      this.value = value;
    }

    /** The key. */
    private String key;

    /** The value. */
    private String value;
  }

  /**
   * Gets the all permissible pages.
   *
   * @return the all permissible pages
   */
  private List<PagePermission> getAllPermissiblePages() {
    List<PagePermission> pagesList = new ArrayList<>();
    Collection<String> defaultPages = Page.getPermissiblePageMap().keySet();
    for (String page : defaultPages) {
      PagePermission pageObject = new PagePermission();
      pageObject.setKey(page);
      pageObject.setValue(page.toLowerCase());
      pagesList.add(pageObject);
    }
    return pagesList;
  }

  /**
   * Gets the temporary permissions.
   *
   * @return the temporary permissions
   * @throws IllegalArgumentException the illegal argument exception
   * @throws SecurityException the security exception
   * @throws IllegalAccessException the illegal access exception
   * @throws InvocationTargetException the invocation target exception
   * @throws NoSuchMethodException the no such method exception
   */
  private Permission[] getTemporaryPermissions()
      throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    String[] permissions = inputData.getPermission().getStringArray();
    if (permissions == null) {
      return null;
    }
    List<Permission> perms = new ArrayList<>();
    for (String permission : permissions) {
      if (permission == null) {
        continue;
      }
      String[] permDetails = permission.split("\\\\");
      if (permDetails.length > 1) {
        Unittype unittype = acs.getUnittype(permDetails[0]);
        Profile profile = unittype.getProfiles().getByName(permDetails[1]);
        if (unittype != null && profile != null) {
          perms.add(new Permission(null, unittype.getId(), profile.getId()));
        }
      } else if (permDetails.length > 0) {
        Unittype unittype = acs.getUnittype(permDetails[0]);
        if (unittype != null) {
          perms.add(new Permission(null, unittype.getId(), null));
        }
      }
    }
    return perms.toArray(new Permission[] {});
  }

  /** The Class GetUnittypeName. */
  public class GetUnittypeName implements TemplateMethodModel {
    public Object exec(List args) throws TemplateModelException {
      if (args.isEmpty()) {
        throw new TemplateModelException("Specify Unit Type Id");
      }
      Integer unittypeId = Integer.parseInt((String) args.get(0));
      Unittype unittype = acs.getUnittype(unittypeId);
      return new SimpleScalar(unittype.getName());
    }
  }

  /** The Class GetProfileName. */
  public class GetProfileName implements TemplateMethodModel {
    public Object exec(List args) throws TemplateModelException {
      if (args.size() < 2) {
        throw new TemplateModelException("Specify Unit Type Id and Profile Id");
      }
      Integer unittypeId = Integer.parseInt((String) args.get(1));
      Integer profileId = Integer.parseInt((String) args.get(0));
      Unittype unittype = acs.getUnittype(unittypeId);
      Profile profile = unittype.getProfiles().getById(profileId);
      if (profile != null) {
        return new SimpleScalar(profile.getName());
      }
      return new SimpleScalar("N/A (id: " + profileId + ")");
    }
  }

  /**
   * Sets the permissions.
   *
   * @param newUser the new user
   * @param update the update
   * @throws IllegalArgumentException the illegal argument exception
   * @throws SecurityException the security exception
   * @throws IllegalAccessException the illegal access exception
   * @throws InvocationTargetException the invocation target exception the no available connection
   *     exception
   * @throws SQLException the sQL exception
   */
  private void setPermissions(User newUser, boolean update)
      throws IllegalAccessException, InvocationTargetException, SQLException {
    Permissions perms = newUser.getPermissions();

    List<Permission> toAdd = new ArrayList<>();
    SessionData sessionData = SessionCache.getSessionData(sessionId);
    WebUser loggedInUser = sessionData.getUser();

    String[] permissions = inputData.getPermission().getStringArray();
    if (permissions != null) {
      for (String permission : permissions) {
        String[] permDetails = permission.split("\\\\");
        if (permDetails.length > 1) {
          Unittype unittype = acs.getUnittype(permDetails[0]);
          Profile profile = unittype.getProfiles().getByName(permDetails[1]);
          if (newUser.getId() != null && unittype != null && profile != null) {
            toAdd.add(new Permission(newUser, unittype.getId(), profile.getId()));
          }
        } else if (permDetails.length > 0) {
          Unittype unittype = acs.getUnittype(permDetails[0]);
          if (newUser.getId() != null && unittype != null) {
            toAdd.add(new Permission(newUser, unittype.getId(), null));
          }
        }
      }
    }

    if (update) {
      // Delete old permissions, if they are not present
      for (Permission old : perms.getPermissions()) {
        boolean amongToBeAdded = false;
        for (Permission newPerm : toAdd) {
          if (amongToBeAdded = isPermissionsEqual(newPerm, old)) {
            break;
          }
        }
        if (!amongToBeAdded) {
          newUser.deletePermission(old);
        }
      }
      // Add permissions, if they are not already present
      for (Permission newPerm : toAdd) {
        boolean alreadyPresent = false;
        for (Permission old : perms.getPermissions()) {
          if (alreadyPresent = isPermissionsEqual(newPerm, old)) {
            break;
          }
        }
        if (!alreadyPresent) {
          newUser.addOrChangePermission(newPerm, loggedInUser);
        }
      }
    } else {
      for (Permission permissionToAdd : toAdd) {
        newUser.addOrChangePermission(permissionToAdd, loggedInUser);
      }
    }
  }

  /**
   * Checks if is permissions equal.
   *
   * @param perm1 the perm1
   * @param perm2 the perm2
   * @return true, if is permissions equal
   */
  private boolean isPermissionsEqual(Permission perm1, Permission perm2) {
    if (perm1.getUnittypeId().intValue() == perm2.getUnittypeId().intValue()) {
      if (perm1.getProfileId() != null && perm2.getProfileId() != null) {
        return perm1.getProfileId().intValue() == perm2.getProfileId().intValue();
      } else {
        return perm1.getProfileId() == null && perm2.getProfileId() == null;
      }
    }
    return false;
  }
}
