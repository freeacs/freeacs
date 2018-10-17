package com.github.freeacs.web.app.page.report.uidata;

import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.report.RecordGroup;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RecordUIDataGroupFromReport {
  private final RecordGroup record;

  public RecordGroup getRecord() {
    return record;
  }

  private final Unit unit;

  public Unit getUnit() {
    return unit;
  }

  RecordUIDataGroupFromReport() {
    unit = null;
    record = null;
  }

  RecordUIDataGroupFromReport(Unit unit, RecordGroup record) {
    this.unit = unit;
    this.record = record;
  }

  public static Collection<? extends RecordUIDataGroupFromReport> convertRecords(
      Unit unit, Collection<RecordGroup> values) {
    List<RecordUIDataGroupFromReport> list = new ArrayList<>();
    for (RecordGroup record : values) {
      list.add(new RecordUIDataGroupFromReport(unit, record));
    }
    return list;
  }
}
