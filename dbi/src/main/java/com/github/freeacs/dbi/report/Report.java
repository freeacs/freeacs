package com.github.freeacs.dbi.report;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@SuppressWarnings({"unchecked", "rawtypes"})
public class Report<R extends Record> {
  private static final Logger logger = LoggerFactory.getLogger(Report.class);
  private final Map<Key, R> map = new TreeMap<>();
  private final Class<R> recordClass;
  private final PeriodType periodType;

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

  public String toString() {
    StringBuilder s = new StringBuilder("Contains  " + map.size() + " records:\n");
    for (Record r : map.values()) {
      s.append("\t").append(r).append("\n");
    }
    return s.toString();
  }

  public Map<Key, R> getMapAggregatedOn(String... keyNames) {
    StringBuilder logMsg = new StringBuilder("Will aggregate report with " + map.size() + " records on keyNames: ");
    for (String keyName : keyNames) {
      logMsg.append(keyName).append(", ");
    }
    logMsg = new StringBuilder(logMsg.substring(0, logMsg.length() - 2));

    logger.info(logMsg.toString());
    Map<Key, R> aggregatedMap = new TreeMap<>();
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

  public KeyFactory getKeyFactory() {
    try {
      Record record = recordClass.getDeclaredConstructor().newInstance();
      return record.getKeyFactory();
    } catch (Throwable t) {
      throw new RuntimeException(
          "The Record class " + recordClass + " did not implement getKeyFactory()");
    }
  }
}
