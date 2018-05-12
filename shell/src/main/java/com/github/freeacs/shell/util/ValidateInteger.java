package com.github.freeacs.shell.util;

public class ValidateInteger implements ValidateInput {

	private Integer min;

	private Integer max;

	public ValidateInteger(Integer minInc, Integer maxExc) {
		this.min = minInc;
		this.max = maxExc;
	}

	public boolean validate(String input) {
		try {
			int i = Integer.parseInt(input);
			if (min != null && i < min)
				return false;
			if (max != null && i >= max)
				return false;
			return true;

		} catch (NumberFormatException nfe) {
			return false;
		}
	}

}
