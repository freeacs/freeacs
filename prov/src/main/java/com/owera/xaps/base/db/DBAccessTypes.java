package com.owera.xaps.base.db;

import com.owera.common.counter.MeasurementTypes;

public class DBAccessTypes implements MeasurementTypes {

	public static final int OK = 0;
	public static final int SQLEXCEPTION = 1;
	public static final int NOAVAILCONN = 2;
	public static final int OTHER = 3;
	
	
	
	
	public int[] getTypes() {
		return new int[] {OK,SQLEXCEPTION,NOAVAILCONN, OTHER};
	}

	public String[] getTypesText() {
		return new String[] {"OK", "SQL-EX", "NO-CONN", "OTHER"};
	}

}
