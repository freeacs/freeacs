package com.github.freeacs.tr069;

public class UnknownMethodException extends RuntimeException {

	private static final long serialVersionUID = 6365246367824984258L;

	public UnknownMethodException(String methodname) {
		super("The method " + methodname + " was not not recognized by the server.");
	}
}
