package com.owera.xaps.web.app.page.staging;

import java.util.Comparator;


/**
 * The Class ShippedUnitComparator.
 */
public class ShippedUnitComparator implements Comparator<ShippedUnit> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(ShippedUnit o1, ShippedUnit o2) {
		if (o1 != null && o2 != null) {
			if (o1.getStatus().equals("NOT CONNECTED") && o2.getStatus().equals("NOT CONNECTED")) {
				if (o1.getRegisteredTms() != null && o2.getRegisteredTms() != null)
					return -o1.getRegisteredTms().compareTo(o2.getRegisteredTms());
				else
					return 0;
			}
			if (o1.getStatus().equals("NOT CONNECTED"))
				return -1;
			if (o2.getStatus().equals("NOT CONNECTED"))
				return 1;
			if (o1.getStagedTms() != null && o2.getStagedTms() != null)
				return -o1.getStagedTms().compareTo(o2.getStagedTms());
		}
		return 0;
	}

}
