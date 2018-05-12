package com.github.freeacs.web.app.page.user;

import com.github.freeacs.dbi.Permission;


/**
 * The Class UserGroupModel.
 */
public class UserGroupModel{
	
	/** The id. */
	private Integer id;
	
	/** The name. */
	private String name;
	
	/** The permissions. */
	private Permission[] permissions;
	
	/**
	 * Instantiates a new user group model.
	 *
	 * @param name the name
	 */
	public UserGroupModel(String name) {
		this.name = name;
	}

	/**
	 * Gets the permissions.
	 *
	 * @return the permissions
	 */
	public Permission[] getPermissions() {
		return permissions;
	}

	/**
	 * Sets the permissions.
	 *
	 * @param permissions the new permissions
	 */
	public void setPermissions(Permission[] permissions) {
		this.permissions = permissions;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
}
