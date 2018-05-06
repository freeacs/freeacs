package com.owera.xapsws.impl;

import java.rmi.RemoteException;

import com.owera.common.log.Logger;
import com.owera.common.util.Cache;
import com.owera.common.util.CacheValue;
import com.owera.xapsws.Login;

public class XAPSWSFactory {

	private static Cache cache = new Cache();
	private static Logger logger = new Logger();

	private static RemoteException error(String msg) {
		logger.error(msg);
		return new RemoteException(msg);
	}

	public static XAPSWS getXAPSWS(Login login) throws RemoteException {
		if (login == null || login.getUsername() == null || login.getPassword() == null)
			throw error("No username and/or password are supplied in the Login object");
		String key = login.getUsername() + login.getPassword();
		if (cache.get(key) == null) {
			int lifetimeSec = 5 * 60;
			XAPSWS xapsWS = new XAPSWS(login, lifetimeSec);
			cache.put(key, new CacheValue(xapsWS, Cache.ABSOLUTE, lifetimeSec * 1000));
		}
		return (XAPSWS) cache.get(key).getObject();
	}

}
