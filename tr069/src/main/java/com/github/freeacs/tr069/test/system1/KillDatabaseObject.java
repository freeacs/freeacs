package com.github.freeacs.tr069.test.system1;

public class KillDatabaseObject {

	// reference to file which contains response of GetParameterValuesRequest (sorted alphabetically)
	private String gpvFile = "null";
	// reference to file which contains response of GetParameterNamesRequest (sorted alphabetically)
	private String gpnFile = "null";
	// an index which reference a certain parameter in the two files above 
	private int paramIndex = 0;
	// an index which reference a certain step of test for this specific parameter
	private int testIndex = 0;
	// the expected result
	private String expectedResult = "mayfail";
	// test started and running
	private boolean testRunning = false;

	public KillDatabaseObject() {

	}

	// We expect every row to have all columns
	public KillDatabaseObject(String row) {
		String[] arr = row.split(";");
		gpnFile = arr[0];
		gpvFile = arr[1];
		paramIndex = new Integer(arr[2]);
		testIndex = new Integer(arr[3]);
		expectedResult = arr[4];
		testRunning = arr[5].equals("true");
	}

	public String getGpvFile() {
		return gpvFile;
	}

	public void setGpvFile(String gpvFile) {
		this.gpvFile = gpvFile;
	}

	public String getGpnFile() {
		return gpnFile;
	}

	public void setGpnFile(String gpnFile) {
		this.gpnFile = gpnFile;
	}

	public int getParamIndex() {
		return paramIndex;
	}

	public void setParamIndex(int paramIndex) {
		this.paramIndex = paramIndex;
	}

	public int getTestIndex() {
		return testIndex;
	}

	public void setTestIndex(int testIndex) {
		this.testIndex = testIndex;
	}

	public String getExpectedResult() {
		return expectedResult;
	}

	public void setExpectedResult(String expectedResult) {
		this.expectedResult = expectedResult;
	}

	public String toString() {
		return gpnFile + ";" + gpvFile + ";" + paramIndex + ";" + testIndex + ";" + expectedResult + ";" + testRunning;

	}

	public boolean isTestRunning() {
		return testRunning;
	}

	public void setTestRunning(boolean testRunning) {
		this.testRunning = testRunning;
	}
}
