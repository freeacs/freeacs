package com.owera.xapsws.impl;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Random;

import com.owera.common.db.ConnectionProperties;
import com.owera.common.db.ConnectionProvider;
import com.owera.common.db.NoAvailableConnectionException;
import com.owera.common.log.Logger;
import com.owera.xaps.dbi.DBI;
import com.owera.xaps.dbi.Identity;
import com.owera.xaps.dbi.Profile;
import com.owera.xaps.dbi.Syslog;
import com.owera.xaps.dbi.SyslogConstants;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.User;
import com.owera.xaps.dbi.Users;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.dbi.XAPSUnit;

import com.owera.xapsws.Login;

import static com.owera.xapsws.impl.Properties.getMaxAge;
import static com.owera.xapsws.impl.Properties.getMaxConn;
import static com.owera.xapsws.impl.Properties.getUrl;

public class XAPSWS {

	private static Logger logger = new Logger();

	private ConnectionProperties cp;
	private Identity id;
	private DBI dbi;
	private XAPS xaps;
	//	private Unittypes allowedUnittypes;
	private Syslog syslog;
	public Random random = new Random();
	//	public Pattern macPattern = Pattern.compile("[0-9a-fA-F]{12}");
	private boolean initialized = false;

	public static String VERSION = "1.4.8";

	public XAPSWS(Login login, int lifetimeSec) throws RemoteException {
		init(login, lifetimeSec);
	}

	public XAPS getXAPS() throws RemoteException {
		return xaps;
	}

	public XAPSUnit getXAPSUnit(XAPS xaps) throws RemoteException {
		try {
			return new XAPSUnit(cp, xaps, xaps.getSyslog());
		} catch (Throwable t) {
			String msg = "An exception occured while retrieving XAPSUnit object";
			logger.error(msg, t);
			throw new RemoteException(msg, t);
		}
	}

	private synchronized void init(Login login, int lifetimeSec) throws RemoteException {
		if (!initialized) {
			try {
				cp = ConnectionProvider.getConnectionProperties(getUrl("xaps"), getMaxAge("xaps"), getMaxConn("xaps"));
				Users users = new Users(cp);
				User user = users.getUnprotected(login.getUsername());
				if (user == null)
					throw error("The user " + login.getUsername() + " is unknown");

				id = new Identity(SyslogConstants.FACILITY_WEBSERVICE, VERSION, user);
				syslog = new Syslog(ConnectionProvider.getConnectionProperties(getUrl("syslog"), getMaxAge("syslog"), getMaxConn("syslog")), id);
				dbi = new DBI(lifetimeSec + 30, cp, syslog);
				xaps = dbi.getXaps();
				if (!login.getPassword().equals(user.getSecret()) && !user.isCorrectSecret(login.getPassword()))
					throw error("The password is incorrect");
				// At this stage we have a positive authentication of the user
				initialized = true;
				OKServlet.setError(null);
			} catch (Throwable t) {
				OKServlet.setError(t);
				String msg = "An exception occured while initializing XAPSWS object: " + t;
				logger.error(msg, t);
				throw new RemoteException(msg, t);
			}
		}
	}

	protected com.owera.xaps.dbi.Unit getUnitByMAC(XAPSUnit xapsUnit, Unittype unittype, Profile profile, String searchStr) throws RemoteException, SQLException, NoAvailableConnectionException {
		com.owera.xaps.dbi.Unit unitFoundByMac = xapsUnit.getUnitByValue(searchStr, unittype, profile);
		if (unitFoundByMac != null) {
			return unitFoundByMac;
		} else {
			throw error("The serialNumber/unique value was not found in xAPS on unittype " + unittype.getName() + " and profile " + profile.getName());
		}
	}

	private RemoteException error(String msg) {
		logger.error(msg);
		return new RemoteException(msg);
	}

	public static RemoteException error(Logger logger, String msg) throws RemoteException {
		logger.error(msg);
		return new RemoteException(msg);
	}

	public static RemoteException error(Logger logger, Throwable t) throws RemoteException {
		String msg = "An exception occurred: " + t.getMessage();
		logger.error(msg, t);
		return new RemoteException(msg, t);
	}

	//	protected void validateLogin(Login loginWS) throws RemoteException {
	//		String username = loginWS.getUsername();
	//		if (username == null)
	//			throw error("No username is defined");
	//		String passwordXAPS = Properties.getPassword();
	//		if (passwordXAPS == null)
	//			return; // No password required from the profile
	//		String passwordWS = loginWS.getPassword();
	//		boolean passwordMatch = false;
	//		if (passwordWS != null && passwordWS.equals(passwordXAPS))
	//			passwordMatch = true;
	//		if (!passwordMatch)
	//			throw error("The login password did not match!");
	//	}

	/**
	 * Requires that unittypeName is not null - will return a unittype if all
	 * permissions for this unittype is granted.
	 * 
	 * @param unittypeName
	 * @return
	 * @throws RemoteException
	 */
	//	protected Unittype authorizeUnittypeTotal(String unittypeName) throws RemoteException {
	//		if (unittypeName == null)
	//			throw error("The unittype name is not specified");
	//		Unittype unittype = xaps.getUnittype(unittypeName);
	//		if (unittype == null)
	//			throw error("The unittype " + unittypeName + " is not found in xAPS");
	//		Permissions permissions = id.getUser().getPermissions();
	//		if (permissions.getPermissions().length == 0) {
	//			
	//			return unittype;
	//		}
	//		Permission perm = permissions.getByUnittypeProfile(unittype.getId(), null);
	//		if (perm == null)
	//			throw error("The login " + id.getUser().getUsername() + " does not have full access to unittype " + unittypeName);
	//		else {
	//			
	//			return unittype;
	//		}
	//	}

	/**
	 * This method will return the object from DBI if it can find it. Reasons
	 * for not finding it is wrong name specified by the clients or no access
	 * to the object for this user. However, returning the object does not implicate
	 * all actions are allowed on this object (like add/change/delete). This
	 * permission check is performed in DBI, and will throw IllegalArgumentExceptions
	 * if it occur. 
	 * @param unittypeName
	 * @return
	 * @throws RemoteException
	 */
	protected Unittype getUnittypeFromXAPS(String unittypeName) throws RemoteException {
		if (unittypeName == null)
			throw error("The unittype name is not specified");
		Unittype unittype = xaps.getUnittype(unittypeName);
		if (unittype == null)
			throw error("The unittype " + unittypeName + " is not found/allowed in xAPS");
		//		Permissions permissions = id.getUser().getPermissions();
		//		if (permissions.getPermissions().length == 0) {
		
		return unittype;
		//		}
		//		for (Permission p : permissions.getPermissions()) {
		//			if (p.getUnittypeId().intValue() == unittype.getId()) {
		//				
		//				return unittype;
		//			}
		//		}
		//		throw error("The login " + id.getUser().getUsername() + " does not have access to unittype " + unittypeName);
	}

	/**
	 * This method will return the object from DBI if it can find it. Reasons
	 * for not finding it is wrong name specified by the clients or no access
	 * to the object for this user. However, returning the object does not implicate
	 * all actions are allowed on this object (like add/change/delete). This
	 * permission check is performed in DBI, and will throw IllegalArgumentExceptions
	 * if it occur. 
	 * @param unittypeName
	 * @param profileName
	 * @return
	 * @throws RemoteException
	 */
	protected Profile getProfileFromXAPS(String unittypeName, String profileName) throws RemoteException {
		Unittype unittype = getUnittypeFromXAPS(unittypeName);
		if (profileName == null)
			throw error("The profile name is not specified");
		Profile profile = unittype.getProfiles().getByName(profileName);
		if (profile == null)
			throw error("The profile " + profileName + " is not found/allowed in xAPS");
		//		Permissions permissions = id.getUser().getPermissions();
		//		if (permissions.getPermissions().length == 0) {
		//			
		//			return profile;
		//		}
		//		if (permissions.getByUnittypeProfile(unittype.getId(), profile.getId()) == null)
		//			if (permissions.getByUnittypeProfile(unittype.getId(), null) == null)
		//				throw error("The login " + id.getUser().getUsername() + " does not have full access to profile " + profileName);
		
		return profile;
	}

	public Identity getId() {
		return id;
	}

}
