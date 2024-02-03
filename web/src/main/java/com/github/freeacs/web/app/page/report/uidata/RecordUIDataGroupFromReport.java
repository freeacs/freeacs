package com.github.freeacs.web.app.page.report.uidata;

import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.report.RecordGroup;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class RecordUIDataGroupFromReport {
  private final RecordGroup record;

  private final Unit unit;

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
