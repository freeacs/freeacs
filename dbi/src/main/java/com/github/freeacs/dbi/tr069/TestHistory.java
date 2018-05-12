package com.github.freeacs.dbi.tr069;

import com.github.freeacs.dbi.Unittype;

import java.util.Date;

public class TestHistory {

	private Integer id;
	private Unittype unittype;
	private Date startTimestamp;
	private Date endTimestamp;
	private String unitId;
	private Integer testCaseId;
	private Boolean failed;
	private Boolean expectError;
	private String result;

	
	public TestHistory() {
		this(null, null, null, null, null);
	}
	
	public TestHistory(Unittype unittype, Date startTimestamp, String unitId, Integer testCaseId, Boolean expectError) {
		this.unittype = unittype;
		this.startTimestamp = startTimestamp;
		this.unitId = unitId;
		this.testCaseId = testCaseId;
		this.expectError = expectError;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public Date getStartTimestamp() {
		return startTimestamp;
	}

	public void setStartTimestamp(Date startTimestamp) {
		this.startTimestamp = startTimestamp;
	}

	public Date getEndTimestamp() {
		return endTimestamp;
	}

	public void setEndTimestamp(Date endTimestamp) {
		this.endTimestamp = endTimestamp;
	}

	public Boolean getFailed() {
		return failed;
	}

	public void setFailed(Boolean failed) {
		this.failed = failed;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUnitId() {
		return unitId;
	}

	public void setUnitId(String unitId) {
		this.unitId = unitId;
	}

	public Integer getTestCaseId() {
		return testCaseId;
	}

	public void setTestCaseId(Integer testCaseId) {
		this.testCaseId = testCaseId;
	}

	public Unittype getUnittype() {
		return unittype;
	}

	public void setUnittype(Unittype unittype) {
		this.unittype = unittype;
	}

	public void addResult(String result) {
		if (this.result == null)
			this.result = result;
		else
			this.result += ("\n" + result);
	}

	public Boolean getExpectError() {
		return expectError;
	}

	public void setExpectError(Boolean expectError) {
		this.expectError = expectError;
	}

}
