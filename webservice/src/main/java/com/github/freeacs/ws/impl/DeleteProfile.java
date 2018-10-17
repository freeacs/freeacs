package com.github.freeacs.ws.impl;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.ws.xml.DeleteProfileRequest;
import com.github.freeacs.ws.xml.DeleteProfileResponse;
import java.rmi.RemoteException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteProfile {
  private static final Logger logger = LoggerFactory.getLogger(DeleteProfile.class);

  public DeleteProfileResponse deleteProfile(
      DeleteProfileRequest dur, DataSource xapsDs, DataSource syslogDs) throws RemoteException {
    try {
      ACSFactory acsWS = ACSWSFactory.getXAPSWS(dur.getLogin(), xapsDs, syslogDs);
      ACS acs = acsWS.getAcs();
      if (dur.getUnittype() == null) {
        throw ACSFactory.error(logger, "No unittype is specified");
      }
      Unittype unittype = acsWS.getUnittypeFromXAPS(dur.getUnittype().getName());
      if (dur.getProfile() == null) {
        throw ACSFactory.error(logger, "No profile name is specified");
      }
      Profile profile = acsWS.getProfileFromXAPS(unittype.getName(), dur.getProfile().getName());
      int rowsDeleted = unittype.getProfiles().deleteProfile(profile, acs, true);
      return getDeleteProfileResponse(rowsDeleted > 0);
    } catch (Throwable t) {
      if (t instanceof RemoteException) {
        throw (RemoteException) t;
      } else {
        String msg = "An exception occurred: " + t.getMessage();
        logger.error(msg, t);
        throw new RemoteException(msg, t);
      }
    }
  }

  private DeleteProfileResponse getDeleteProfileResponse(boolean b) {
    DeleteProfileResponse response = new DeleteProfileResponse();
    response.setDeleted(b);
    return response;
  }
}
