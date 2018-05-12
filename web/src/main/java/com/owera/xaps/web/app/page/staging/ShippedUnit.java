package com.owera.xaps.web.app.page.staging;

import com.github.freeacs.dbi.UnitJobStatus;


/**
 * The class contains both the Unit object (with all unit parameters) and the 
 * status of the job regarding this particular unit.
 * 
 * Because of the peculiarities with shipment (the job is completed/finished when it has reached
 * the unconfirmed_failed status) we add some methods to hide this logic.
 * 
 * @author Morten
 *
 */
public class ShippedUnit {
	
	/** The unit id. */
	private String unitId;
	
	/** The serial number. */
	private String serialNumber;
	
	/** The registered tms. */
	private String registeredTms;
	
	/** The status. */
	private String status = "NOT CONNECTED";
	
	/** The staged tms. */
	private String stagedTms;

	/**
	 * Instantiates a new shipped unit.
	 *
	 * @param unitId the unit id
	 * @param serialNumber the serial number
	 * @param status the status
	 * @param registeredTms the registered tms
	 * @param stagedTms the staged tms
	 */
	public ShippedUnit(String unitId, String serialNumber, String status, String registeredTms, String stagedTms) {
		this.unitId = unitId;
		this.serialNumber = serialNumber;
		if (status != null && !status.trim().equals(""))
			this.status = status;
		this.registeredTms = registeredTms;
		this.stagedTms = stagedTms;
	}

	/**
	 * Gets the serial number.
	 *
	 * @return the serial number
	 */
	public String getSerialNumber() {
		return serialNumber;
	}

	/**
	 * Gets the registered tms.
	 *
	 * @return the registered tms
	 */
	public String getRegisteredTms() {
		return registeredTms;
	}

	/**
	 * Gets the unit id.
	 *
	 * @return the unit id
	 */
	public String getUnitId() {
		return unitId;
	}

	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Gets the staged tms.
	 *
	 * @return the staged tms
	 */
	public String getStagedTms() {
		return stagedTms;
	}

	/**
	 * Checks if is staged.
	 *
	 * @return true, if is staged
	 */
	public boolean isStaged() {
		return status.equals(UnitJobStatus.COMPLETED_OK) || status.equals(UnitJobStatus.UNCONFIRMED_FAILED);
	}
}
