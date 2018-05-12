package com.github.freeacs.common.counter;

/**
 * This interface represents the types/status you want to measure. Let's say you
 * have this status in your system:
 * 
 * OK, ERROR, RESEND
 * 
 * Then you should make an implementation of this interface which return {0,1,2}
 * and {"OK","ERROR","RESEND"}
 * 
 * @author me3
 * 
 */
public interface MeasurementTypes {
	public int[] getTypes();

	public String[] getTypesText();

}
