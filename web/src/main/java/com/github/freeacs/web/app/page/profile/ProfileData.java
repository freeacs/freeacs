package com.github.freeacs.web.app.page.profile;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;

import java.util.Map;


/**
 * The Class ProfileData.
 */
public class ProfileData extends InputData {
	
	/** The profilename. */
	private Input profilename = Input.getStringInput("profilename");
	
	/** The profile copy. */
	private Input profileCopy = Input.getStringInput("profilecopy");
	
	/** The cmd. */
	private Input cmd = Input.getStringInput("cmd");

	/**
	 * Gets the profile copy.
	 *
	 * @return the profile copy
	 */
	public Input getProfileCopy() {
		return profileCopy;
	}

	/**
	 * Sets the profile copy.
	 *
	 * @param profileCopy the new profile copy
	 */
	public void setProfileCopy(Input profileCopy) {
		this.profileCopy = profileCopy;
	}

	/**
	 * Gets the profilename.
	 *
	 * @return the profilename
	 */
	public Input getProfilename() {
		return profilename;
	}

	/**
	 * Sets the profilename.
	 *
	 * @param profilename the new profilename
	 */
	public void setProfilename(Input profilename) {
		this.profilename = profilename;
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.input.InputData#setCmd(com.owera.xaps.web.app.input.Input)
	 */
	public void setCmd(Input cmd) {
		this.cmd = cmd;
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.input.InputData#getCmd()
	 */
	public Input getCmd() {
		return cmd;
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.input.InputData#bindForm(java.util.Map)
	 */
	@Override
	public void bindForm(Map<String, Object> root) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.input.InputData#validateForm()
	 */
	@Override
	public boolean validateForm() {
		// TODO Auto-generated method stub
		return false;
	}
}
