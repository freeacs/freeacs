package com.github.freeacs.web.app.page.certificates;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;

import java.util.Map;


/**
 * Contains the input data definitions for the Certificate page.
 * 
 * @author Jarl Amdré Hübenthal
 *
 */
public class CryptoData extends InputData {
	/** The name. */
	private Input name = Input.getStringInput("name");
	/** The id. */
	private Input id = Input.getIntegerInput("id");
	/** The certificate. */
	private Input certificate = Input.getFileInput("certificate");
	/** The action. */
	private Input action = Input.getStringInput("action");
	
	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.input.InputData#bindForm(java.util.Map)
	 */
	@Override
	public void bindForm(Map<String, Object> root) {
		root.put(name.getKey(), name.getString());
		root.put(certificate.getKey(), certificate.getString());
	}
	
	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.input.InputData#validateForm()
	 */
	@Override
	public boolean validateForm(){
		boolean valid = true;
		if(certificate.getFileAsString()==null || certificate.getFileAsString().length()<1){
			certificate.setError("File is empty");
			valid=false;
		}
		return valid;
	}
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public Input getName() {
		return name;
	}
	
	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(Input name) {
		this.name = name;
	}
	
	/**
	 * Gets the certificate.
	 *
	 * @return the certificate
	 */
	public Input getCertificate() {
		return certificate;
	}
	
	/**
	 * Sets the certificate.
	 *
	 * @param certificate the new certificate
	 */
	public void setCertificate(Input certificate) {
		this.certificate = certificate;
	}
	
	/**
	 * Gets the action.
	 *
	 * @return the action
	 */
	public Input getAction() {
		return action;
	}
	
	/**
	 * Sets the action.
	 *
	 * @param action the new action
	 */
	public void setAction(Input action) {
		this.action = action;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public Input getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(Input id) {
		this.id = id;
	}
}
