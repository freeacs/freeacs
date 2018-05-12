package com.github.freeacs.web.app.page.login;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;

import java.util.Map;


/**
 * The Class LoginData.
 */
public class LoginData extends InputData {

	/** The url. */
	private Input url = Input.getStringInput("url");
	
	/** The user. */
	private Input user = Input.getStringInput("user");
	
	/** The pass. */
	private Input pass = Input.getStringInput("pass");
	
	/** The index. */
	private Input index = Input.getStringInput("index");
	
	/** The form submit. */
	private Input formSubmit = Input.getStringInput("formsubmit");
	
	/** The logoff. */
	private Input logoff = Input.getStringInput("logoff");

	/**
	 * Gets the url.
	 *
	 * @return the url
	 */
	public Input getUrl() {
		return url;
	}

	/**
	 * Sets the url.
	 *
	 * @param url the new url
	 */
	public void setUrl(Input url) {
		this.url = url;
	}

	/**
	 * Gets the user.
	 *
	 * @return the user
	 */
	public Input getUser() {
		return user;
	}

	/**
	 * Sets the user.
	 *
	 * @param user the new user
	 */
	public void setUser(Input user) {
		this.user = user;
	}

	/**
	 * Gets the index.
	 *
	 * @return the index
	 */
	public Input getIndex() {
		return index;
	}

	/**
	 * Sets the index.
	 *
	 * @param index the new index
	 */
	public void setIndex(Input index) {
		this.index = index;
	}

	/**
	 * Gets the pass.
	 *
	 * @return the pass
	 */
	public Input getPass() {
		return pass;
	}

	/**
	 * Sets the pass.
	 *
	 * @param pass the new pass
	 */
	public void setPass(Input pass) {
		this.pass = pass;
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.input.InputData#getFormSubmit()
	 */
	public Input getFormSubmit() {
		return formSubmit;
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.input.InputData#setFormSubmit(com.owera.xaps.web.app.input.Input)
	 */
	public void setFormSubmit(Input formSubmit) {
		this.formSubmit = formSubmit;
	}

	/**
	 * Gets the logoff.
	 *
	 * @return the logoff
	 */
	public Input getLogoff() {
		return logoff;
	}

	/**
	 * Sets the logoff.
	 *
	 * @param logoff the new logoff
	 */
	public void setLogoff(Input logoff) {
		this.logoff = logoff;
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
