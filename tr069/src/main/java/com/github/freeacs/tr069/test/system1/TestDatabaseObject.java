package com.github.freeacs.tr069.test.system1;

import com.github.freeacs.common.util.NaturalComparator;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class TestDatabaseObject {
	private String step = "7.3.1.txt";
	private String deviceType = "IAD";
	private String testType = "Auto";
	private String report = null;
	private String status = "";
	private String run = "false";

	public TestDatabaseObject() {

	}

	public TestDatabaseObject(String row) {
		if (row != null) {
			String[] rowArray = row.split(";");
			this.step = rowArray[0];
			this.deviceType = rowArray[1];
			this.testType = rowArray[2];
			if (rowArray.length > 3)
				this.report = rowArray[3];
			if (rowArray.length > 4)
				this.status = rowArray[4];
			if (rowArray.length > 5)
				this.run = rowArray[5];
		}
	}

	public String toString() {
		String str = step + ";" + deviceType + ";" + testType + ";";
		if (report != null)
			str += report;
		str += ";";
		if (status != null)
			str += status;
		str += ";" + run;
		return str;
	}

	public String getStep() {
		return step;
	}

	public void setStep(String step) {
		this.step = step;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getTestType() {
		return testType;
	}

	public void setTestType(String testType) {
		this.testType = testType;
	}

	public String getReport() {
		return report;
	}

	public void setReport(String report) {
		this.report = report;
	}

	public String getStatus() {
		File dir = TestServlet.getFileFromResource("tests");
		String[] files = dir.list();
		Arrays.sort(files, new NaturalComparator());
		String[] finished = status.split(":");
		Arrays.sort(finished, new NaturalComparator());
		int match = 0;
		for (int i = 0; i < finished.length; i++) {
			for (int j = 0; j < files.length; j++) {
				if (finished[i].equals(files[j]))
					match++;
			}
		}
		if (match == files.length - 2)
			return "Completed (" + match + " of " + (files.length - 2) + ")";
		else
			return "Not completed (" + match + " of " + (files.length - 2) + ")";
	}

	public String getLastStep() {
		if (status != null && status.length() > 0) {
			String[] finished = status.split(":");
			Arrays.sort(finished, new NaturalComparator());
			return finished[finished.length - 1];
		}
		return "7.3.1.txt";
	}

	public void addOk(String step) {
		String[] finished = status.split(":");
		Map<String, String> finishedMap = new TreeMap<String, String>(new NaturalComparator());
		for (String finishedStep : finished) {
			finishedMap.put(finishedStep, "OK");
		}
		finishedMap.put(step, "OK");
		String tmp = "";
		for (String finishedStep : finishedMap.keySet()) {
			tmp += finishedStep + ":";
		}
		if (tmp.endsWith(":"))
			tmp = tmp.substring(0, tmp.length() - 1);
		status = tmp;
	}

	public String getRun() {
		return run;
	}

	public void setRun(String run) {
		this.run = run;
	}

}
