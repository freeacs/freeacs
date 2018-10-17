package com.github.freeacs.shell.menu;

import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.dbi.UnittypeParameterValues;
import com.github.freeacs.dbi.tr069.TR069DMLoader;
import com.github.freeacs.dbi.tr069.TR069DMParameter;
import com.github.freeacs.dbi.tr069.TR069DMParameter.StringType;
import com.github.freeacs.dbi.tr069.TR069DMParameterMap;
import com.github.freeacs.shell.Context;
import com.github.freeacs.shell.Session;
import com.github.freeacs.shell.output.Line;
import com.github.freeacs.shell.output.Listing;
import com.github.freeacs.shell.output.OutputHandler;
import com.github.freeacs.shell.util.Validation;
import java.util.ArrayList;
import java.util.List;

public class UnittypeParameterMenu {
  private Context context;
  private Session session;
  private static TR069DMParameterMap dmMap;

  static {
    try {
      dmMap = TR069DMLoader.load();
    } catch (Exception e) {
    }
  }

  public UnittypeParameterMenu(Session session) {
    this.session = session;
    this.context = session.getContext();
  }

  private void generateenum(String[] inputArr, OutputHandler oh) throws Exception {
    UnittypeParameter utp = context.getUnittypeParameter();
    TR069DMParameter dmp = dmMap.getParameter(utp.getName());
    if (dmp == null) {
      context.println(utp.getName() + " : No TR069 data model found for this parameter");
      return;
    }
    if (dmp.getEnumeration() != null
        && !dmp.getEnumeration().isEmpty()
        && dmp.getEnumeration().get(0).getValue() != null) {
      UnittypeParameterValues upv = utp.getValues();
      context.print(utp.getName() + " : Adding the following enumerations: ");
      if (upv == null) {
        upv = new UnittypeParameterValues();
        utp.setValues(upv);
      }
      upv.setValues(new ArrayList<String>()); // clear old enumerations
      for (StringType st : dmp.getEnumeration()) {
        upv.getValues().add(st.getValue());
        context.print(st.getValue() + " ");
      }
      context
          .getUnittype()
          .getUnittypeParameters()
          .addOrChangeUnittypeParameter(utp, session.getAcs());
      context.println("");
    } else {
      context.println(utp.getName() + " : No enumeration found for this parameter");
    }
  }

  private void listvalues(String[] inputArr, OutputHandler oh) throws Exception {
    UnittypeParameter up = context.getUnittypeParameter();
    UnittypeParameterValues upv = up.getValues();
    Listing listing = oh.getListing();
    listing.setHeading("Type", "Values");
    if (upv != null && upv.getType() != null) {
      Line line = new Line();

      line.addValue(upv.getType());
      if (upv.getType().equals(UnittypeParameterValues.ENUM)) {
        for (String value : upv.getValues()) {
          line.addValue(value);
        }
      } else if (upv.getType().equals(UnittypeParameterValues.REGEXP)) {
        line.addValue(upv.getPattern().toString());
      }
      listing.addLine(line);
    }
  }

  private void setvalues(String[] args) throws Exception {
    Validation.numberOfArgs(args, 3);
    UnittypeParameter up = context.getUnittypeParameter();
    UnittypeParameterValues upv = up.getValues();
    if (upv == null) {
      upv = new UnittypeParameterValues();
      up.setValues(upv);
    }
    if (!args[1].equals(UnittypeParameterValues.ENUM)
        && !args[1].equals(UnittypeParameterValues.REGEXP)) {
      throw new IllegalArgumentException(
          "The first argument must be either "
              + UnittypeParameterValues.ENUM
              + " or "
              + UnittypeParameterValues.REGEXP);
    }
    if (args[1].equals(UnittypeParameterValues.ENUM)) {
      List<String> values = new ArrayList<>();
      for (int i = 2; i < args.length; i++) {
        values.add(args[i]);
      }
      upv.setValues(values);
    } else if (args[1].equals(UnittypeParameterValues.REGEXP)) {
      upv.setPattern(args[2]);
    }
    context
        .getUnittype()
        .getUnittypeParameters()
        .addOrChangeUnittypeParameter(up, session.getAcs());
    context.println(
        "["
            + session.getCounter()
            + "] The unittype parameter values for "
            + up.getName()
            + " are updated");
    session.incCounter();
  }

  private void delvalues(String[] args) throws Exception {
    UnittypeParameter unittypeParameter = context.getUnittypeParameter();
    unittypeParameter.getValues().setValues(new ArrayList<String>());
    unittypeParameter.getValues().setPattern(null);
    context
        .getUnittype()
        .getUnittypeParameters()
        .addOrChangeUnittypeParameter(unittypeParameter, session.getAcs());
    unittypeParameter.setValues(null);
    context.println(
        "["
            + session.getCounter()
            + "] The unittype parameter values for "
            + unittypeParameter.getName()
            + " are deleted.");
    session.incCounter();
  }

  public boolean execute(String[] inputArr, OutputHandler oh) throws Exception {
    if (inputArr[0].startsWith("del")) {
      delvalues(inputArr);
    } else if (inputArr[0].startsWith("list")) {
      listvalues(inputArr, oh);
    } else if (inputArr[0].startsWith("set")) {
      setvalues(inputArr);
    } else if (inputArr[0].startsWith("gene")) {
      generateenum(inputArr, oh);
    } else {
      throw new IllegalArgumentException("The command was not recognized.");
    }
    return false;
  }
}
