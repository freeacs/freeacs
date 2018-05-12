package com.github.freeacs.dbi.crypto;

import com.github.freeacs.dbi.Certificate;

public class CertificateDetails {
	private String type;
	private String limitType;
	private Integer limit;
	private String customerName;

	public CertificateDetails(String type, String limitType, Integer limit, String customerName) {
		setType(type);
		setLimitType(limitType);
		setLimit(limit);
		setCustomerName(customerName);
	}

	public String toString() {
		return "\nType: " + type + "\nLimit: " + limit + "\nCustomer: " + customerName;
	}

	public String getType() {
		return type;
	}

	private void setType(String type) {
		if (type != null && (type.equals(Certificate.CERT_TYPE_PROVISIONING) || type.equals(Certificate.CERT_TYPE_REPORT)))
			this.type = type;
		else
			throw new IllegalArgumentException("Certificate type must be " + Certificate.CERT_TYPE_PROVISIONING + " or " + Certificate.CERT_TYPE_REPORT);
	}

	public String getLimitType() {
		return limitType;
	}

	private void setLimitType(String limitType) {
		if (limitType != null && (limitType.equals(Certificate.TRIAL_TYPE_COUNT) || limitType.equals(Certificate.TRIAL_TYPE_DAYS)))
			this.limitType = limitType;
		else if (limitType != null)
			throw new IllegalArgumentException("Certificate limit type must be " + Certificate.TRIAL_TYPE_DAYS + " or " + Certificate.TRIAL_TYPE_COUNT);
	}

	public Integer getLimit() {
		return limit;
	}

	private void setLimit(Integer limit) {
		this.limit = limit;
	}

	public String getCustomerName() {
		return customerName;
	}

	private void setCustomerName(String customerName) {
		if (customerName != null && customerName.trim().length() <= 1)
			throw new IllegalArgumentException("Customer name must be minimum 2 characters long");
		this.customerName = customerName;
	}
}
