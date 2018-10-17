package com.github.freeacs.shell.menu;

import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.GroupParameter;
import com.github.freeacs.dbi.GroupParameters;
import com.github.freeacs.dbi.Parameter;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.shell.Context;
import com.github.freeacs.shell.Session;
import com.github.freeacs.shell.command.Option;
import com.github.freeacs.shell.output.Heading;
import com.github.freeacs.shell.output.Line;
import com.github.freeacs.shell.output.Listing;
import com.github.freeacs.shell.output.OutputHandler;
import com.github.freeacs.shell.util.Validation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroupMenu {
  private Session session;
  private Context context;

  public GroupMenu(Session session) {
    this.session = session;
    this.context = session.getContext();
  }

  /**
   * Returns true : has processed a cd-command Returns false : has processed another command
   * (everything else)
   */
  public boolean execute(String[] inputArr, OutputHandler oh) throws Exception {
    if (inputArr[0].startsWith("dela")) {
      delallparams(inputArr);
    } else if (inputArr[0].startsWith("delp")) {
      delparam(inputArr);
    } else if (inputArr[0].startsWith("listd")) {
      listdetails(inputArr, oh);
    } else if ("listparams".equals(inputArr[0])) {
      listparams(inputArr, oh, false);
    } else if (inputArr[0].startsWith("listparamsforexport")) {
      listparamsforexport(inputArr, oh);
    } else if (inputArr[0].startsWith("listu")) {
      listunits(inputArr, oh);
    } else if (inputArr[0].startsWith("setp")) {
      setparam(inputArr);
    } else if (inputArr[0].startsWith("coun")) {
      count(inputArr, oh);
    } else {
      throw new IllegalArgumentException("The command " + inputArr[0] + " was not recognized.");
    }
    return false;
  }

  private void setparam(String[] inputArr) throws Exception {
    Validation.numberOfArgs(inputArr, 4);
    Group group = context.getGroup();
    List<Parameter> params = ParameterParser.parse(context, inputArr);
    if (params != null) {
      GroupParameters groupParams = group.getGroupParameters();
      for (Parameter param : params) {
        String action = "added";
        GroupParameter gp = null;
        if (param.getGroupParameterId() == null
            || groupParams.getById(param.getGroupParameterId()) == null) {
          gp = new GroupParameter(param, group);
        } else {
          gp =
              groupParams.getByName(
                  param.getUnittypeParameter().getName() + "#" + param.getGroupParameterId());
          gp.setParameter(param);
          action = "changed";
        }
        group.getGroupParameters().addOrChangeGroupParameter(gp, session.getAcs());
        session.println("[" + session.getCounter() + "] The group parameter is " + action);
        session.incCounter();
      }
    }
  }

  private void delparam(String[] args) throws Exception {
    Validation.numberOfArgs(args, 2);
    Group group = context.getGroup();
    String unittypeParameterName = args[1];
    GroupParameters groupParams = group.getGroupParameters();
    GroupParameter gp = groupParams.getByName(unittypeParameterName);
    if (gp != null) {
      groupParams.deleteGroupParameter(gp, session.getAcs());
      session.println("[" + session.getCounter() + "] The group parameter is deleted");
    } else {
      session.println("[" + session.getCounter() + "] The group parameter does not exist.");
    }
    session.incCounter();
  }

  private void listparamsforexport(String[] args, OutputHandler oh) throws Exception {
    listparams(args, oh, true);
  }

  private void listparams(String[] args, OutputHandler oh, boolean export) throws Exception {
    GroupParameter[] gpArr = context.getGroup().getGroupParameters().getGroupParameters();
    Listing listing = oh.getListing();
    listing.setHeading("Unit Type Parameter Name", "Operator", "Value", "Data Type");
    for (GroupParameter gp : gpArr) {
      Parameter p = gp.getParameter();
      if (!Validation.matches(
          args.length > 1 ? args[1] : null,
          p.getUnittypeParameter().getName(),
          p.getValue(),
          p.getOp().getOperatorLiteral(),
          p.getType().getType())) {
        continue;
      }
      Line line = new Line();
      if (export) {
        line.addValue(gp.getParameter().getUnittypeParameter().getName());
      } else {
        line.addValue(gp.getName());
      }
      line.addValue(gp.getParameter().getOp().getOperatorLiteral());
      if (gp.getParameter().getValue() == null || gp.getParameter().valueWasNull()) {
        line.addValue("NULL");
      } else {
        line.addValue(gp.getParameter().getValue());
      }
      line.addValue(gp.getParameter().getType().getType());
      listing.addLine(line);
    }
  }

  private void delallparams(String[] args) throws Exception {
    Group group = context.getGroup();

    GroupParameter[] gpArr = group.getGroupParameters().getGroupParameters();
    for (GroupParameter gp : gpArr) {
      group.getGroupParameters().deleteGroupParameter(gp, session.getAcs());
    }
    session.println("[" + session.getCounter() + "] The group parameters are deleted");
    session.incCounter();
  }

  private void listdetails(String[] args, OutputHandler oh) throws Exception {
    List<Group> groups = new ArrayList<>();
    Group tmp = context.getGroup();
    while (tmp != null) {
      groups.add(tmp);
      tmp = tmp.getParent();
    }
    Profile profile = context.getGroup().getTopParent().getProfile();
    int units = session.getAcsUnit().getUnitCount(context.getGroup());
    context.getGroup().setCount(units);
    context.getUnittype().getGroups().addOrChangeGroup(context.getGroup(), session.getAcs());

    oh.setHeading("Details:\n");
    for (int i = 0; i < groups.size(); i++) {
      Group g = groups.get(i);
      String parentStr = "Parent-" + i;
      if (i == 0) {
        parentStr = "        ";
      }

      oh.print(parentStr + "               Id : " + g.getId() + "\n");
      oh.print(parentStr + "             Name : " + g.getName() + "\n");
      for (int j = 0; j < groups.get(i).getGroupParameters().getGroupParameters().length; j++) {
        GroupParameter gp = g.getGroupParameters().getGroupParameters()[j];
        oh.print(parentStr + "      Parameter-" + j + " : ");
        oh.print(
            String.format(
                "%-30s " + gp.getParameter().getOp().getOperatorLiteral() + " ",
                gp.getParameter().getUnittypeParameter().getName()));
        if (gp.getParameter().getValue() == null || gp.getParameter().valueWasNull()) {
          oh.print("NULL\n");
        } else {
          oh.print(gp.getParameter().getValue() + "\n");
        }
      }
    }
    String profileStr = "NULL";
    if (profile != null) {
      profileStr = profile.getName();
    }

    oh.print("                  Profile : " + profileStr + "\n");
    oh.print("          Number of units : " + units + "\n");
  }

  private void count(String[] args, OutputHandler oh) throws Exception {
    Listing listing = oh.getListing();
    listing.setHeading(new Heading(new Line("Unit-count")));
    int count = session.getAcsUnit().getUnitCount(context.getGroup());
    listing.addLine(new Line(String.valueOf(count)));
    context.getGroup().setCount(count);
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
    listing.setHeading(new Heading(headingLine), true);
    Map<String, Unit> units = session.getAcsUnit().getUnits(context.getGroup());
    for (Map.Entry<String, Unit> entry : units.entrySet()) {
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
    context.getGroup().setCount(units.size());
  }
}
