package com.github.freeacs.ws.impl;

import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.ws.xml.ArrayOfUnittype;
import com.github.freeacs.ws.xml.GetUnittypesRequest;
import com.github.freeacs.ws.xml.GetUnittypesResponse;
import com.github.freeacs.ws.xml.ObjectFactory;
import com.github.freeacs.ws.xml.UnittypeList;
import java.rmi.RemoteException;
import java.util.Arrays;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetUnittypes {
  private static final Logger logger = LoggerFactory.getLogger(GetUnittypes.class);

  public GetUnittypesResponse getUnittypes(
      GetUnittypesRequest gur, DataSource xapsDs, DataSource syslogDs) throws RemoteException {
    try {
      ACSFactory xapsWS = ACSWSFactory.getXAPSWS(gur.getLogin(), xapsDs, syslogDs);
      if (gur.getUnittypeName() != null) {
        Unittype unittypeXAPS = xapsWS.getUnittypeFromXAPS(gur.getUnittypeName().getValue());
        com.github.freeacs.ws.xml.Unittype[] unittypeArray =
            new com.github.freeacs.ws.xml.Unittype[1];
        unittypeArray[0] = ConvertACS2WS.convert(unittypeXAPS);
        return getGetUnittypesResponse(unittypeArray);
      } else { // return all Unittypes allowed
        Unittype[] unittypeXAPSArr = xapsWS.getAcs().getUnittypes().getUnittypes();
        com.github.freeacs.ws.xml.Unittype[] unittypeArray =
            new com.github.freeacs.ws.xml.Unittype[unittypeXAPSArr.length];
        int i = 0;
        for (Unittype unittypeXAPS : unittypeXAPSArr) {
          unittypeArray[i++] = ConvertACS2WS.convert(unittypeXAPS);
        }
        return getGetUnittypesResponse(unittypeArray);
      }
    } catch (RemoteException re) {
      throw re;
    } catch (Throwable t) {
      throw ACSFactory.error(logger, t);
    }
  }

  private GetUnittypesResponse getGetUnittypesResponse(
      com.github.freeacs.ws.xml.Unittype[] unittypeArray) {
    UnittypeList unittypeList = new UnittypeList();
    ArrayOfUnittype arrayOfUnittype = new ArrayOfUnittype();
    arrayOfUnittype.getItem().addAll(Arrays.asList(unittypeArray));
    unittypeList.setUnittypeArray(arrayOfUnittype);
    GetUnittypesResponse response = new GetUnittypesResponse();
    ObjectFactory factory = new ObjectFactory();
    response.setUnittypes(factory.createGetUnittypesResponseUnittypes(unittypeList));
    return response;
  }
}
