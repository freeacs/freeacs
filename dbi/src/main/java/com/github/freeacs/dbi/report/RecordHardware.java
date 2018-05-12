package com.github.freeacs.dbi.report;

import java.util.Date;

public class RecordHardware extends Record<RecordHardware> {

	public static KeyFactory keyFactory = new KeyFactory("Unittype", "Profile", "SoftwareVersion");
	private Key key;

	private Date tms;
	private PeriodType periodType;
	private String unittypeName;
	private String profileName;
	private String softwareVersion;

//	private Counter unitCount = new Counter();
	private Counter bootCount = new Counter();
	private Counter bootWatchdogCount = new Counter();
	private Counter bootMiscCount = new Counter();
	private Counter bootPowerCount = new Counter();
	private Counter bootResetCount = new Counter();
	private Counter bootProvCount = new Counter();
	private Counter bootProvSwCount = new Counter();
	private Counter bootProvConfCount = new Counter();
	private Counter bootProvBootCount = new Counter();
	private Counter bootUserCount = new Counter();
	private Average memoryHeapDdrPoolAvg = new Average(1024);
	private Average memoryHeapDdrCurrentAvg = new Average(1024);
	private Average memoryHeapDdrLowAvg = new Average(1024);
	private Average memoryHeapOcmPoolAvg = new Average(1024);
	private Average memoryHeapOcmCurrentAvg = new Average(1024);
	private Average memoryHeapOcmLowAvg = new Average(1024);
	private Average memoryNpDdrPoolAvg = new Average(1);
	private Average memoryNpDdrCurrentAvg = new Average(1);
	private Average memoryNpDdrLowAvg = new Average(1);
	private Average memoryNpOcmPoolAvg = new Average(1);
	private Average memoryNpOcmCurrentAvg = new Average(1);
	private Average memoryNpOcmLowAvg = new Average(1);
	private Average cpeUptimeAvg = new Average(1);

	protected RecordHardware() {
	}

	public RecordHardware(Date tms, PeriodType periodType, String unittypeName, String profileName, String softwareVersion) {
		this.tms = tms;
		this.periodType = periodType;
		this.unittypeName = unittypeName;
		this.profileName = profileName;
		if (softwareVersion == null)
			softwareVersion = "Unknown";
		this.softwareVersion = softwareVersion;
		this.key = keyFactory.makeKey(tms, periodType, unittypeName, profileName, softwareVersion);
	}

	public Key getKey() {
		return key;
	}

	public Date getTms() {
		return tms;
	}

	public PeriodType getPeriodType() {
		return periodType;
	}

	public String getUnittypeName() {
		return unittypeName;
	}

	public String getProfileName() {
		return profileName;
	}

	public RecordHardware clone() {
		RecordHardware clone = new RecordHardware(tms, periodType, unittypeName, profileName, softwareVersion);
		clone.setBootCount(this.getBootCount().clone());
		clone.setBootMiscCount(this.getBootMiscCount().clone());
		clone.setBootPowerCount(this.getBootPowerCount().clone());
		clone.setBootProvBootCount(this.getBootProvBootCount().clone());
		clone.setBootProvConfCount(this.getBootProvConfCount().clone());
		clone.setBootProvCount(this.getBootProvCount().clone());
		clone.setBootProvSwCount(this.getBootProvSwCount().clone());
		clone.setBootUserCount(this.getBootUserCount().clone());
		clone.setBootWatchdogCount(this.getBootWatchdogCount().clone());
		clone.setMemoryHeapDdrPoolAvg(this.getMemoryHeapDdrPoolAvg().clone());
		clone.setMemoryHeapDdrCurrentAvg(this.getMemoryHeapDdrCurrentAvg().clone());
		clone.setMemoryHeapDdrLowAvg(this.getMemoryHeapDdrLowAvg().clone());
		clone.setMemoryHeapOcmPoolAvg(this.getMemoryHeapOcmPoolAvg().clone());
		clone.setMemoryHeapOcmCurrentAvg(this.getMemoryHeapOcmCurrentAvg().clone());
		clone.setMemoryHeapOcmLowAvg(this.getMemoryHeapOcmLowAvg().clone());
		clone.setMemoryNpDdrPoolAvg(this.getMemoryNpDdrPoolAvg().clone());
		clone.setMemoryNpDdrCurrentAvg(this.getMemoryNpDdrCurrentAvg().clone());
		clone.setMemoryNpDdrLowAvg(this.getMemoryNpDdrLowAvg().clone());
		clone.setMemoryNpOcmPoolAvg(this.getMemoryNpOcmPoolAvg().clone());
		clone.setMemoryNpOcmCurrentAvg(this.getMemoryNpOcmCurrentAvg().clone());
		clone.setMemoryNpOcmLowAvg(this.getMemoryNpOcmLowAvg().clone());
		clone.setCpeUptimeAvg(this.getCpeUptimeAvg().clone());
		return clone;
	}

	public void add(RecordHardware record) {
		this.getBootCount().add(record.getBootCount());
		this.getBootMiscCount().add(record.getBootMiscCount());
		this.getBootPowerCount().add(record.getBootPowerCount());
		this.getBootProvBootCount().add(record.getBootProvBootCount());
		this.getBootProvConfCount().add(record.getBootProvConfCount());
		this.getBootProvCount().add(record.getBootProvCount());
		this.getBootProvSwCount().add(record.getBootProvSwCount());
		this.getBootResetCount().add(record.getBootResetCount());
		this.getBootUserCount().add(record.getBootUserCount());
		this.getBootWatchdogCount().add(record.getBootWatchdogCount());
		this.getMemoryHeapDdrPoolAvg().add(record.getMemoryHeapDdrPoolAvg());
		this.getMemoryHeapDdrCurrentAvg().add(record.getMemoryHeapDdrCurrentAvg());
		this.getMemoryHeapDdrLowAvg().add(record.getMemoryHeapDdrLowAvg());
		this.getMemoryHeapOcmPoolAvg().add(record.getMemoryHeapOcmPoolAvg());
		this.getMemoryHeapOcmCurrentAvg().add(record.getMemoryHeapOcmCurrentAvg());
		this.getMemoryHeapOcmLowAvg().add(record.getMemoryHeapOcmLowAvg());
		this.getMemoryNpDdrPoolAvg().add(record.getMemoryNpDdrPoolAvg());
		this.getMemoryNpDdrCurrentAvg().add(record.getMemoryNpDdrCurrentAvg());
		this.getMemoryNpDdrLowAvg().add(record.getMemoryNpDdrLowAvg());
		this.getMemoryNpOcmPoolAvg().add(record.getMemoryNpOcmPoolAvg());
		this.getMemoryNpOcmCurrentAvg().add(record.getMemoryNpOcmCurrentAvg());
		this.getMemoryNpOcmLowAvg().add(record.getMemoryNpOcmLowAvg());
		this.getCpeUptimeAvg().add(record.getCpeUptimeAvg());
	}

	public KeyFactory getKeyFactory() {
		return keyFactory;
	}

	public Counter getBootCount() {
		return bootCount;
	}

	public void setBootCount(Counter bootCount) {
		this.bootCount = bootCount;
	}

	public Counter getBootWatchdogCount() {
		return bootWatchdogCount;
	}

	public void setBootWatchdogCount(Counter bootWatchdogCount) {
		this.bootWatchdogCount = bootWatchdogCount;
	}

	public Counter getBootMiscCount() {
		return bootMiscCount;
	}

	public void setBootMiscCount(Counter bootMiscCount) {
		this.bootMiscCount = bootMiscCount;
	}

	public Counter getBootPowerCount() {
		return bootPowerCount;
	}

	public void setBootPowerCount(Counter bootPowerCount) {
		this.bootPowerCount = bootPowerCount;
	}

	public Counter getBootResetCount() {
		return bootResetCount;
	}

	public void setBootResetCount(Counter bootResetCount) {
		this.bootResetCount = bootResetCount;
	}

	public Counter getBootProvCount() {
		return bootProvCount;
	}

	public void setBootProvCount(Counter bootProvCount) {
		this.bootProvCount = bootProvCount;
	}

	public Counter getBootProvSwCount() {
		return bootProvSwCount;
	}

	public void setBootProvSwCount(Counter bootProvSwCount) {
		this.bootProvSwCount = bootProvSwCount;
	}

	public Counter getBootProvConfCount() {
		return bootProvConfCount;
	}

	public void setBootProvConfCount(Counter bootProvConfCount) {
		this.bootProvConfCount = bootProvConfCount;
	}

	public Counter getBootProvBootCount() {
		return bootProvBootCount;
	}

	public void setBootProvBootCount(Counter bootProvBootCount) {
		this.bootProvBootCount = bootProvBootCount;
	}

	public Counter getBootUserCount() {
		return bootUserCount;
	}

	public void setBootUserCount(Counter bootUserCount) {
		this.bootUserCount = bootUserCount;
	}

	public Average getMemoryHeapDdrPoolAvg() {
		return memoryHeapDdrPoolAvg;
	}

	public void setMemoryHeapDdrPoolAvg(Average memoryHeapDdrPoolAvg) {
		this.memoryHeapDdrPoolAvg = memoryHeapDdrPoolAvg;
	}

	public Average getMemoryHeapDdrCurrentAvg() {
		return memoryHeapDdrCurrentAvg;
	}

	public void setMemoryHeapDdrCurrentAvg(Average memoryHeapDdrCurrentAvg) {
		this.memoryHeapDdrCurrentAvg = memoryHeapDdrCurrentAvg;
	}

	public Average getMemoryHeapDdrLowAvg() {
		return memoryHeapDdrLowAvg;
	}

	public void setMemoryHeapDdrLowAvg(Average memoryHeapDdrLowAvg) {
		this.memoryHeapDdrLowAvg = memoryHeapDdrLowAvg;
	}

	public Average getMemoryHeapOcmPoolAvg() {
		return memoryHeapOcmPoolAvg;
	}

	public void setMemoryHeapOcmPoolAvg(Average memoryHeapOcmPoolAvg) {
		this.memoryHeapOcmPoolAvg = memoryHeapOcmPoolAvg;
	}

	public Average getMemoryHeapOcmCurrentAvg() {
		return memoryHeapOcmCurrentAvg;
	}

	public void setMemoryHeapOcmCurrentAvg(Average memoryHeapOcmCurrentAvg) {
		this.memoryHeapOcmCurrentAvg = memoryHeapOcmCurrentAvg;
	}

	public Average getMemoryHeapOcmLowAvg() {
		return memoryHeapOcmLowAvg;
	}

	public void setMemoryHeapOcmLowAvg(Average memoryHeapOcmLowAvg) {
		this.memoryHeapOcmLowAvg = memoryHeapOcmLowAvg;
	}

	public Average getMemoryNpDdrPoolAvg() {
		return memoryNpDdrPoolAvg;
	}

	public void setMemoryNpDdrPoolAvg(Average memoryNpDdrPoolAvg) {
		this.memoryNpDdrPoolAvg = memoryNpDdrPoolAvg;
	}

	public Average getMemoryNpDdrCurrentAvg() {
		return memoryNpDdrCurrentAvg;
	}

	public void setMemoryNpDdrCurrentAvg(Average memoryNpDdrCurrentAvg) {
		this.memoryNpDdrCurrentAvg = memoryNpDdrCurrentAvg;
	}

	public Average getMemoryNpDdrLowAvg() {
		return memoryNpDdrLowAvg;
	}

	public void setMemoryNpDdrLowAvg(Average memoryNpDdrLowAvg) {
		this.memoryNpDdrLowAvg = memoryNpDdrLowAvg;
	}

	public Average getMemoryNpOcmPoolAvg() {
		return memoryNpOcmPoolAvg;
	}

	public void setMemoryNpOcmPoolAvg(Average memoryNpOcmPoolAvg) {
		this.memoryNpOcmPoolAvg = memoryNpOcmPoolAvg;
	}

	public Average getMemoryNpOcmCurrentAvg() {
		return memoryNpOcmCurrentAvg;
	}

	public void setMemoryNpOcmCurrentAvg(Average memoryNpOcmCurrentAvg) {
		this.memoryNpOcmCurrentAvg = memoryNpOcmCurrentAvg;
	}

	public Average getMemoryNpOcmLowAvg() {
		return memoryNpOcmLowAvg;
	}

	public void setMemoryNpOcmLowAvg(Average memoryNpOcmLowAvg) {
		this.memoryNpOcmLowAvg = memoryNpOcmLowAvg;
	}

	public String getSoftwareVersion() {
		return softwareVersion;
	}

	public Average getCpeUptimeAvg() {
		return cpeUptimeAvg;
	}

	public void setCpeUptimeAvg(Average uptime) {
		this.cpeUptimeAvg = uptime;
	}

}
