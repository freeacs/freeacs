package com.github.freeacs.web.app.page.user;

import com.github.freeacs.dbi.User;
import com.github.freeacs.dbi.Users;
import com.github.freeacs.web.app.util.SessionCache;
import com.github.freeacs.web.app.util.SessionData;

import javax.sql.DataSource;
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
	 * @param xapsDataSource
	 * @return the users
	 * @throws SQLException the sQL exception
	 *  the no available connection exception
	 */
	Users getUsers(String sessionid, DataSource xapsDataSource) throws SQLException {
		return new Users(xapsDataSource);
	}

	/**
	 * Gets the all users.
	 *
	 * @param min the min
	 * @param max the max
	 * @param sessionId the session id
	 * @param group the group
	 * @param xapsDataSource
	 * @return the all users
	 * @throws SQLException the sQL exception
	 *  the no available connection exception
	 */
	UserModel[] getAllUsers(Integer min, Integer max, String sessionId, UserGroupModel group, DataSource xapsDataSource) throws SQLException {
		Users users = getUsers(sessionId, xapsDataSource);
		SessionData sessionData = SessionCache.getSessionData(sessionId);
		User loggedInUser = sessionData.getUser();
		UserModel[] toReturn = UserModel.convertUsers(users.getUsers(loggedInUser), group);
		max = max < toReturn.length + 1 ? max : toReturn.length;
		return Arrays.asList(toReturn).subList(min, max).toArray(new UserModel[] {});
	}
}