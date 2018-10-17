package com.github.freeacs.shell.sync;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.Unittype.ProvisioningProtocol;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.dbi.UnittypeParameterFlag;
import com.github.freeacs.dbi.UnittypeParameterValues;
import com.github.freeacs.dbi.UnittypeParameters;
import com.github.freeacs.shell.Session;
import com.github.freeacs.shell.output.OutputHandler;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Sync {
  public static void main(String[] args, Session session, OutputHandler oh) throws Exception {
    /* Parse/validate input arguments */
    String unittypeName = args[1];
    ProvisioningProtocol protocol = ProvisioningProtocol.toEnum(args[2]);
    String filename = args[3];
    ACS acs = session.getAcs();
    Unittype unittype = acs.getUnittype(unittypeName);

    if (unittype != null && unittype.getProtocol() != protocol) {
      throw new IllegalArgumentException(
          "The unittype has a different protocol than the one supplied: "
              + args[3]
              + ", you may omit the last argument");
    }

    /* Load XML-document */
    UnittypeXML unitType = new UnittypeXML();
    unitType.load(filename);

    /* Create/Update Unittype */
    String action = "created";
    if (unittype == null) {
      unittype =
          new Unittype(unittypeName, unitType.info.vendor, unitType.info.description, protocol);
    } else {
      unittype.setVendor(unitType.info.vendor);
      unittype.setDescription(unitType.info.description);
      action = "changed";
      protocol = unittype.getProtocol();
    }
    session.getAcs().getUnittypes().addOrChangeUnittype(unittype, session.getAcs());
    oh.print("Unittype " + unittypeName + " basic information " + action + "\n");

    /* Create/Update Unittype Parameters */
    UnittypeXML.Parameters parameters = unitType.parameters;
    UnittypeParameters utps = unittype.getUnittypeParameters();
    Set<String> parameterNamesInUnittypeXML = new HashSet<>();
    List<UnittypeParameter> utpList = new ArrayList<>();
    for (int i = 0; i < parameters.list.size(); i++) {
      UnittypeXML.Parameter parameter = parameters.list.get(i);
      parameterNamesInUnittypeXML.add(parameter.name);
      if (!parameter.protocol_match(protocol.toString())) {
        continue;
      }
      UnittypeParameter utp = utps.getByName(parameter.name);
      if (utp == null) {
        utp =
            new UnittypeParameter(
                unittype,
                parameter.name,
                new UnittypeParameterFlag(parameter.deviceflags + parameter.addflags));
        action = "created";
      } else {
        utp.getFlag().setFlag(parameter.deviceflags + parameter.addflags);
        action = "changed";
      }
      //			utps.addOrChangeUnittypeParameter(utp, xaps);
      oh.print("Unittype parameter " + utp.getName() + " " + action + "\n");

      /* Create/Change Unittype Parameter Values */
      if (parameter.enum_ != null && !utp.getFlag().isReadOnly()) {
        UnittypeParameterValues utpvs = new UnittypeParameterValues();
        utpvs.setValues(parameter.enum_.getValues(parameter.default_value));
        utp.setValues(utpvs);
        //				utps.addOrChangeUnittypeParameter(utp, xaps);
        oh.print("Unittype parameter values are created/changed\n");
      } else if ("boolean".equals(parameter.type) && "RW".equals(parameter.deviceflags)) {
        UnittypeParameterValues utpvs = new UnittypeParameterValues();
        utpvs.setValues(Arrays.asList(new String[] {"0", "1"}));
        utp.setValues(utpvs);
      }
      utpList.add(utp);
    }
    oh.print("Commiting the changes to the database - notifying other modules about the change\n");
    utps.addOrChangeUnittypeParameters(utpList, acs);

    /* Delete Unittype Parameters not defined in XML */
    FileWriter fw = new FileWriter("setunittype-delete-report.xss", true);
    UnittypeParameter[] utpArr = utps.getUnittypeParameters();
    boolean headingPrinted = false;
    for (UnittypeParameter utp : utpArr) {
      if (!utp.getName().startsWith("System")
          && !parameterNamesInUnittypeXML.contains(utp.getName())) {
        if (!headingPrinted) {
          oh.print("\nNB! NB! These parameters printed below are undefined in the XML\n");
          oh.print("definition, you may run the commands below to delete them from the\n");
          oh.print("unittype. Note that if a unittype parameter is actually used, you\n");
          oh.print("will not be able to delete it with this command, you first have to\n");
          oh.print("delete the actual unit/profile/group/job parameter.");
          oh.print("The commands are also appended to file: setunittype-delete-report.xss\n\n");
          headingPrinted = true;
        }
        oh.print("/ut:" + unittypeName + " delparam " + utp.getName() + "\n");
        fw.write("/ut:" + unittypeName + " delparam " + utp.getName() + "\n");
      }
    }
    fw.close();
  }
}
