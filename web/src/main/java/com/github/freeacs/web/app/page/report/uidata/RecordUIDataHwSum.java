package com.github.freeacs.web.app.page.report.uidata;

import com.github.freeacs.dbi.Unit;
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.util.DateUtils;
import freemarker.template.TemplateModelException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This wrapper class will contain a list of wrapped RecordHardware instances.
 *
 * <p>Contains a static add method <code>addRecord(RecordUIDataHardware record)</code>.
 *
 * <p>Maintains a set of booleans for memory status and an integer for number of boots.
 *
 * @author Jarl Andre Hubenthal
 */
public class RecordUIDataHwSum {
  /** The unit. */
  @Getter
  private Unit unit;

  /** The boot counts. */
  private Long bootCount = 0L;

  @Setter
  @Getter
  private Long bootMisc = 0L;

  @Setter
  @Getter
  private Long bootPower = 0L;

  @Setter
  @Getter
  private Long bootProv = 0L;

  /** The heap ddr bad. */
  @Getter
  private double heapDdrMaxPercent;

  /** The heap ocm bad. */
  @Getter
  private double heapOcmMaxPercent;

  /** The row background style. */
  private String rowBackgroundStyle = "";

  /** The records. */
  @Getter
  private final List<RecordUIDataHardware> records = new ArrayList<>();

  /** Instantiates a new record ui data hw sum. */
  RecordUIDataHwSum() {}

  /**
   * Instantiates a new record ui data hw sum.
   *
   * @param unit the unit
   */
  public RecordUIDataHwSum(Unit unit) {
    this.unit = unit;
  }

  /**
   * Adds the record.
   *
   * @param record the record
   */
  public void addRecordIfRelevant(RecordUIDataHardware record) {
    if (record.isRecordRelevant()) {
      heapDdrMaxPercent = Math.max(record.getMemoryHeapDdrUsagePercent(), heapDdrMaxPercent);
      heapOcmMaxPercent = Math.max(record.getMemoryHeapOcmUsagePercent(), heapOcmMaxPercent);
      long bootTotal = record.getBootMisc() + record.getBootPower() + record.getBootProv();
      this.bootCount += bootTotal;
      this.bootMisc += record.getBootMisc();
      this.bootPower += record.getBootPower();
      this.bootProv += record.getBootProv();
      this.records.add(record);
    }
  }

  public boolean isMemoryRelevant() {
    for (RecordUIDataHardware rec : records) {
      if (rec.isMemoryRelevant()) {
        return true;
      }
    }
    return false;
  }

  public boolean isUptimeRelevant() {
    for (RecordUIDataHardware rec : records) {
      if (rec.isUptimeRelevant()) {
        return true;
      }
    }
    return false;
  }

  public boolean isBootRelevant() {
    for (RecordUIDataHardware rec : records) {
      if (rec.isBootsRelevant()) {
        return true;
      }
    }
    return false;
  }

  public Long getCpeUptimeAvg() {
    long millis = 0L;
    RecordUIDataHardware record = null;
    for (RecordUIDataHardware rec : records) {
      if (rec.getTms().getTime() > millis
          && rec.getCpeUptimeAvg().get() != null
          && rec.getCpeUptimeAvg().get() > 0) {
        millis = rec.getTms().getTime();
        record = rec;
      }
    }
    return record != null
        ? record.getCpeUptimeAvg().get() / record.getCpeUptimeAvg().getDividend()
        : null;
  }

  public String getCpeUptimeAvgReadable() {
    long up = getCpeUptimeAvg();
    return DateUtils.getUpTime(up);
  }

  /**
   * Gets the boots.
   *
   * @return the boots
   */
  public Long getBoots() {
    return bootCount;
  }

    /**
   * Gets the boot count as long.
   *
   * @return the boot count as long
   */
  public Long getBootCountAsLong() {
    return bootCount;
  }

  /**
   * Gets the row background style.
   *
   * @return the row background style
   */
  public String getRowBackgroundStyle() {
    try {
      String score = "100";
      if (getBoots() > 0) {
        score = "70";
      }
      rowBackgroundStyle =
          new AbstractWebPage.RowBackgroundColorMethod().exec(Arrays.asList(score));
    } catch (TemplateModelException e) {
      rowBackgroundStyle = "";
    }
    return rowBackgroundStyle;
  }

  public String getTableOfMemoryUnused() {
    return RecordUIDataHardware.getTableOfMemoryUnused(records);
  }
}
