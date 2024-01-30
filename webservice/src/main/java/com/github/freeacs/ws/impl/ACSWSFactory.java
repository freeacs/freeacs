package com.github.freeacs.ws.impl;

import com.github.freeacs.common.util.Cache;
import com.github.freeacs.common.util.CacheValue;
import com.github.freeacs.ws.xml.Login;
import java.rmi.RemoteException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ACSWSFactory {
  private static Cache cache = new Cache();
  private static final Logger logger = LoggerFactory.getLogger(ACSWSFactory.class);

  private static RemoteException error(String msg) {
    logger.error(msg);
    return new RemoteException(msg);
  }

  public static ACSFactory getXAPSWS(Login login, DataSource xaps, DataSource syslog)
      throws RemoteException {
    if (login == null || login.getUsername() == null || login.getPassword() == null) {
      throw error("No username and/or password are supplied in the Login object");
    }
    String key = login.getUsername() + login.getPassword();
    if (cache.get(key) == null) {
      int lifetimeSec = 5 * 60;
      ACSFactory xapsWS = new ACSFactory(login, lifetimeSec, xaps, syslog);
      cache.put(key, new CacheValue(xapsWS, Cache.ABSOLUTE, lifetimeSec * 1000));
    }
    return (ACSFactory) cache.get(key).getObject();
  }
}
