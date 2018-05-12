package com.github.freeacs.dbi.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;


@SuppressWarnings({ "unchecked", "rawtypes" })
public class Report<R extends Record> {
	private static Logger logger = LoggerFactory.getLogger(Report.class);
	private Map<Key, R> map = new TreeMap<Key, R>();
	private Class<R> recordClass;
	private PeriodType periodType;

	public Report(Class<R> recordClass, PeriodType periodType) {
		this.recordClass = recordClass;
		this.periodType = periodType;
	}

	public R getRecord(Key key) {
		return map.get(key);
	}

	public void setRecord(Key key, R record) {
		map.put(key, record);
	}

	public Class<R> getRecordClass() {
		return recordClass;
	}

	public Map<Key, R> getMap() {
		return map;
	}

	public String toString() {
		String s = "Contains  " + map.size() + " records:\n";
		for (Record r : map.values()) {
			s += "\t" + r.toString() + "\n";
		}
		return s;
	}

	public Map<Key, R> getMapAggregatedOn(String... keyNames) {
		String logMsg = "Will aggregate report with " + map.size() + " records on keyNames: ";
		for (String keyName : keyNames) {
			logMsg += keyName + ", ";
		}
		logMsg = logMsg.substring(0, logMsg.length() - 2);
		
		logger.info(logMsg);
		Map<Key, R> aggregatedMap = new TreeMap<Key, R>();
		for (Entry<Key, R> entry : map.entrySet()) {
			Key key = entry.getKey();
			R record = entry.getValue();
			Key transformedKey = key.transform(keyNames);
			R aggregatedRecord = aggregatedMap.get(transformedKey);
			if (aggregatedRecord == null) {
				aggregatedRecord = (R) record.clone();
				aggregatedMap.put(transformedKey, aggregatedRecord);
			} else {
				aggregatedRecord.add(record);
			}
		}
		logger.info("Have aggregated report into " + aggregatedMap.size() + " records");
		return aggregatedMap;
	}

	public PeriodType getPeriodType() {
		return periodType;
	}

	public KeyFactory getKeyFactory() {
		try {
			Record record = (Record) recordClass.newInstance();
			return record.getKeyFactory();

		} catch (Throwable t) {
			throw new RuntimeException("The Record class " + recordClass + " did not implement getKeyFactory()");
		}
	}

}
