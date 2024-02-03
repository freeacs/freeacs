package com.github.freeacs.web.app.page.report.uidata;

import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.report.Key;
import com.github.freeacs.dbi.report.RecordProvisioning;
import com.github.freeacs.dbi.report.Report;
import lombok.Getter;
import lombok.Setter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Setter
@Getter
public class RecordUIDataProv {
  private Unit unit;
  private String output;
  private long okCount;
  private long rescheduledCount;
  private long errorCount;
  private long missingCount;

  public static List<RecordUIDataProv> convertRecords(
      ACSUnit acsUnit, Map<String, Report<RecordProvisioning>> reportMap) throws SQLException {
    List<RecordUIDataProv> list = new ArrayList<>();

    for (Entry<String, Report<RecordProvisioning>> reportMapEntry : reportMap.entrySet()) {
      Unit unit = acsUnit.getUnitById(reportMapEntry.getKey());
      Map<Key, RecordProvisioning> recordMap =
          reportMapEntry.getValue().getMapAggregatedOn("Output");
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

}
