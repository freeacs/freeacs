package com.github.freeacs.common.counter;

public class BooleanMeasurementTypes implements MeasurementTypes {

	public static final int ERROR = 0;
	public static final int OK = 1;
	
	
	
	public int[] getTypes() {
		return new int[] {ERROR,OK};
	}

	public String[] getTypesText() {
		return new String[] {"ERROR", "OK"};
	}

}
