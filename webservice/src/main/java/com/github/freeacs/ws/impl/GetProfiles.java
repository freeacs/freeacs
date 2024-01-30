package com.github.freeacs.ws.impl;

import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.ws.xml.ArrayOfProfile;
import com.github.freeacs.ws.xml.GetProfilesRequest;
import com.github.freeacs.ws.xml.GetProfilesResponse;
import com.github.freeacs.ws.xml.ObjectFactory;
import com.github.freeacs.ws.xml.ProfileList;
import java.rmi.RemoteException;
import java.util.Arrays;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetProfiles {
  private static final Logger logger = LoggerFactory.getLogger(GetProfiles.class);

  public GetProfilesResponse getProfiles(
      GetProfilesRequest gur, DataSource xapsDs, DataSource syslogDs) throws RemoteException {
    try {
      ACSFactory xapsWS = ACSWSFactory.getXAPSWS(gur.getLogin(), xapsDs, syslogDs);
      if (gur.getUnittype() == null || gur.getUnittype().getName() == null) {
        throw ACSFactory.error(logger, "No unittype is specified");
      }
      Unittype unittype = xapsWS.getUnittypeFromXAPS(gur.getUnittype().getName());
      com.github.freeacs.ws.xml.Profile[] profileArray = null;
      if (gur.getProfile() == null || gur.getProfile().getName() == null) {
        Profile[] profileXAPSArr = unittype.getProfiles().getProfiles();
        profileArray = new com.github.freeacs.ws.xml.Profile[profileXAPSArr.length];
        int i = 0;
        for (Profile profileXAPS : profileXAPSArr) {
          profileArray[i++] = ConvertACS2WS.convert(profileXAPS);
        }
      } else {
        profileArray = new com.github.freeacs.ws.xml.Profile[1];
        Profile p =
            xapsWS.getProfileFromXAPS(unittype.getName(), gur.getProfile().getValue().getName());
        profileArray[0] = ConvertACS2WS.convert(p);
      }
      GetProfilesResponse response = new GetProfilesResponse();
      ProfileList profileList = new ProfileList();
      ArrayOfProfile arrayOfProfile = new ArrayOfProfile();
      arrayOfProfile.getItem().addAll(Arrays.asList(profileArray));
      profileList.setProfileArray(arrayOfProfile);
      ObjectFactory factory = new ObjectFactory();
      response.setProfiles(factory.createGetProfilesResponseProfiles(profileList));
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
