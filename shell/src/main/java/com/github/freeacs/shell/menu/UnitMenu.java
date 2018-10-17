package com.github.freeacs.shell.menu;

import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.ProfileParameter;
import com.github.freeacs.dbi.SyslogConstants;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.UnitParameter;
import com.github.freeacs.dbi.Unittype.ProvisioningProtocol;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.dbi.util.ACSVersionCheck;
import com.github.freeacs.dbi.util.ProvisioningMode;
import com.github.freeacs.dbi.util.SystemConstants;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.shell.Context;
import com.github.freeacs.shell.Session;
import com.github.freeacs.shell.UnitTempStorage;
import com.github.freeacs.shell.output.Heading;
import com.github.freeacs.shell.output.Line;
import com.github.freeacs.shell.output.Listing;
import com.github.freeacs.shell.output.OutputHandler;
import com.github.freeacs.shell.util.Validation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

public class UnitMenu {
  private Context context;
  private Session session;

  public UnitMenu(Session session) {
    this.session = session;
    this.context = session.getContext();
  }

  private void setparam(String[] args) throws Exception {
    Validation.numberOfArgs(args, 3);
    UnittypeParameter utp = context.getUnittype().getUnittypeParameters().getByName(args[1]);
    if (utp == null) {
      throw new IllegalArgumentException(
          "["
              + session.getCounter()
              + "] The unit parameter name is not a unittype parameter name. Add the unittype parameter first.");
    } else {
      Unit unit = context.getUnit();
      Map<String, UnitParameter> unitParameters = unit.getUnitParameters();
      UnitParameter unitParameter = unitParameters.get(args[1]);
      String action = null;
      if (unitParameter == null) {
        unitParameter = new UnitParameter(utp, unit.getId(), args[2], context.getProfile());
        action = "added or changed";
      } else {
        unitParameter.getParameter().setValue(args[2]);
        action = "changed";
      }
      session.getBatchStorage().getAddChangeUnitParameters().add(unitParameter);
      String message =
          "[" + session.getCounter() + "] The unit parameter " + args[1] + " is " + action + ".";
      if (utp.getFlag().isReadOnly() && !utp.getFlag().isSystem()) {
        message +=
            " WARN: The parameter is Read-Only, the new value may be overwritten on next provisioning";
      }
      context.println(message);
      if (session.getBatchStorage().getAddChangeUnitParameters().size() == 1000) {
        UnitTempStorage uts = session.getBatchStorage().getAddUnits();
        for (Entry<Profile, List<String>> entry : uts.getUnits().entrySet()) {
          session.getAcsUnit().addUnits(entry.getValue(), entry.getKey());
        }
        uts.reset();
        session
            .getAcsUnit()
            .addOrChangeUnitParameters(
                session.getBatchStorage().getAddChangeUnitParameters(), context.getProfile());
        session.getBatchStorage().setAddChangeUnitParameters(null);
      }
      session.incCounter();
    }
  }

  private void setsessionparam(String[] args) throws Exception {
    Validation.numberOfArgs(args, 2);
    UnittypeParameter up = context.getUnittype().getUnittypeParameters().getByName(args[1]);
    if (up == null) {
      throw new IllegalArgumentException(
          "["
              + session.getCounter()
              + "] The unit parameter name is not a unittype parameter name. Add the unittype parameter first.");
    } else {
      Unit unit = context.getUnit();
      //			unit = context.getXapsU().getUnit(unit);
      Map<String, UnitParameter> sessionParameters = unit.getSessionParameters();
      UnitParameter unitParameter = sessionParameters.get(args[1]);
      String action = null;
      if (unitParameter == null) {
        unitParameter =
            new UnitParameter(up, unit.getId(), "REQUEST-SESSION-PARAMETER", context.getProfile());
        action = "added or changed";
      } else {
        unitParameter.getParameter().setValue("REQUEST-SESSION-PARAMETER");
        action = "changed";
      }
      List<UnitParameter> unitParameterList = new ArrayList<>();
      unitParameterList.add(unitParameter);
      session
          .getAcsUnit()
          .addOrChangeSessionUnitParameters(unitParameterList, context.getProfile());
      context.println(
          "["
              + session.getCounter()
              + "] The unit session parameter "
              + args[1]
              + " is "
              + action
              + ".");
      session.incCounter();
    }
  }

  private void delparam(String[] args) throws Exception {
    Validation.numberOfArgs(args, 2);
    Unit unit = context.getUnit();
    if (!unit.isParamsAvailable()) {
      unit = session.getAcsUnit().getUnitById(unit.getId());
      context.setUnit(unit);
    }
    Map<String, UnitParameter> unitParameters = unit.getUnitParameters();
    if (unitParameters == null || unitParameters.get(args[1]) == null) {
      context.println(
          "[" + session.getCounter() + "] The unit parameter " + args[1] + " does not exist.");
    } else {
      session.getBatchStorage().getDeleteUnitParameters().add(unitParameters.get(args[1]));
      context.println(
          "["
              + session.getCounter()
              + "] The unit parameter "
              + args[1]
              + " is scheduled for deletion.");
      if (session.getBatchStorage().getDeleteUnitParameters().size() == 1000) {
        session
            .getAcsUnit()
            .deleteUnitParameters(session.getBatchStorage().getDeleteUnitParameters());
        session.println("The unit parameters scheduled for deletion are deleted");
        session.getBatchStorage().setDeleteUnitParameters(null);
      }
    }
    session.incCounter();
  }

  public boolean execute(String[] args, OutputHandler oh) throws Exception {
    if (args[0].startsWith("delp")) {
      delparam(args);
    } else if (args[0].startsWith("kick")) {
      modeChange(args);
    } else if (args[0].startsWith("lista")) {
      listparamsImpl(args, oh, PrintLevel.ALL);
    } else if (args[0].startsWith("listu")) {
      listparamsImpl(args, oh, PrintLevel.UNIT);
    } else if (args[0].startsWith("regu") || args[0].startsWith("read")) {
      modeChange(args);
    } else if (args[0].startsWith("refr")) {
      refresh(args);
    } else if (args[0].startsWith("prov")) {
      modeChange(args);
    } else if (args[0].startsWith("setp")) {
      setparam(args);
    } else if (args[0].startsWith("setse")) {
      setsessionparam(args);
    } else {
      throw new IllegalArgumentException("The command " + args[0] + " was not recognized.");
    }
    return false;
  }

  private void refresh(String[] inputArr) throws Exception {
    context.setUnit(session.getAcsUnit().getUnitById(context.getUnit().getId()));
    context.println("The unit is refreshed");
  }

  private void modeChange(String[] inputArr) throws Exception {
    if (context.getUnittype().getProtocol() != ProvisioningProtocol.TR069) {
      throw new IllegalArgumentException(
          "The unittype does not support a TR-069 provisioning protocol, hence kick is not possible.");
    }

    long MAX_WAIT_MS = 30000; // default timeout

    Unit unit = context.getUnit();
    if (inputArr[0].startsWith("read")) {
      MAX_WAIT_MS = 60000;
      unit.toWriteQueue(SystemParameters.PROVISIONING_MODE, ProvisioningMode.READALL.toString());
    } else if (inputArr[0].startsWith("regu")) {
      context.println("The device will return to " + ProvisioningMode.REGULAR + " mode");
      unit.toWriteQueue(SystemParameters.PROVISIONING_MODE, ProvisioningMode.REGULAR.toString());
      //			unit.toWriteQueue(SystemParameters.PROVISIONING_STATE,
      // ProvisioningState.READY.toString());
      session.getAcsUnit().addOrChangeQueuedUnitParameters(unit);
      context.setUnit(session.getAcsUnit().getUnitById(context.getUnit().getId()));
      return;
    }
    session.getAcsUnit().addOrChangeQueuedUnitParameters(unit);
    DBI dbi = session.getDbi();
    if (inputArr.length > 1 && "async".equals(inputArr[1])) {
      dbi.publishKick(context.getUnit(), SyslogConstants.FACILITY_STUN);
      return;
    }
    context.println(
        "Fusion will try to connect to the device, waiting max "
            + MAX_WAIT_MS / 1000
            + "s to see outcome.");
    unit = session.getAcsUnit().getUnitById(context.getUnit().getId());
    String lct = unit.getParameterValue(SystemParameters.LAST_CONNECT_TMS);
    if (lct == null) {
      lct = "";
    }
    dbi.publishKick(context.getUnit(), SyslogConstants.FACILITY_STUN);

    /*
     * At this point all command/changes on the unit has been issued. We must now wait to see if the device
     * makes contact with the server.
     */

    long start = System.currentTimeMillis();
    //		boolean modePeriodic = false;
    String lastInspectionMessage = "";
    do {
      Thread.sleep(500);
      Unit u = session.getAcsUnit().getUnitById(context.getUnit().getId());
      context.setUnit(u);
      //			if (!modePeriodic && u.getProvisioningMode() == ProvisioningMode.PERIODIC) {
      //				context.println("STUN server has changed the provisioning mode back to " +
      // u.getProvisioningMode());
      //				modePeriodic = true;
      //			}
      String inspectionMessage = u.getParameterValue(SystemParameters.INSPECTION_MESSAGE, false);
      if (inspectionMessage != null
          && !inspectionMessage.equals(lastInspectionMessage)
          && !inspectionMessage.equals(SystemConstants.DEFAULT_INSPECTION_MESSAGE)) {
        context.println(inspectionMessage);
        lastInspectionMessage = inspectionMessage;
      }
      String newLct = u.getParameterValue(SystemParameters.LAST_CONNECT_TMS);
      if (newLct != null && !lct.equals(newLct)) {
        context.println("SUCCESS: Device has connected to Fusion at " + newLct);
        break;
      }
      //			if (u.getProvisioningState() == ProvisioningState.READY) {
      //				context.println("SUCCESS: Device has connected to the TR-069 server and updated
      // Fusion");
      //				break;
      //			}
      if (System.currentTimeMillis() - start > MAX_WAIT_MS) {
        context.println(
            "ERROR: Device did not connect in "
                + MAX_WAIT_MS / 1000
                + "s, must be rebooted manually complete request");
        break;
      }
    } while (true);
    context.getUnit().toDeleteQueue(SystemParameters.INSPECTION_MESSAGE);
    session.getAcsUnit().deleteUnitParameters(context.getUnit());
    context.setUnit(session.getAcsUnit().getUnitById(context.getUnit().getId()));
  }

  private enum PrintLevel {
    UNIT,
    ALL
  }

  private void listparamsImpl(String[] inputArr, OutputHandler oh, PrintLevel pl) throws Exception {
    Unit unit = context.getUnit();
    unit = session.getAcsUnit().getUnitById(unit.getId());
    context.setUnit(unit);

    boolean sessionMode =
        context.getUnit().isSessionMode() && ACSVersionCheck.unitParamSessionSupported;

    // Prepare paramSet - which parameters can be listed  && Make heading
    Line headingLine = new Line();
    headingLine.addValue("Unit Type Parameter Name");
    headingLine.addValue("Unit Parameter Value/Provisioned Value");
    TreeSet<String> paramSet = new TreeSet<>(unit.getUnitParameters().keySet());
    if (pl == PrintLevel.ALL) {
      headingLine.addValue("Profile Parameter Value");
      for (ProfileParameter pp : unit.getProfile().getProfileParameters().getProfileParameters()) {
        paramSet.add(pp.getUnittypeParameter().getName());
      }
      if (sessionMode) {
        headingLine.addValue("CPE Parameter Value");
        paramSet.addAll(unit.getSessionParameters().keySet());
      }
    }
    Listing listing = oh.getListing();
    listing.setHeading(new Heading(headingLine));

    // Loop through params
    for (String paramName : paramSet) {
      // Find unitValue, profileValue and cpeValue
      String profileValue = null;
      String cpeValue = null;
      UnitParameter up = unit.getUnitParameters().get(paramName);
      String unitValue = up != null ? up.getValue() : null;
      if (pl == PrintLevel.ALL) {
        ProfileParameter pp = unit.getProfile().getProfileParameters().getByName(paramName);
        profileValue = pp != null ? pp.getValue() : null;
        if (sessionMode) {
          UnitParameter sp = unit.getSessionParameters().get(paramName);
          cpeValue = sp != null ? sp.getValue() : null;
        }
      }

      // Filter based on param names and param values
      if (!Validation.matches(
          inputArr.length > 1 ? inputArr[1] : null, paramName, unitValue, profileValue, cpeValue)) {
        continue;
      }

      // Print to listing
      Line line = new Line(paramName);
      line.addValue(unitValue);
      if (pl == PrintLevel.ALL) {
        line.addValue(profileValue);
        if (sessionMode) {
          line.addValue(cpeValue);
        }
      }
      listing.addLine(line);
    }
  }
}
