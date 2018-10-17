package com.github.freeacs.shell.transform;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.UnitParameter;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.shell.Session;
import com.github.freeacs.shell.util.StringUtil;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Transform {
  public static void transform(Session session, String unitFile, String paramFile)
      throws Exception {
    ACS acs = session.getAcs();
    ACSUnit acsUnit = session.getAcsUnit();
    BufferedReader unitFileBr = new BufferedReader(new FileReader(unitFile));
    String unitFileLine = null;
    Map<String, String> convertParamMap = new HashMap<>();
    BufferedReader paramFileBr = new BufferedReader(new FileReader(paramFile));
    String paramFileLine = null;
    while ((paramFileLine = paramFileBr.readLine()) != null) {
      String[] params = StringUtil.split(paramFileLine);
      if (convertParamMap.get(params[0]) != null) {
        convertParamMap.put(params[0], convertParamMap.get(params[0]) + "," + params[1]);
      } else {
        convertParamMap.put(params[0], params[1]);
      }
    }
    while ((unitFileLine = unitFileBr.readLine()) != null) {
      String[] units = StringUtil.split(unitFileLine);
      Unit unit = acsUnit.getUnitById(units[0]);
      List<String> addUnits = new ArrayList<>();
      addUnits.add(units[1]);
      Unit newUnit = acsUnit.getUnitById(units[1]);
      Profile profile = acs.getProfile("NPA201E-2", "internal-sip-server");
      acsUnit.addUnits(addUnits, profile);
      Map<String, UnitParameter> unitParameters = unit.getUnitParameters();
      Unittype newUnittype = acs.getUnittype("NPA201E-2");
      List<UnitParameter> newUnitParams = new ArrayList<>();
      for (Entry<String, UnitParameter> entry : unitParameters.entrySet()) {
        if (entry.getKey().endsWith("ConnectTms")
            || entry.getKey().endsWith("DesiredSoftwareVersion")) {
          continue;
        }
        if (convertParamMap.get(entry.getKey()) != null) {
          String[] newParams = convertParamMap.get(entry.getKey()).split(",");
          for (String newParam : newParams) {
            UnittypeParameter newUtp =
                newUnittype.getUnittypeParameters().getByName(newParam.trim());
            if (newUtp == null) {
              System.out.println(
                  "Couldn't find " + convertParamMap.get(entry.getKey()) + " in new Unittype");
            }
            newUnitParams.add(
                new UnitParameter(newUtp, newUnit.getId(), entry.getValue().getValue(), profile));
          }
        }
        //				else {
        //					String newParamName = entry.getKey().replaceFirst("Device.",
        // "InternetGatewayDevice.");
        //					UnittypeParameter newUtp =
        // newUnittype.getUnittypeParameters().getByName(newParamName);
        //					if (newUtp == null)
        //						System.out.println("Couldn't find " + entry.getKey() + " in new Unittype - the
        // parameter is removed entirely");
        //					else
        //						newUnitParams.add(new UnitParameter(newUtp, newUnit.getId(),
        // entry.getValue().getValue(), profile));
        //				}
      }
      acsUnit.addOrChangeUnitParameters(newUnitParams, profile);
    }
  }
}
