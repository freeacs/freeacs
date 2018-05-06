package com.owera.xaps.web.app.page.staging;

import java.util.Map;

import com.owera.xaps.web.app.input.Input;
import com.owera.xaps.web.app.input.InputData;


/**
 * The Class StagingDistributorData.
 */
public class StagingDistributorData extends InputData {
	
	/** The model name. */
	private Input modelName = Input.getStringInput("modelname");
	
	/** The distributor name. */
	private Input distributorName = Input.getStringInput("distributorname");
	
	/** The version number. */
	private Input versionNumber = Input.getStringInput("versionnumber");
	
	/** The taiwan. */
	private Input taiwan = Input.getFileInput("taiwan");
	
	/** The new distributor name. */
	private Input newDistributorName = Input.getStringInput("distributor");
	
	/** The distributor. */
	private Input distributor = Input.getStringInput("distributor");
	
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

	/**
	 * Gets the model name.
	 *
	 * @return the model name
	 */
	public Input getModelName() {
		return modelName;
	}

	/**
	 * Sets the model name.
	 *
	 * @param modelName the new model name
	 */
	public void setModelName(Input modelName) {
		this.modelName = modelName;
	}

	/**
	 * Gets the distributor name.
	 *
	 * @return the distributor name
	 */
	public Input getDistributorName() {
		return distributorName;
	}

	/**
	 * Sets the distributor name.
	 *
	 * @param distributorName the new distributor name
	 */
	public void setDistributorName(Input distributorName) {
		this.distributorName = distributorName;
	}

	/**
	 * Gets the version number.
	 *
	 * @return the version number
	 */
	public Input getVersionNumber() {
		return versionNumber;
	}

	/**
	 * Sets the version number.
	 *
	 * @param versionNumber the new version number
	 */
	public void setVersionNumber(Input versionNumber) {
		this.versionNumber = versionNumber;
	}

	/**
	 * Gets the taiwan.
	 *
	 * @return the taiwan
	 */
	public Input getTaiwan() {
		return taiwan;
	}

	/**
	 * Sets the taiwan.
	 *
	 * @param taiwan the new taiwan
	 */
	public void setTaiwan(Input taiwan) {
		this.taiwan = taiwan;
	}

	/**
	 * Gets the new distributor name.
	 *
	 * @return the new distributor name
	 */
	public Input getNewDistributorName() {
		return newDistributorName;
	}

	/**
	 * Sets the new distributor name.
	 *
	 * @param newDistributorName the new new distributor name
	 */
	public void setNewDistributorName(Input newDistributorName) {
		this.newDistributorName = newDistributorName;
	}

	/**
	 * Gets the distributor.
	 *
	 * @return the distributor
	 */
	public Input getDistributor() {
		return distributor;
	}

	/**
	 * Sets the distributor.
	 *
	 * @param distributor the new distributor
	 */
	public void setDistributor(Input distributor) {
		this.distributor = distributor;
	}

}
