package com.github.freeacs.dbi.tr069;

import com.github.freeacs.dbi.UnittypeParameter;

public class TestCaseParameter {

	public enum TestCaseParameterType {
		GET, SET, FAC;
	}

	private Integer id;
	private TestCaseParameterType type;
	private UnittypeParameter utp;
	private TR069DMParameter dmp;
	private String value;
	private int notification;

	public TestCaseParameter(TestCaseParameterType type, UnittypeParameter utp, TR069DMParameter dmp, String value, int notification) {
		super();
		this.type = type;
		this.utp = utp;
		this.dmp = dmp;
		this.value = value;
		this.notification = notification;
	}

	public UnittypeParameter getUnittypeParameter() {
		return utp;
	}

	public void setUnittypeParameter(UnittypeParameter utp) {
		this.utp = utp;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getNotification() {
		return notification;
	}

	public void setNotification(int notification) {
		this.notification = notification;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public boolean equals(Object o) {
		if (!(o instanceof TestCaseParameter))
			return false;
		TestCaseParameter tcp = (TestCaseParameter) o;
		if (tcp.getId() != null && this.getId() != null && tcp.getId().intValue() == this.getId())
			return true;

		if (tcp.getType() != this.getType())
			return false;
		if (tcp.getNotification() != this.getNotification())
			return false;
		if (tcp.getUnittypeParameter().getId() != this.getUnittypeParameter().getId().intValue())
			return false;
		if (tcp.getValue() == null && this.getValue() != null || tcp.getValue() != null && this.getValue() == null)
			return false;
		if (tcp.getValue() != null && !tcp.getValue().equals(this.getValue()))
			return false;
		return true;
	}

	public TestCaseParameterType getType() {
		return type;
	}

	public void setType(TestCaseParameterType type) {
		this.type = type;
	}

	public String toString() {
		return String.format("%-5s" + utp.getName(), type.toString());
	}

	public TR069DMParameter getDataModelParameter() {
		return dmp;
	}

	public void setDataModelParameter(TR069DMParameter dmp) {
		this.dmp = dmp;
	}

}
