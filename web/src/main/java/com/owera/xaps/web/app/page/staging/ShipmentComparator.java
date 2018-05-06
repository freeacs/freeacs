package com.owera.xaps.web.app.page.staging;

import java.util.Comparator;


/**
 * The Class ShipmentComparator.
 */
public class ShipmentComparator implements Comparator<Shipment> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Shipment o1, Shipment o2) {
		return o2.getName().compareTo(o1.getName());
	}

}
