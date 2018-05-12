package com.github.freeacs.web.app.page.staging;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;

import java.util.Map;


/**
 * The Class StagingReturnData.
 */
public class StagingReturnData extends InputData {
	
	/** The unit list. */
	private Input unitList = Input.getFileInput("unitlist");
	
	/** The mac. */
	private Input mac = Input.getStringInput("mac");
	
	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.input.InputData#bindForm(java.util.Map)
	 */
	@Override
	public void bindForm(Map<String, Object> root) {
		
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
	 * Validate return shipment.
	 *
	 * @return true, if successful
	 */
	public boolean validateReturnShipment() {
		boolean valid = true;
		if(unitList.getFile().getSize()==0 && (mac.getString()==null || mac.getString().trim().length()==0)){
			unitList.setError("Could not return units. Specify a file containing a list of macs or input a single mac.");
			valid = false;
		}
		return valid;
	}

	/**
	 * Gets the unit list.
	 *
	 * @return the unit list
	 */
	public Input getUnitList() {
		return unitList;
	}

	/**
	 * Sets the unit list.
	 *
	 * @param unitList the new unit list
	 */
	public void setUnitList(Input unitList) {
		this.unitList = unitList;
	}

	/**
	 * Gets the mac.
	 *
	 * @return the mac
	 */
	public Input getMac() {
		return mac;
	}

	/**
	 * Sets the mac.
	 *
	 * @param mac the new mac
	 */
	public void setMac(Input mac) {
		this.mac = mac;
	}

}
