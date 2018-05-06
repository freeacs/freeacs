package com.owera.xaps.web.app.page.report.uidata;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.owera.common.db.NoAvailableConnectionException;
import com.owera.xaps.dbi.Unit;
import com.owera.xaps.dbi.XAPSUnit;
import com.owera.xaps.dbi.report.Key;
import com.owera.xaps.dbi.report.RecordProvisioning;
import com.owera.xaps.dbi.report.Report;

public class RecordUIDataProv {
	private Unit unit;
	private String output;
	private long okCount;
	private long rescheduledCount;
	private long errorCount;
	private long missingCount;

	public static List<RecordUIDataProv> convertRecords(XAPSUnit xapsUnit, Map<String, Report<RecordProvisioning>> reportMap) throws SQLException, NoAvailableConnectionException {
		List<RecordUIDataProv> list = new ArrayList<RecordUIDataProv>();

		for (Entry<String, Report<RecordProvisioning>> reportMapEntry : reportMap.entrySet()) {
			Unit unit = xapsUnit.getUnitById(reportMapEntry.getKey());
			Map<Key, RecordProvisioning> recordMap = reportMapEntry.getValue().getMapAggregatedOn("Output");
			for (Entry<Key, RecordProvisioning> recordMapEntry : recordMap.entrySet()) {
				Key key = recordMapEntry.getKey();
				RecordProvisioning record = recordMapEntry.getValue();
				RecordUIDataProv uiRecord = new RecordUIDataProv();
				uiRecord.setUnit(unit);
				uiRecord.setOutput(key.getKeyElement("Output").getValue());
				uiRecord.setOkCount(record.getProvisioningOkCount().get());
				uiRecord.setRescheduledCount(record.getProvisioningRescheduledCount().get());
				uiRecord.setErrorCount(record.getProvisioningErrorCount().get());
				uiRecord.setMissingCount(record.getProvisioningMissingCount().get());
				list.add(uiRecord);
			}
		}
		return list;
	}

	/**
	 * Instantiates a new record ui data hw sum.
	 */
	public RecordUIDataProv() {
	}

	public String getOutput() {
		return output;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public long getOkCount() {
		return okCount;
	}

	public void setOkCount(long okCount) {
		this.okCount = okCount;
	}

	public long getRescheduledCount() {
		return rescheduledCount;
	}

	public void setRescheduledCount(long rescheduledCount) {
		this.rescheduledCount = rescheduledCount;
	}

	public long getErrorCount() {
		return errorCount;
	}

	public void setErrorCount(long errorCount) {
		this.errorCount = errorCount;
	}

	public long getMissingCount() {
		return missingCount;
	}

	public void setMissingCount(long missingCount) {
		this.missingCount = missingCount;
	}

}
