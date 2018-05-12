package com.github.freeacs.web.app.page.user;

import com.github.freeacs.dbi.User;
import com.github.freeacs.dbi.Users;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class UserModel.
 */
public class UserModel {

	/** The id. */
	private Long id = null;

	/** The username. */
	private String username = null;

	/** The fullname. */
	private String fullname = null;

	/** The access. */
	private String access = null;

	/** The password. */
	private String password = null;

	/** The admin flag */
	private Boolean admin = null;

	public Boolean getAdmin() {
		return admin;
	}

	public void setAdmin(Boolean admin) {
		this.admin = admin;
	}

	/** The group. */
	private UserGroupModel group;

	/**
	 * From user.
	 *
	 * @param user the user
	 * @param group the group
	 * @return the user model
	 */
	public static UserModel fromUser(User user, UserGroupModel group) {
		UserModel newUser = new UserModel();
		newUser.setId(user.getId().longValue());
		newUser.setUsername(user.getUsername());
		newUser.setFullname(user.getFullname());
		newUser.setAccess(user.getAccess());
		newUser.setGroup(group);
		return newUser;
	}

	/**
	 * To user.
	 *
	 * @param user the user
	 * @return the user
	 */
	public static User toUser(UserModel user, Users users) {
		User newUser = new User(user.getUsername(), user.getFullname(), user.getAccess(), user.getAdmin(), users);
		newUser.setSecretHashed(user.getUsername());
		// TODO set group
		// newUser.setGroup(user.toDBIGroup());
		return newUser;
	}

	/**
	 * Convert users.
	 *
	 * @param users the users
	 * @param group the group
	 * @return the user model[]
	 */
	public static UserModel[] convertUsers(User[] users, UserGroupModel group) {
		List<UserModel> newUsers = new ArrayList<UserModel>();
		for (User user : users) {
			newUsers.add(fromUser(user, group));
		}
		return newUsers.toArray(new UserModel[] {});
	}

	/**
	 * Instantiates a new user model.
	 */
	public UserModel() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the id
	 * @return the user model
	 */
	public UserModel setId(Long id) {
		this.id = id;
		return this;
	}

	/**
	 * Gets the username.
	 *
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Sets the username.
	 *
	 * @param username the new username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Gets the fullname.
	 *
	 * @return the fullname
	 */
	public String getFullname() {
		return fullname;
	}

	/**
	 * Sets the fullname.
	 *
	 * @param fullname the new fullname
	 */
	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	/**
	 * Gets the access.
	 *
	 * @return the access
	 */
	public String getAccess() {
		return access;
	}

	/**
	 * Sets the access.
	 *
	 * @param access the new access
	 */
	public void setAccess(String access) {
		this.access = access;
	}

	/**
	 * Gets the password.
	 *
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password.
	 *
	 * @param password the new password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Gets the group.
	 *
	 * @return the group
	 */
	public UserGroupModel getGroup() {
		return group;
	}

	/**
	 * Sets the group.
	 *
	 * @param group the new group
	 */
	public void setGroup(UserGroupModel group) {
		this.group = group;
	}
}
