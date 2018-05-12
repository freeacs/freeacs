package com.github.freeacs.web.app.page.staging;


/**
 * The Class Shipment.
 */
public class Shipment {
	
	/** The name. */
	private String name;
	
	/** The canceled. */
	private boolean canceled;
	
	/** The time. */
	private String time;

	/**
	 * Instantiates a new shipment.
	 *
	 * @param n the n
	 * @param c the c
	 */
	public Shipment(String n, boolean c) {
		name = n;
		setTime(n.substring(n.lastIndexOf(':') + 1).replace('_', ' '));
		canceled = c;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	protected void setName(String name) {
		this.name = name;
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
	 * Sets the canceled.
	 *
	 * @param canceled the new canceled
	 */
	protected void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}

	/**
	 * Checks if is canceled.
	 *
	 * @return true, if is canceled
	 */
	public boolean isCanceled() {
		return canceled;
	}

	/**
	 * Sets the time.
	 *
	 * @param time the new time
	 */
	public void setTime(String time) {
		this.time = time;
	}

	/**
	 * Gets the time.
	 *
	 * @return the time
	 */
	public String getTime() {
		return time;
	}

}