package com.github.freeacs.tr069.test.system2;

import com.github.freeacs.base.Log;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.UnitParameter;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.tr069.Steps;
import com.github.freeacs.dbi.tr069.Steps.Step;
import com.github.freeacs.dbi.tr069.TestCase;
import com.github.freeacs.dbi.tr069.TestHistory;
import com.github.freeacs.dbi.util.SystemParameters;

import java.util.List;

/**
 * This class defines a group of TestCases, and how to execute them.
 * Also contains state, to tell the server what to do next
 * 
 * @author Morten
 *
 */
public class TestUnit {

	public enum TestState {
		STARTTEST, STARTCASE, ENDCASE, ENDTEST;
	}

	private Unittype unittype;
	private Unit unit;
	private List<TestCase> testCases;
	private TestCase currentCase;
	//	private boolean endOfTest = false;
	private TestHistory history;
	private Steps steps;
	private boolean resetOnStartup;
	private TestState testState = TestState.STARTTEST;

	public TestUnit(Unittype unittype, Unit unit, List<TestCase> testCases) throws TestException {
		super();
		this.unittype = unittype;
		this.unit = unit;
		if (unit == null)
			throw new TestException("No Unit object - not possible to run tests");
		this.testCases = testCases;
		if (testCases == null || testCases.size() == 0)
			throw new TestException("No test cases are defined - possibly because the filters do not match any test cases - not possible to run tests");
		UnitParameter stepsUp = unit.getUnitParameters().get(SystemParameters.TEST_STEPS);
		if (stepsUp == null || stepsUp.getValue() == null || stepsUp.getValue().trim().equals(""))
			throw new TestException("The unit parameter " + SystemParameters.TEST_STEPS + " is not defined - not possible to run tests");
		try {
			this.steps = new Steps(stepsUp.getValue());
		} catch (IllegalArgumentException iae) {
			throw new TestException(iae.getMessage() + " - not possible to run tests");
		}
		UnitParameter resetOnStartupUp = unit.getUnitParameters().get(SystemParameters.TEST_RESET_ON_STARTUP);
		if (resetOnStartupUp != null) {
			String resetOnStartupStr = resetOnStartupUp.getValue();
			if (resetOnStartupStr.equals("1") || resetOnStartupStr.equalsIgnoreCase("true"))
				resetOnStartup = true;
			else if (resetOnStartupStr.equals("0") || resetOnStartupStr.equalsIgnoreCase("false"))
				resetOnStartup = false;
			else
				throw new TestException("The unit parameter " + SystemParameters.TEST_RESET_ON_STARTUP + " is set to a non-valid value (" + resetOnStartupUp.getValue()
						+ ") - not possible to run tests");
		}
	}

	public Steps getSteps() {
		return steps;
	}

	public Unittype getUnittype() {
		return unittype;
	}

	public void setUnittype(Unittype unittype) {
		this.unittype = unittype;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	//	public List<TestCase> getTestCases() {
	//		return testCases;
	//	}

	public void setTestCases(List<TestCase> testCases) {
		this.testCases = testCases;
	}

	public TestCase getCurrentCase() {
		return currentCase;
	}

	public Step getCurrentStep() {
		return steps.getCurrentStep();
	}

	public void setCurrentCase(TestCase currentCase) {
		this.currentCase = currentCase;
	}

	public void next() {

		//		CaseStep caseStep = null;

		// Always: STARTTEST -> STARTCASE
		if (testState == TestState.STARTTEST) {
			testState = TestState.STARTCASE;
			//			Step step = null;
			if (resetOnStartup) {
				steps.setCurrentStep(Step.RESET);
			} else {
				/*step = */steps.next(); // Will never return null, since there must be at least 1 step
			}
			currentCase = testCases.get(0);
			//			caseStep = new CaseStep(currentCase, step);
		}

		// If no more steps: STARTCASE -> ENDCASE 
		else if (testState == TestState.STARTCASE) {
			Step step = steps.next();
			if (step == null) {
				testState = TestState.ENDCASE;
				history = null;
			}
			//			caseStep = new CaseStep(currentCase, step);
		}

		// If no more cases: ENDCASE -> ENDTEST, else ENDCASE -> STARTCASE
		else if (testState == TestState.ENDCASE) {
			TestCase nextCase = null;
			boolean match = false;
			for (TestCase tc : testCases) {
				if (match) {
					nextCase = tc;
					break;
				}
				if (tc.getId().intValue() == currentCase.getId())
					match = true;
			}
			//			Step step = null;
			if (nextCase == null)
				testState = TestState.ENDTEST;
			else {
				testState = TestState.STARTCASE;
				steps.reset();
				/*step =*/steps.next();
			}
			currentCase = nextCase;
			//			caseStep = new CaseStep(currentCase, step);
		}

		// Do nothing
		else { // testState == TestState.ENDTEST
			currentCase = null;
			//			caseStep = new CaseStep(null, null);
		}

		//		return caseStep;
		String logMsg = "Next case-step : ";
		if (currentCase != null)
			logMsg += currentCase.getId() + ",";
		else
			logMsg += "null,";
		if (steps != null)
			logMsg += steps.getCurrentStep();
		else
			logMsg += "null";
		Log.debug(TestUnit.class, logMsg);
	}

	public TestHistory getHistory() {
		return history;
	}

	public void setHistory(TestHistory history) {
		this.history = history;
	}

	public TestState getTestState() {
		return testState;
	}

	public void setTestState(TestState testState) {
		this.testState = testState;
	}

}
