package com.owera.xaps.web.app.page.user;

import com.github.freeacs.common.db.ConnectionProperties;
import com.github.freeacs.common.db.NoAvailableConnectionException;
import com.github.freeacs.dbi.User;
import com.github.freeacs.dbi.Users;
import com.owera.xaps.web.app.util.SessionCache;
import com.owera.xaps.web.app.util.SessionData;

import java.sql.SQLException;
import java.util.Arrays;

/**
 * The Class PermissionController.
 */
public abstract class PermissionController {

	/**
	 * Gets the users.
	 *
	 * @param sessionid the sessionid
	 * @return the users
	 * @throws SQLException the sQL exception
	 * @throws NoAvailableConnectionException the no available connection exception
	 */
	Users getUsers(String sessionid) throws SQLException, NoAvailableConnectionException {
		ConnectionProperties props = SessionCache.getXAPSConnectionProperties(sessionid);
		if (props == null)
			throw new NotAllowedException("You are not logged in");
		return new Users(props);
	}

	/**
	 * Gets the all users.
	 *
	 * @param min the min
	 * @param max the max
	 * @param sessionId the session id
	 * @param group the group
	 * @return the all users
	 * @throws SQLException the sQL exception
	 * @throws NoAvailableConnectionException the no available connection exception
	 */
	UserModel[] getAllUsers(Integer min, Integer max, String sessionId, UserGroupModel group) throws SQLException, NoAvailableConnectionException {
		Users users = getUsers(sessionId);
		SessionData sessionData = SessionCache.getSessionData(sessionId);
		User loggedInUser = sessionData.getUser();
		UserModel[] toReturn = UserModel.convertUsers(users.getUsers(loggedInUser), group);
		max = max < toReturn.length + 1 ? max : toReturn.length;
		return Arrays.asList(toReturn).subList(min, max).toArray(new UserModel[] {});
	}
}