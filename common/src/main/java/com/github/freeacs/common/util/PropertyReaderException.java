package com.github.freeacs.common.util;

public class PropertyReaderException extends RuntimeException {

	private static final long serialVersionUID = 1464507105516835591L;

	public PropertyReaderException(String propertyfile) {
		super("The propertyfile " + propertyfile + " could not be found");
	}
}
