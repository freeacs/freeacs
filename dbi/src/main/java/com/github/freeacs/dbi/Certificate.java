package com.github.freeacs.dbi;

import com.github.freeacs.dbi.crypto.Crypto;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Certificate {

	public static String CERT_TYPE_PROVISIONING = "Provisioning";
	public static String CERT_TYPE_REPORT = "Report";
	public static String TRIAL_TYPE_DAYS = "Days";
	public static String TRIAL_TYPE_COUNT = "Count";

	public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	private static Pattern datePattern = Pattern.compile("until (\\d{4}-\\d{2}-\\d{2})");
	private static Pattern countPattern = Pattern.compile("maximum level (\\d+)");
	private static Pattern issuePattern = Pattern.compile("issued to ([^,]+)");

	private Integer id;
	private String name;
	private String certificate;
	private boolean trial;
	private boolean decrypted;
	@SuppressWarnings("unused")
	private String decryptedString;
	private String trialType;
	private String certType;
	private Integer maxCount;
	private Date dateLimit;
	private String oldName;
	private boolean processed;
	private String issuedTo;

	public Certificate(String name, String certificate) {
		setName(name);
		setCertificate(certificate);
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getCertificate() {
		return certificate;
	}

	public String getCertificateDecrypted() {
		return Crypto.decryptUsingRSAPublicKey(certificate);
	}

	public void process() {
		try {
			String dec = getCertificateDecrypted();
			decryptedString = dec;
			if (dec == null || dec.indexOf("not possible to decrypt") > -1) {
				decrypted = false;
				trial = false;
				trialType = null;
				certType = null;
			} else
				decrypted = true;
			if (dec.indexOf(CERT_TYPE_PROVISIONING) > -1)
				certType = CERT_TYPE_PROVISIONING;
			if (dec.indexOf(CERT_TYPE_REPORT) > -1)
				certType = CERT_TYPE_REPORT;
			Matcher m = datePattern.matcher(dec);
			if (m.find()) {
				trial = true;
				trialType = TRIAL_TYPE_DAYS;
				String dateStr = m.group(1);
				dateLimit = dateFormat.parse(dateStr);
			}
			m = countPattern.matcher(dec);
			if (m.find()) {
				trial = true;
				trialType = TRIAL_TYPE_COUNT;
				maxCount = new Integer(m.group(1));
			}
			m = issuePattern.matcher(dec);
			if (m.find()) {
				issuedTo = m.group(1).trim();
			}

		} catch (Throwable t) {
			decrypted = false;
		}
		processed = true;
	}

	@Override
	public String toString() {
		return "[" + id + "] [" + name + "] [" + certificate + "]";
	}

	protected void setId(Integer id) {
		this.id = id;
	}

	protected String getOldName() {
		return oldName;
	}

	public void setName(String name) {
		if (name == null || name.trim().equals(""))
			throw new IllegalArgumentException("Certificate name cannot be null or an empty string");
		if (!name.equals(this.name))
			this.oldName = this.name;
		this.name = name;
	}

	protected void setOldName(String oldName) {
		this.oldName = oldName;
	}

	public void setCertificate(String certificate) {
		if (certificate == null || certificate.trim().equals(""))
			throw new IllegalArgumentException("Certificate cannot be null or an empty string");
		if (certificate.length() != 256)
			throw new IllegalArgumentException("Certificate length is wrong, should be 256 (in hex format)");
		this.certificate = certificate;
	}

	public boolean isTrial() {
		if (!processed)
			process();
		return trial;
	}

	public String getTrialType() {
		if (!processed)
			process();
		return trialType;
	}

	public String getCertType() {
		if (!processed)
			process();
		return certType;
	}

	public boolean isValid(Integer count) {
		if (!processed)
			process();
		if (decrypted) {
			if (trial) {
				if (maxCount != null && count > maxCount)
					return false;
				if (dateLimit != null && System.currentTimeMillis() > dateLimit.getTime())
					return false;
				return true;
			} else
				return true;
		} else
			return false;
	}

	public boolean isProductionAndValid() {
		if (decrypted && !trial)
			return true;
		return false;
	}

	public boolean isProcessed() {
		return processed;
	}

	public Integer getMaxCount() {
		if (!processed)
			process();
		return maxCount;
	}

	public Date getDateLimit() {
		if (!processed)
			process();
		return dateLimit;
	}

	public boolean isDecrypted() {
		return decrypted;
	}

	public String getIssuedTo() {
		return issuedTo;
	}

}
