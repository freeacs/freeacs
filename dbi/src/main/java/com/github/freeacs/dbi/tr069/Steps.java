package com.github.freeacs.dbi.tr069;

import com.github.freeacs.dbi.tr069.TestCaseParameter.TestCaseParameterType;

public class Steps {

	public enum Step {
		// The EXE step is only used for FILE method
		// The GET,SET steps are only used for the VALUE/ATTRIBUTE method
		REBOOT, RESET, GET, SET, EXECUTE, EMPTY;
	}

	private Step[] steps;

	private Step currentStep;

	private int index = 0;

	public Steps(String stepsStr) {
		steps = buildSteps(stepsStr);
		index = -1;
		currentStep = null; // initial state is null
	}

	// TEST_STEPS may contains a comma-separated list of STEPS: GET, SET, REBOOT, RESET
	// We expect maximum 1 occurrence of each STEP, not allowed to write: GET, GET
	private Step[] buildSteps(String stepsStr) {
		String[] stepStrArray = stepsStr.split(",");
		Step[] stepArray = new Step[stepStrArray.length];
		int counter = 0;
		for (String stepStr : stepStrArray) {
			Step step = null;
			try {
				step = Step.valueOf(stepStr.trim());
			} catch (IllegalArgumentException iae) {
				throw new IllegalArgumentException("The step " + stepStr.trim() + " is not a valid step");
			}
			for (Step alreadyAddedStep : stepArray) {
				if (alreadyAddedStep == step)
					throw new IllegalArgumentException("The step " + stepStr.trim() + "  is invalid because it is repeated");
			}
			stepArray[counter++] = step;
		}
		return stepArray;
	}

	public boolean lastStep() {
		if (currentStep == steps[steps.length - 1])
			return true;
		else
			return false;
	}

	/**
	 * Will return GET or FAC:
	 * SET: Will reset any state about RESET 
	 * GET: MUST come after SET, but no RESET can come between SET and GET
	 * FAC: MUST come after SET AND a RESET
	 * 
	 * Will return null if GET is first in step-sequence --> no comparison with TestCaseParameter
	 * @return
	 * @throws TestException 
	 */
	public TestCaseParameterType getCompareType() {
		boolean setMode = false;
		boolean reset = false;
		for (Step s : steps) {
			if (s == Step.SET) {
				setMode = true;
				reset = false;
			}
			if (s == Step.RESET)
				reset = true;
			if (s == Step.GET) {
				if (setMode && reset)
					return TestCaseParameterType.FAC;
				if (setMode)
					return TestCaseParameterType.GET;
			}
		}
		return null;
	}

	public Step getCurrentStep() {
		return currentStep;
	}
	
	public void setCurrentStep(Step step) {
		this.currentStep = step;
	}

	/**
	 * Return the next step in steps. Return null if no more steps found.
	 * @param tu
	 * @return
	 */
	public Step next() {
		if (currentStep != null && currentStep == Step.REBOOT || currentStep == Step.RESET)
			currentStep = Step.EMPTY;
		else if (index < steps.length - 1) // next step
			currentStep = steps[++index];
		else { // no more steps
			currentStep = null;
		}
		return currentStep;
	}

	/**
	 * Reset index to -1, to start over on the steps - next() will return Step again
	 */
	public void reset() {
		index = -1;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Step s : steps) {
			sb.append(s.toString());
			sb.append(",");
		}
		return sb.substring(0, sb.length() - 1);
	}
	
}
