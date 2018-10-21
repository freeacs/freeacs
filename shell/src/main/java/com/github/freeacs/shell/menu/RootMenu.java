package com.github.freeacs.shell.menu;

import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.Permission;
import com.github.freeacs.dbi.Permissions;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.Unittype.ProvisioningProtocol;
import com.github.freeacs.dbi.UnittypeParameterFlag;
import com.github.freeacs.dbi.User;
import com.github.freeacs.dbi.Users;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.shell.Session;
import com.github.freeacs.shell.UnitTempStorage;
import com.github.freeacs.shell.command.Option;
import com.github.freeacs.shell.output.Heading;
import com.github.freeacs.shell.output.Line;
import com.github.freeacs.shell.output.Listing;
import com.github.freeacs.shell.output.OutputHandler;
import com.github.freeacs.shell.sync.Sync;
import com.github.freeacs.shell.testperf.MakeTestperfUnits;
import com.github.freeacs.shell.transform.Transform;
import com.github.freeacs.shell.util.Validation;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RootMenu {
  private static Pattern hashedSecretPattern = Pattern.compile("[A-F0-9]{40}");

  private Session session;

  public RootMenu(Session session) {
    this.session = session;
  }

  public void executescript() throws Exception {
    if (!session.getBatchStorage().getAddChangeUnittypeParameters().isEmpty()) {
      try {
        Unittype ut =
            session.getBatchStorage().getAddChangeUnittypeParameters().get(0).getUnittype();
        ut.getUnittypeParameters()
            .addOrChangeUnittypeParameters(
                session.getBatchStorage().getAddChangeUnittypeParameters(), session.getAcs());
        session.getBatchStorage().setAddChangeUnittypeParameters(null);
      } catch (Exception t) {
        session.getBatchStorage().setAddChangeUnittypeParameters(null);
        throw t;
      }
    }
    if (session.getContext().getProfile() != null) {
      UnitTempStorage uts = null;
      ACSUnit xapsU = session.getAcsUnit();
      try {
        uts = session.getBatchStorage().getAddUnits();
        for (Entry<Profile, List<String>> entry : uts.getUnits().entrySet()) {
          session.getAcsUnit().addUnits(entry.getValue(), entry.getKey());
        }
        uts.reset();
        if (!session.getBatchStorage().getAddChangeUnitParameters().isEmpty()) {
          xapsU.addOrChangeUnitParameters(
              session.getBatchStorage().getAddChangeUnitParameters(),
              session.getContext().getProfile());
          session.getBatchStorage().setAddChangeUnitParameters(null);
        }
        if (!session.getBatchStorage().getDeleteUnitParameters().isEmpty()) {
          xapsU.deleteUnitParameters(session.getBatchStorage().getDeleteUnitParameters());
          session.getBatchStorage().setDeleteUnitParameters(null);
          session.println("The unit parameters scheduled for deletion are deleted");
        }
        uts = session.getBatchStorage().getDeleteUnits();
        boolean unitsDeleted = false;
        for (Entry<Profile, List<String>> entry : uts.getUnits().entrySet()) {
          session.getAcsUnit().deleteUnits(entry.getValue());
          unitsDeleted = true;
        }
        if (unitsDeleted) {
          session.println("The units scheduled for deletion are deleted");
        }
        uts.reset();
      } catch (Exception t) {
        if (uts != null) {
          uts.reset();
        }
        throw t;
      }
    }
    if (!session.getBatchStorage().getDeleteUnittypeParameters().isEmpty()) {
      try {
        Unittype ut = session.getBatchStorage().getDeleteUnittypeParameters().get(0).getUnittype();
        ut.getUnittypeParameters()
            .deleteUnittypeParameters(
                session.getBatchStorage().getDeleteUnittypeParameters(), session.getAcs());
        session.getBatchStorage().setDeleteUnittypeParameters(null);
        session.println("The unit type parameters scheduled for deletion are deleted");
      } catch (Exception t) {
        session.getBatchStorage().setDeleteUnittypeParameters(null);
        throw t;
      }
    }
  }

  private void listperms(String[] args, OutputHandler oh) {
    Users users = session.getUsers();
    Listing listing = oh.getListing();
    listing.setHeading("Username", "Unittype", "Profile");
    for (User user : users.getUsers(session.getVerifiedFusionUser())) {
      Permission[] permissions = user.getPermissions().getPermissions();
      for (Permission p : permissions) {
        Unittype unittype = session.getAcs().getUnittype(p.getUnittypeId());
        if (!Validation.matches(args.length > 1 ? args[1] : null, unittype.getName())) {
          continue;
        }
        Line line = new Line(user.getUsername(), unittype.getName());
        Profile profile = unittype.getProfiles().getById(p.getProfileId());
        if (profile != null) {
          line.addValue(profile.getName());
        } else {
          line.addValue("NULL");
        }
        listing.addLine(line);
      }
    }
  }

  private void listusers(String[] args, OutputHandler oh) {
    Users users = session.getUsers();
    Listing listing = oh.getListing();
    listing.setHeading("Username", "Full name", "Secret (hashed)", "Access", "Admin");
    for (User user : users.getUsers(session.getVerifiedFusionUser())) {
      if (!Validation.matches(
          args.length > 1 ? args[1] : null, user.getUsername(), user.getAccess())) {
        continue;
      }
      Line line = new Line(user.getUsername(), user.getFullname(), user.getSecret());
      if (user.getAccess() != null && user.getAccess().indexOf('[') > 0) {
        int startPos = user.getAccess().indexOf('[');
        int endPos = user.getAccess().indexOf(']');
        line.addValue(user.getAccess().substring(startPos + 1, endPos));
      } else {
        line.addValue(user.getAccess());
      }
      line.addValue(Boolean.toString(user.isAdmin()));
      listing.addLine(line);
    }
  }

  private void listunittypes(String[] args, OutputHandler oh) {
    Unittype[] unittypes = session.getAcs().getUnittypes().getUnittypes();
    Listing listing = oh.getListing();
    if (oh.getCommand().getOptions().containsKey(Option.OPTION_LIST_ALL_COLUMNS)) {
      listing.setHeading(Listing.HEADER_UNITTYPE, "Protocol", "Vendor", "Description");
      for (Unittype ut : unittypes) {
        if (!Validation.matches(
            args.length > 1 ? args[1] : null, ut.getName(), ut.getVendor(), ut.getDescription())) {
          continue;
        }
        Line line = new Line();
        line.addValue(ut.getName());
        line.addValue(ut.getProtocol().toString());
        line.addValue(ut.getVendor());
        line.addValue(ut.getDescription());
        listing.addLine(line);
      }
    } else {
      listing.setHeading(Listing.HEADER_UNITTYPE);
      for (Unittype ut : unittypes) {
        if (!Validation.matches(args.length > 1 ? args[1] : null, ut.getName())) {
          continue;
        }
        listing.addLine(ut.getName());
      }
    }
  }

  private void delperm(String[] args) throws Exception {
    Validation.numberOfArgs(args, 3);
    String username = args[1];
    String unittypeName = args[2];
    String profileName = "NULL";
    if (args.length > 3) {
      profileName = args[3];
    }
    Users users = session.getUsers();
    User user = users.getProtected(username, session.getVerifiedFusionUser());
    if (user == null) {
      throw new IllegalArgumentException("The user " + username + " does not exist");
    }
    Permissions permissions = user.getPermissions();
    Unittype unittype = session.getAcs().getUnittype(unittypeName);
    if (unittype == null) {
      throw new IllegalArgumentException("The unittype " + unittypeName + " does not exist");
    }
    Integer profileId = null;
    if (!"NULL".equals(profileName)) {
      Profile profile = unittype.getProfiles().getByName(profileName);
      if (profile == null) {
        throw new IllegalArgumentException("The profile " + profileName + " does not exist");
      }
      profileId = profile.getId();
    }
    Permission p = permissions.getByUnittypeProfile(unittype.getId(), profileId);
    if (p == null) {
      throw new IllegalArgumentException("The permission does not exist");
    }
    user.deletePermission(p);
    session.println("The permission was deleted");
  }

  private void deluser(String[] args) throws Exception {
    Validation.numberOfArgs(args, 2);
    Users users = session.getUsers();
    String username = args[1];
    User user = users.getProtected(username, session.getVerifiedFusionUser());
    if (user == null) {
      session.println("The user " + username + " does not exist.");
    } else {
      users.delete(user, session.getVerifiedFusionUser());
      session.println("The user " + username + " is deleted.");
    }
  }

  private void setperm(String[] args) throws Exception {
    Validation.numberOfArgs(args, 3);
    String username = args[1];
    String unittypeName = args[2];
    String profileName = "NULL";
    if (args.length > 3) {
      profileName = args[3];
    }
    Users users = session.getUsers();
    User user = users.getProtected(username, session.getVerifiedFusionUser());
    if (user == null) {
      throw new IllegalArgumentException("The user " + username + " does not exist");
    }
    Permissions permissions = user.getPermissions();
    Unittype unittype = session.getAcs().getUnittype(unittypeName);
    if (unittype == null) {
      throw new IllegalArgumentException("The unittype " + unittypeName + " does not exist");
    }
    Integer profileId = null;
    if (!"NULL".equals(profileName)) {
      Profile profile = unittype.getProfiles().getByName(profileName);
      if (profile == null) {
        throw new IllegalArgumentException("The profile " + profileName + " does not exist");
      }
      profileId = profile.getId();
    }
    Permission p = permissions.getByUnittypeProfile(unittype.getId(), profileId);
    String action = "touched (no change)";
    if (p == null) {
      action = "added";
      p = new Permission(user, unittype.getId(), profileId);
    }
    user.addOrChangePermission(p, session.getVerifiedFusionUser());
    session.println("The permission is " + action);
  }

  private void setuser(String[] args) throws Exception {
    Validation.numberOfArgs(args, 5);
    Users users = session.getUsers();
    String username = args[1];
    String fullname = args[2];
    String secret = args[3];
    String accessStr = args[4];
    boolean admin = false;
    if (args.length > 5) {
      admin = "true".equals(args[5].toLowerCase());
    } else if ("admin".equals(username)) {
      admin = true;
    }
    if ("NULL".equals(accessStr)) {
      accessStr = "";
    }
    if (accessStr != null && !"".equals(accessStr.trim())) {
      if (username.equalsIgnoreCase(Users.USER_ADMIN)) {
        accessStr = Users.ACCESS_ADMIN;
      } else {
        Set<String> webPages = new HashSet<>();
        java.util.Collections.addAll(webPages, Permissions.WEB_PAGES);
        // OLD: { "search", "unit", "profile", "unittype", "group", "job", "software", "syslog",
        // "report", "monitor", "staging" };
        // NEW { "support", "limited-provisioning", "full-provisioning", "report", "staging",
        // "monitor" };
        // Convert from OLD to NEW
        accessStr = accessStr.replace("search", "support");
        accessStr = accessStr.replace("unittype", "full-provisioning");
        accessStr = accessStr.replace("unit", "support");
        accessStr = accessStr.replace("profile", "limited-provisioning");
        accessStr = accessStr.replace("group", "full-provisioning");
        accessStr = accessStr.replace("job", "full-provisioning");
        accessStr = accessStr.replace("software", "full-provisioning");
        accessStr = accessStr.replace("syslog", "support");
        String[] accessArr = accessStr.split(",");
        String newAccess = "";
        for (String a : accessArr) {
          String access = a.trim();
          if (!webPages.contains(access) && !a.equals(Users.ACCESS_ADMIN)) {
            throw new IllegalArgumentException("The access " + a + " is not valid");
          }
          newAccess += access + ",";
        }
        if (newAccess.endsWith(",")) {
          newAccess = newAccess.substring(0, newAccess.length() - 1);
        }
        accessStr = "WEB[" + newAccess + "]";
      }
    }
    User user = users.getProtected(username, session.getVerifiedFusionUser());
    String action = "changed";
    if (user == null) {
      user = new User(username, fullname, accessStr, admin, users);
      action = "added";
    } else {
      user.setUsername(username);
      user.setFullname(fullname);
      user.setAccess(accessStr);
      user.setAdmin(admin);
    }
    Matcher m = hashedSecretPattern.matcher(secret);
    if (m.matches()) {
      user.setSecretHashed(secret);
    } else {
      user.setSecretClearText(secret);
    }
    users.addOrChange(user, session.getVerifiedFusionUser());
    session.println("The user " + user.getUsername() + " is " + action + ".");
  }

  private void setunittype(String[] args, OutputHandler oh) throws Exception {
    Validation.numberOfArgs(args, 4);
    if (args.length >= 5) {
      Unittype unittype = session.getAcs().getUnittype(args[1]);
      String action = "";
      if (unittype == null) {
        unittype = new Unittype(args[1], args[3], args[4], ProvisioningProtocol.toEnum(args[2]));
        action = "added";
      } else {
        unittype.setProtocol(ProvisioningProtocol.toEnum(args[2]));
        unittype.setVendor(args[3]);
        unittype.setDescription(args[4]);
        action = "changed";
      }
      session.getAcs().getUnittypes().addOrChangeUnittype(unittype, session.getAcs());
      session.println("The unittype " + unittype.getName() + " is " + action + ".");
    } else if (args.length == 4) {
      Sync.main(args, session, oh);
    }
  }

  private void delunittype(String[] args) throws Exception {
    Validation.numberOfArgs(args, 2);
    Unittype unittype = session.getAcs().getUnittype(args[1]);
    if (unittype == null) {
      session.println("The unittype does not exist.");
    } else {
      session.getAcs().getUnittypes().deleteUnittype(unittype, session.getAcs(), true);
      session.println("The unittype is deleted.");
    }
  }

  private void listparams(String[] args, OutputHandler oh) {
    Validation.numberOfArgs(args, 1);
    Listing listing = oh.getListing();
    listing.setHeading("Unit Type Parameter Name", "Flag");
    if (args.length > 1 && "staging".equals(args[1])) {
      for (Entry<String, UnittypeParameterFlag> entry :
          SystemParameters.stagingParameters.entrySet()) {
        listing.addLine(entry.getKey(), entry.getValue().getFlag());
      }
    } else {
      for (Entry<String, UnittypeParameterFlag> entry :
          SystemParameters.commonParameters.entrySet()) {
        listing.addLine(entry.getKey(), entry.getValue().getFlag());
      }
    }
  }

  /**
   * Returns true : has processed a cd-command Returns false : has processed another command
   * (everything else)
   */
  public boolean execute(String[] inputArr, OutputHandler oh) throws Exception {
    if (inputArr[0].startsWith("delperm")) {
      delperm(inputArr);
    } else if (inputArr[0].startsWith("delunit")) {
      delunittype(inputArr);
    } else if (inputArr[0].startsWith("deluser")) {
      deluser(inputArr);
    } else if (inputArr[0].startsWith("listpe")) {
      listperms(inputArr, oh);
    } else if (inputArr[0].startsWith("listpa")) {
      listparams(inputArr, oh);
    } else if (inputArr[0].startsWith("listunitt")) {
      listunittypes(inputArr, oh);
    } else if (inputArr[0].startsWith("listunits")) {
      listunits(inputArr, oh);
    } else if (inputArr[0].startsWith("listus")) {
      listusers(inputArr, oh);
    } else if (inputArr[0].startsWith("setun")) {
      setunittype(inputArr, oh);
    } else if (inputArr[0].startsWith("setus")) {
      setuser(inputArr);
    } else if (inputArr[0].startsWith("setpe")) {
      setperm(inputArr);
    } else if ("maketestperfunits".equals(inputArr[0])) {
      makeTestperfUnits(inputArr);
    } else if ("maketransform".equals(inputArr[0])) {
      Transform.transform(session, inputArr[1], inputArr[2]);
    } else if ("executescript".equals(inputArr[0])) {
      session.println("The command does not apply here.");
    } else {
      throw new IllegalArgumentException("The command " + inputArr[0] + " was not recognized.");
    }

    return false;
  }

  private void makeTestperfUnits(String[] inputArr) throws Exception {
    String[] args = new String[3];
    if (inputArr.length == 4) {
      args[0] = inputArr[1];
      args[1] = inputArr[2];
      args[2] = inputArr[3];
    } else {
      if (inputArr.length == 3) {
        args[0] = inputArr[1];
        args[1] = inputArr[2];
      } else if (inputArr.length == 2) {
        args = findRange(inputArr[1], session);
        //			addUpgradeSWJob(session);
        makeTCScript(args);
      } else {
        args[0] = "0";
        args[1] = "99";
      }
      args[2] = "internal/owera/groups/testperf.input";
    }
    MakeTestperfUnits.execute(args);
  }

  private void makeTCScript(String[] args) throws IOException {
    FileWriter fw = new FileWriter("internal/owera/scripts/runtestclients.sh");
    fw.write("#!/bin/sh\n");
    fw.write("java -jar xapstr069tc.jar -t 1 -m 300 -s 1 -b 1000 -d 3 ");
    fw.write("-u https://localhost/xapstr069 ");
    fw.write("-r 0-" + args[0] + " &\n");
    fw.write("java -jar xapstr069tc.jar -t 1 -m 300 -s 1 -b 1000 -d 3 ");
    fw.write("-u https://localhost/xapstr069 ");
    fw.write("-r " + args[0] + "-" + args[1] + " &\n");
    fw.close();
  }

  private String[] findRange(String arg0, Session session) throws SQLException {
    String[] args = new String[3];
    int add = Integer.parseInt(arg0);
    ACSUnit acsUnit = session.getAcsUnit();
    int i = 0;
    Unit unit = null;
    do {
      String unitId = String.format("000000-TR069TestClient-%012d", i);
      unit = acsUnit.getUnitById(unitId);
      i = i + add;
    } while (unit != null);
    args[0] = String.valueOf(i - add);
    args[1] = String.valueOf(i);
    return args;
  }

  private Map<String, Unit> getUnits(String[] args) throws Exception {
    Map<String, Unit> units = null;
    if (args.length > 1) {
      units = session.getAcsUnit().getUnits("%" + args[1] + "%", null, null, null);
    } else {
      units = session.getAcsUnit().getUnits((String) null, null, null, null);
    }
    return units;
  }

  private void listunits(String[] inputArr, OutputHandler oh) throws Exception {
    Listing listing = oh.getListing();
    listing.setHeading(new Heading(new Line("Unit Id")), true);
    Map<String, Unit> units = getUnits(inputArr);
    for (Unit unit : units.values()) {
      listing.addLine(new Line(unit.getId()), unit);
    }
  }
}
