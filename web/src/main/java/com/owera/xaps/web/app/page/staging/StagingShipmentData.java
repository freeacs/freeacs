package com.owera.xaps.web.app.page.staging;

import java.util.Map;

import com.owera.xaps.web.app.input.Input;
import com.owera.xaps.web.app.input.InputData;


/**
 * The Class StagingShipmentData.
 */
public class StagingShipmentData extends InputData {
	
	/** The shipment. */
	private Input shipment = Input.getStringInput("shipment");
	
	/** The shipment name. */
	private Input shipmentName = Input.getStringInput("shipmentname");
	
	/** The unit list. */
	private Input unitList = Input.getFileInput("unitlist");
	
	/** The mac. */
	private Input mac = Input.getStringInput("mac");
	
	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.input.InputData#bindForm(java.util.Map)
	 */
	@Override
	public void bindForm(Map<String, Object> root) {
		root.put(shipmentName.getKey(), shipmentName.getString());
		root.put(shipment.getKey(), shipment.getString());
		root.put(mac.getKey(), mac.getString());
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.input.InputData#validateForm()
	 */
	@Override
	public boolean validateForm() {
		return false;
	}

	/**
	 * Gets the shipment.
	 *
	 * @return the shipment
	 */
	public Input getShipment() {
		return shipment;
	}

	/**
	 * Sets the shipment.
	 *
	 * @param shipment the new shipment
	 */
	public void setShipment(Input shipment) {
		this.shipment = shipment;
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

	/**
	 * Gets the shipment name.
	 *
	 * @return the shipment name
	 */
	public Input getShipmentName() {
		return shipmentName;
	}

	/**
	 * Sets the shipment name.
	 *
	 * @param shipmentName the new shipment name
	 */
	public void setShipmentName(Input shipmentName) {
		this.shipmentName = shipmentName;
	}

	/**
	 * Validate cancel unit.
	 *
	 * @return true, if successful
	 */
	public boolean validateCancelUnit() {
		boolean valid = true;
		if(getUnit().getString()==null || getUnit().getString().trim().length()==0){
			getUnit().setError("Could not cancel the unit. No Unit Id specified.");
			valid = false;
		}
		return valid;
	}

	/**
	 * Validate add shipment.
	 *
	 * @return true, if successful
	 */
	public boolean validateAddShipment() {
		boolean valid = true;
		if(unitList.getFile().getSize()==0 && (mac.getString()==null || mac.getString().trim().length()==0)){
			unitList.setError("Could not add shipment. Specify a file containing a list of macs or input a single mac.");
			valid = false;
		}
		return valid;
	}

}
