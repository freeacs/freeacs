package com.github.freeacs.dbi.report;

import java.util.Map.Entry;

public class ReportConverter {
  public static Report<RecordSyslog> convertSyslogReport(
      Report<RecordSyslog> r, PeriodType periodType) {
    Report<RecordSyslog> report = new Report<RecordSyslog>(RecordSyslog.class, periodType);
    for (Entry<Key, RecordSyslog> entry : r.getMap().entrySet()) {
      Key key = entry.getKey();
      if (key.getPeriodType().isLongerThan(periodType)) {
        throw new IllegalArgumentException(
            "Cannot convert a voip record to another voip record because its periodType is longer");
      }
      RecordSyslog record = entry.getValue();
      RecordSyslog tmp =
          new RecordSyslog(
              key.getTms(),
              periodType,
              record.getUnittypeName(),
              record.getProfileName(),
              record.getSeverity(),
              record.getEventId(),
              record.getFacility());
      Key newKey = tmp.getKey();
      RecordSyslog recordFromReport = report.getRecord(newKey);
      if (recordFromReport == null) {
        recordFromReport = tmp;
        report.setRecord(newKey, recordFromReport);
      }
      recordFromReport.add(record);
    }
    return report;
  }

  public static Report<RecordProvisioning> convertProvReport(
      Report<RecordProvisioning> r, PeriodType periodType) {
    Report<RecordProvisioning> report =
        new Report<RecordProvisioning>(RecordProvisioning.class, periodType);
    for (Entry<Key, RecordProvisioning> entry : r.getMap().entrySet()) {
      Key key = entry.getKey();
      if (key.getPeriodType().isLongerThan(periodType)) {
        throw new IllegalArgumentException(
            "Cannot convert a voip record to another voip record because its periodType is longer");
      }
      RecordProvisioning record = entry.getValue();
      RecordProvisioning tmp =
          new RecordProvisioning(
              key.getTms(),
              periodType,
              record.getUnittypeName(),
              record.getProfileName(),
              record.getSoftwareVersion(),
              record.getOutput());
      Key newKey = tmp.getKey();
      RecordProvisioning recordFromReport = report.getRecord(newKey);
      if (recordFromReport == null) {
        recordFromReport = tmp;
        report.setRecord(newKey, recordFromReport);
      }
      recordFromReport.add(record);
    }
    return report;
  }

  public static Report<RecordVoip> convertVoipReport(Report<RecordVoip> r, PeriodType periodType) {
    Report<RecordVoip> report = new Report<RecordVoip>(RecordVoip.class, periodType);
    for (Entry<Key, RecordVoip> entry : r.getMap().entrySet()) {
      Key key = entry.getKey();
      if (key.getPeriodType().isLongerThan(periodType)) {
        throw new IllegalArgumentException(
            "Cannot convert a voip record to another voip record because its periodType is longer");
      }
      RecordVoip record = entry.getValue();
      RecordVoip tmp =
          new RecordVoip(
              key.getTms(),
              periodType,
              record.getUnittypeName(),
              record.getProfileName(),
              record.getSoftwareVersion(),
              record.getLine());
      Key newKey = tmp.getKey();
      RecordVoip recordFromReport = report.getRecord(newKey);
      if (recordFromReport == null) {
        recordFromReport = tmp;
        report.setRecord(newKey, recordFromReport);
      }
      recordFromReport.add(record);
    }
    return report;
  }

  public static Report<RecordHardware> convertHardwareReport(
      Report<RecordHardware> r, PeriodType periodType) {
    Report<RecordHardware> report = new Report<RecordHardware>(RecordHardware.class, periodType);
    for (Entry<Key, RecordHardware> entry : r.getMap().entrySet()) {
      Key key = entry.getKey();
      if (key.getPeriodType().isLongerThan(periodType)) {
        throw new IllegalArgumentException(
            "Cannot convert periodType of the record since the origianl periodType is longer");
      }
      RecordHardware record = entry.getValue();
      RecordHardware tmp =
          new RecordHardware(
              key.getTms(),
              periodType,
              record.getUnittypeName(),
              record.getProfileName(),
              record.getSoftwareVersion());
      Key newKey = tmp.getKey();
      RecordHardware recordFromReport = report.getRecord(newKey);
      if (recordFromReport == null) {
        recordFromReport = tmp;
        report.setRecord(newKey, recordFromReport);
      }
      recordFromReport.add(record);
    }
    return report;
  }
}
