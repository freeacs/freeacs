package com.github.freeacs.ws.impl;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.Unittype.ProvisioningProtocol;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.dbi.UnittypeParameterFlag;
import com.github.freeacs.dbi.Unittypes;
import com.github.freeacs.dbi.User;
import com.github.freeacs.ws.xml.AddOrChangeUnittypeRequest;
import com.github.freeacs.ws.xml.AddOrChangeUnittypeResponse;
import com.github.freeacs.ws.xml.ParameterList;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddOrChangeUnittype {
  private static final Logger logger = LoggerFactory.getLogger(AddOrChangeUnittype.class);

  private ACS acs;
  private ACSFactory acsWS;

  private void addOrChangeUnittypeImpl(Unittype unittypeXAPS, AddOrChangeUnittypeRequest gur)
      throws SQLException, RemoteException {
    unittypeXAPS.setDescription(gur.getUnittype().getDescription().getValue());
    //		unittypeXAPS.setMatcherId(gur.getUnittype().getMatcherId());
    unittypeXAPS.setProtocol(
        ProvisioningProtocol.toEnum(gur.getUnittype().getProtocol().getValue()));
    unittypeXAPS.setVendor(gur.getUnittype().getVendor().getValue());
    ParameterList parameterList = gur.getUnittype().getParameters().getValue();
    List<UnittypeParameter> acUtpList = new ArrayList<>();
    List<UnittypeParameter> dUtpList = new ArrayList<>();
    if (parameterList != null && parameterList.getParameterArray() != null) {
      for (com.github.freeacs.ws.xml.Parameter param :
          parameterList.getParameterArray().getItem()) {
        UnittypeParameter utp = unittypeXAPS.getUnittypeParameters().getByName(param.getName());
        if (param.getFlags() != null
            && !"D".equals(param.getFlags().getValue())
            && !"AC".equals(param.getFlags().getValue())) {
          throw ACSFactory.error(
              logger,
              "Flag for parameter "
                  + param.getName()
                  + " had value "
                  + param.getFlags()
                  + ", but must be either D or AC");
        }
        if (param.getFlags() == null || "AC".equals(param.getFlags().getValue())) {
          if (utp == null) {
            acUtpList.add(
                new UnittypeParameter(
                    unittypeXAPS,
                    param.getName(),
                    new UnittypeParameterFlag(param.getValue().getValue())));
          } else {
            utp.setFlag(new UnittypeParameterFlag(param.getValue().getValue()));
            acUtpList.add(utp);
          }
        } else if ("D".equals(param.getFlags().getValue()) && utp != null) {
          dUtpList.add(utp);
        }
      }
    }
    //		System.out.println("AOC: Unitypes object: " + xapsWS.getXAPS().getUnittypes());
    acsWS.getAcs().getUnittypes().addOrChangeUnittype(unittypeXAPS, acs);
    for (UnittypeParameter utp : dUtpList) {
      //			System.out.println("D: Unittype: " + unittypeXAPS + ", UTP:" + utp.getName() + ", " +
      // utp.getFlag().getFlag());
      unittypeXAPS.getUnittypeParameters().deleteUnittypeParameter(utp, acs);
    }
    for (UnittypeParameter utp : acUtpList) {
      //			System.out.println("AOC: Unittype: " + unittypeXAPS + ", UTP:" + utp.getName() + ", " +
      // utp.getFlag().getFlag());
      unittypeXAPS.getUnittypeParameters().addOrChangeUnittypeParameter(utp, acs);
    }
  }

  public AddOrChangeUnittypeResponse addOrChangeUnittype(
      AddOrChangeUnittypeRequest gur, DataSource xapsDs, DataSource syslogDs)
      throws RemoteException {
    try {
      acsWS = ACSWSFactory.getXAPSWS(gur.getLogin(), xapsDs, syslogDs);
      acs = acsWS.getAcs();
      User user = acsWS.getId().getUser();
      if (gur.getUnittype() == null || gur.getUnittype().getName() == null) {
        throw ACSFactory.error(logger, "No unittype name specified");
      }
      boolean isAdmin = user.getPermissions().getPermissions().length == 0;
      Unittypes unittypes = acs.getUnittypes();
      Unittype unittypeXAPS = null;
      if (unittypes.getByName(gur.getUnittype().getName()) == null) { // make new unittype
        if (isAdmin) { // allow if login is admin
          com.github.freeacs.ws.xml.Unittype uWS = gur.getUnittype();
          unittypeXAPS =
              new Unittype(
                  uWS.getName(),
                  uWS.getVendor().getValue(),
                  uWS.getDescription().getValue(),
                  ProvisioningProtocol.toEnum(uWS.getProtocol().getValue()));
          addOrChangeUnittypeImpl(unittypeXAPS, gur);
        } else {
          throw ACSFactory.error(
              logger,
              "The unittype "
                  + gur.getUnittype().getName()
                  + " does not exist, your login does not have the permissions to create it.");
        }
      } else { // change an existing one
        unittypeXAPS = acsWS.getUnittypeFromXAPS(gur.getUnittype().getName());
        addOrChangeUnittypeImpl(unittypeXAPS, gur);
      }
      AddOrChangeUnittypeResponse response = new AddOrChangeUnittypeResponse();
      response.setUnittype(ConvertACS2WS.convert(unittypeXAPS));
      return response;
    } catch (Throwable t) {
      if (t instanceof RemoteException) {
        throw (RemoteException) t;
      } else {
        throw ACSFactory.error(logger, t);
      }
    }
  }
}
