package com.github.freeacs.shell.menu;

import com.github.freeacs.dbi.Parameter;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.ProfileParameter;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.shell.Context;
import com.github.freeacs.shell.Session;
import com.github.freeacs.shell.UnitTempStorage;
import com.github.freeacs.shell.command.Option;
import com.github.freeacs.shell.output.Heading;
import com.github.freeacs.shell.output.Line;
import com.github.freeacs.shell.output.Listing;
import com.github.freeacs.shell.output.OutputHandler;
import com.github.freeacs.shell.util.Validation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ProfileMenu {
  private Session session;
  private Context context;

  public ProfileMenu(Session session) {
    this.session = session;
    this.context = session.getContext();
  }

  private void listparams(String[] inputArr, OutputHandler oh) throws Exception {
    ProfileParameter[] profileParameters =
        context.getProfile().getProfileParameters().getProfileParameters();
    Listing listing = oh.getListing();
    listing.setHeading("Unittype parameter name", "Value");
    for (ProfileParameter profileParameter : profileParameters) {
      if (!Validation.matches(
          inputArr.length > 1 ? inputArr[1] : null,
          profileParameter.getUnittypeParameter().getName(),
          profileParameter.getValue())) {
        continue;
      }
      listing.addLine(
          profileParameter.getUnittypeParameter().getName(), profileParameter.getValue());
    }
  }

  private void setparam(String[] args) throws Exception {
    Validation.numberOfArgs(args, 3);
    ProfileParameter profileParameter =
        context.getProfile().getProfileParameters().getByName(args[1]);
    String action = "";
    UnittypeParameter up = context.getUnittype().getUnittypeParameters().getByName(args[1]);
    if (up == null) {
      throw new IllegalArgumentException(
          "The profile parameter "
              + args[1]
              + " is not a unittype parameter in unittype "
              + context.getUnittype().getName());
    } else {
      if ("NULL".equals(args[2])) {
        if (profileParameter == null) {
          action = "ignored (parameter was set to NULL and did not exist in database)";
        } else {
          action = "deleted (parameter was set to NULL)";
          context
              .getProfile()
              .getProfileParameters()
              .deleteProfileParameter(profileParameter, session.getAcs());
        }
      } else {
        if (profileParameter == null) {
          profileParameter = new ProfileParameter(context.getProfile(), up, args[2]);
          action = "added";
        } else {
          profileParameter.setValue(args[2]);
          action =
              "changed (parameter existed in database, this message does not imply a change of value)";
        }
        context
            .getProfile()
            .getProfileParameters()
            .addOrChangeProfileParameter(profileParameter, session.getAcs());
      }
      session.println(
          "["
              + session.getCounter()
              + "] The profile parameter "
              + args[1]
              + " is "
              + action
              + ".");
      session.incCounter();
    }
  }

  private void delparam(String[] args) throws Exception {
    Validation.numberOfArgs(args, 2);
    ProfileParameter profileParameter =
        context.getProfile().getProfileParameters().getByName(args[1]);
    if (profileParameter == null) {
      throw new IllegalArgumentException("The profile parameter does not exist.");
    } else {
      context
          .getProfile()
          .getProfileParameters()
          .deleteProfileParameter(profileParameter, session.getAcs());
      session.println(
          "[" + session.getCounter() + "] The profile parameter " + args[1] + " is deleted.");
      session.incCounter();
    }
  }

  private void listunits(String[] args, OutputHandler oh) throws Exception {
    Listing listing = oh.getListing();
    Line headingLine = new Line("Unit Id");
    if (oh.getCommand().getOptions().containsKey(Option.OPTION_LIST_ALL_COLUMNS)) {
      Map<String, String> displayableMap =
          context.getUnittype().getUnittypeParameters().getDisplayableNameMap();
      for (String shortName : displayableMap.values()) {
        headingLine.addValue(shortName);
      }
    }
    listing.setHeading(new Heading(headingLine));
    Map<String, Unit> units = getUnitMap(args);
    for (Entry<String, Unit> entry : units.entrySet()) {
      String unitId = entry.getKey();
      Line line = new Line(unitId);
      if (oh.getCommand().getOptions().containsKey(Option.OPTION_LIST_ALL_COLUMNS)) {
        Map<String, String> displayableMap =
            context.getUnittype().getUnittypeParameters().getDisplayableNameMap();
        Unit unit = session.getAcsUnit().getUnitById(unitId);
        for (String utpName : displayableMap.keySet()) {
          String value = unit.getParameters().get(utpName);
          if (value != null) {
            line.addValue(value);
          } else {
            line.addValue("NULL");
          }
        }
      }
      listing.addLine(line, entry.getValue());
    }
  }

  private void setunit(String[] args) throws Exception {
    Validation.numberOfArgs(args, 2);
    UnitTempStorage uts = session.getBatchStorage().getAddUnits();
    uts.addUnit(context.getProfile(), args[1]);
    session.println(
        "["
            + session.getCounter()
            + "] The unit "
            + args[1]
            + " was added/changed (context: "
            + context
            + ")");
    if (uts.size() == 1000) {
      for (Entry<Profile, List<String>> entry : uts.getUnits().entrySet()) {
        session.getAcsUnit().addUnits(entry.getValue(), entry.getKey());
      }
      uts.reset();
    }
    session.incCounter();
  }

  private void delunit(String[] args) throws Exception {
    Validation.numberOfArgs(args, 2);
    UnitTempStorage uts = session.getBatchStorage().getDeleteUnits();
    uts.addUnit(context.getProfile(), args[1]);
    session.println(
        "[" + session.getCounter() + "] The unit " + args[1] + " is scheduled for deletion");
    if (uts.size() == 1000) {
      for (Entry<Profile, List<String>> entry : uts.getUnits().entrySet()) {
        session.getAcsUnit().deleteUnits(entry.getValue());
      }
      context.println("The units scheduled for deletion are deleted");
      uts.reset();
    }
    session.incCounter();
  }

  private void delallunits() throws Exception {
    session.getAcsUnit().deleteUnits(context.getProfile());
    session.println("All units (in this profile) were deleted");
  }

  private void moveunit(String[] args) throws Exception {
    Validation.numberOfArgs(args, 3);
    Unit unit =
        session.getAcsUnit().getUnitById(args[1], context.getUnittype(), context.getProfile());
    Profile profile = context.getUnittype().getProfiles().getByName(args[2]);
    if (unit != null && profile != null) {
      List<String> unitIds = new ArrayList<>();
      unitIds.add(args[1]);
      session.getAcsUnit().moveUnits(unitIds, profile);
      session.println(
          "[" + session.getCounter() + "] The unit was moved to profile " + profile.getName());
    } else {
      session.println("[" + session.getCounter() + "] The unit does not exist");
    }
    session.incCounter();
  }

  /**
   * Returns true : has processed a cd-command Returns false : has processed another command
   * (everything else)
   */
  public boolean execute(String[] inputArr, OutputHandler oh) throws Exception {
    String cmd = inputArr[0];
    if (cmd.startsWith("delallunits")) {
      delallunits();
    } else if (cmd.startsWith("delp")) {
      delparam(inputArr);
    } else if (cmd.startsWith("delunit")) {
      delunit(inputArr);
    } else if (cmd.startsWith("listp")) {
      listparams(inputArr, oh);
    } else if (cmd.startsWith("listu")) {
      listunits(inputArr, oh);
    } else if (cmd.startsWith("moveu")) {
      moveunit(inputArr);
    } else if (cmd.startsWith("setp")) {
      setparam(inputArr);
    } else if (cmd.startsWith("setu")) {
      setunit(inputArr);
    } else {
      throw new IllegalArgumentException("The command " + inputArr[0] + " was not recognized.");
    }
    return false;
  }

  private Map<String, Unit> getUnitMap(String[] args) throws Exception {
    Map<String, Unit> units = null;
    if (args.length == 1) {
      units =
          session
              .getAcsUnit()
              .getUnits((String) null, context.getUnittype(), context.getProfile(), null);
    } else if (args.length == 2) {
      units =
          session
              .getAcsUnit()
              .getUnits("%" + args[1] + "%", context.getUnittype(), context.getProfile(), null);
    } else if (args.length > 2) {
      List<Parameter> params = ParameterParser.parse(context, args);
      units =
          session.getAcsUnit().getUnits(context.getUnittype(), context.getProfile(), params, null);
    }
    return units;
  }
}
