package com.github.freeacs.web.app.page.report.uidata;

import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.report.RecordHardware;
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.util.DateUtils;
import freemarker.template.TemplateModelException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is a wrapper class for RecordHardware. Calculates percents and human friendly information.
 *
 * <p>Contains a static converter method <code>convertRecords(List<RecordHardware> records)</code>
 * for converting a larger set of RecordHardware objects.
 *
 * @author Jarl Andre Hubenthal
 */
public class RecordUIDataHardware extends RecordHardware {
  /** The boot count. */
  private Long bootProv;

  /** The boot misc. */
  private Long bootMisc;

  private Long bootPower;

  /** The memory heap ddr current avg. */
  private Long memoryHeapDdrCurrentAvg;

  /** The memory heap ddr lowest avg. */
  private Long memoryHeapDdrLowestAvg;

  /** The memory heap ddr pool avg. */
  private Long memoryHeapDdrPoolAvg;

  /** The memory heap ddr percent. */
  private double memoryHeapDdrUsagePercent;

  /** The memory heap ocm current avg. */
  private Long memoryHeapOcmCurrentAvg;

  /** The memory heap ocm lowest avg. */
  private Long memoryHeapOcmLowestAvg;

  /** The memory heap ocm pool avg. */
  private Long memoryHeapOcmPoolAvg;

  /** The memory heap ocm percent. */
  private double memoryHeapOcmUsagePercent;

  /** The up time avg. */
  private Long upTimeAvg;

  /** The row background style. */
  private String rowBackgroundStyle = "";

  /** The memory heap ddr lowest percent. */
  private double memoryHeapDdrHighestUsagePercent;

  /** The memory heap ocm lowest percent. */
  private double memoryHeapOcmHighestUsagePercent;

  /** The unit. */
  private Unit unit;

  private final RecordUIDataHardwareFilter limits;

  private long bootTotal;

  /**
   * Instantiates a new record ui data hardware.
   *
   * @param record the record
   */
  RecordUIDataHardware(RecordHardware record, Unit unit, RecordUIDataHardwareFilter limits) {
    super(
        record.getTms(),
        record.getPeriodType(),
        record.getUnittypeName(),
        record.getProfileName(),
        record.getSoftwareVersion());
    add(record);
    this.unit = unit;
    this.limits = limits;
    bootTotal = getBootCount().get() / getBootCount().getDividend();
    bootPower = getBootPowerCount().get() / getBootPowerCount().getDividend();
    bootMisc = getBootMiscCount().get() / getBootMiscCount().getDividend();
    bootProv = getBootProvCount().get() / getBootProvCount().getDividend();
    if (getMemoryHeapDdrCurrentAvg().get() != null) {
      memoryHeapDdrCurrentAvg =
          getMemoryHeapDdrCurrentAvg().get() / getMemoryHeapDdrCurrentAvg().getDividend();
    }
    if (getMemoryHeapDdrLowAvg().get() != null) {
      memoryHeapDdrLowestAvg =
          getMemoryHeapDdrLowAvg().get() / getMemoryHeapDdrLowAvg().getDividend();
    }
    if (getMemoryHeapDdrPoolAvg().get() != null) {
      memoryHeapDdrPoolAvg =
          getMemoryHeapDdrPoolAvg().get() / getMemoryHeapDdrPoolAvg().getDividend();
    }
    if (getMemoryHeapOcmCurrentAvg().get() != null) {
      memoryHeapOcmCurrentAvg =
          getMemoryHeapOcmCurrentAvg().get() / getMemoryHeapOcmCurrentAvg().getDividend();
    }
    if (getMemoryHeapOcmLowAvg().get() != null) {
      memoryHeapOcmLowestAvg =
          getMemoryHeapOcmLowAvg().get() / getMemoryHeapOcmLowAvg().getDividend();
    }
    if (getMemoryHeapOcmPoolAvg().get() != null) {
      memoryHeapOcmPoolAvg =
          getMemoryHeapOcmPoolAvg().get() / getMemoryHeapOcmPoolAvg().getDividend();
    }
    if (getCpeUptimeAvg().get() != null) {
      upTimeAvg = getCpeUptimeAvg().get() / getCpeUptimeAvg().getDividend();
    }
    memoryHeapDdrUsagePercent = getPercentageUsed(memoryHeapDdrCurrentAvg, memoryHeapDdrPoolAvg);
    memoryHeapOcmUsagePercent = getPercentageUsed(memoryHeapOcmCurrentAvg, memoryHeapOcmPoolAvg);
    try {
      String score = "100";
      if (getBootMessage() != null) {
        score = "70";
      }
      rowBackgroundStyle =
          new AbstractWebPage.RowBackgroundColorMethod().exec(Arrays.asList(score));
    } catch (TemplateModelException e) {
      rowBackgroundStyle = "";
    }
  }
  /** Avoid public use. */
  public boolean isRecordRelevant() {
    return limits.isRecordRelevant(this);
  }

  public boolean isUptimeRelevant() {
    return limits.isUptimeRelevant(this);
  }

  public boolean isMemoryRelevant() {
    return limits.isMemoryRelevant(this);
  }

  public boolean isBootsRelevant() {
    return limits.isBootsRelevant(this);
  }

  public String getTableOfMemoryUnused() {
    return getTableOfMemoryUnused(Arrays.asList(this));
  }

  /**
   * Gets the table of memory unused.
   *
   * @return the table of memory unused
   */
  public static String getTableOfMemoryUnused(List<RecordUIDataHardware> records) {
    String html = "<table>";
    for (RecordUIDataHardware record : records) {
      html +=
          "<tr><td border=1>"
              + record.getTmsAsString()
              + "<br />Memory used:<br />Heap(Ocm: "
              + record.getMemoryHeapOcmUsagePercentAsString()
              + "%, Ddr: "
              + record.getMemoryHeapDdrUsagePercentAsString()
              + "%)</td></tr>";
    }
    html += "</table>";
    return html;
  }

  /**
   * First calculate the percent of currently available memory Then subtract that value from 100 to
   * get the percentage of used memory.
   *
   * @param current the current
   * @param pool the pool
   * @return the percentage used
   */
  private double getPercentageUsed(Long current, Long pool) {
    double d = RecordUIDataMethods.getPercent(current, pool);
    if (d == 0) {
      return 0d;
    }
    return 100 - d;
  }

  /**
   * Convert records.
   *
   * @param records the records
   * @return the list
   */
  public static List<RecordUIDataHardware> convertRecords(
      Unit unit, List<RecordHardware> records, RecordUIDataHardwareFilter limits) {
    List<RecordUIDataHardware> list = new ArrayList<>();
    for (RecordHardware record : records) {
      list.add(new RecordUIDataHardware(record, unit, limits));
    }
    return list;
  }

  /**
   * Gets the row background style.
   *
   * @return the row background style
   */
  public String getRowBackgroundStyle() {
    return rowBackgroundStyle;
  }

  /**
   * Gets the tms as string.
   *
   * @return the tms as string
   */
  public String getTmsAsString() {
    return RecordUIDataConstants.DATE_FORMAT.format(getTms()).replace(" ", "&nbsp;");
  }

  /**
   * Gets the boot message.
   *
   * @return the boot message
   */
  public String getBootMessage() {
    StringBuilder string = new StringBuilder();
    if (super.getBootWatchdogCount().get() != null && super.getBootWatchdogCount().get() > 0) {
      string.append("Watchdog boot,");
    }
    if (super.getBootPowerCount().get() != null && super.getBootPowerCount().get() > 0) {
      string.append("Power boot,");
    }
    if (super.getBootMiscCount().get() != null && super.getBootMiscCount().get() > 0) {
      string.append("Miscellaneous boot,");
    }
    if (super.getBootProvBootCount().get() != null && super.getBootProvBootCount().get() > 0) {
      string.append("Provisioning boot,");
    }
    if (super.getBootProvConfCount().get() != null && super.getBootProvConfCount().get() > 0) {
      string.append("Provisioning config boot,");
    }
    if (super.getBootProvCount().get() != null && super.getBootProvCount().get() > 0) {
      string.append("Provisioning boot,");
    }
    if (super.getBootProvSwCount().get() != null && super.getBootProvSwCount().get() > 0) {
      string.append("Provisioning software boot,");
    }
    if (super.getBootResetCount().get() != null && super.getBootResetCount().get() > 0) {
      string.append("Reset boot,");
    }
    if (super.getBootUserCount().get() != null && super.getBootUserCount().get() > 0) {
      string.append("User boot");
    }
    if (string.length() > 0) {
      String s = string.toString();
      if (s.endsWith(",")) {
        s = s.substring(0, s.length() - 1);
      }
      return s;
    }
    return null;
  }

  /**
   * Format number with two desimals.
   *
   * @param toFormat the to format
   * @return the string
   */
  private String formatNumberWithTwoDesimals(Double toFormat) {
    return RecordUIDataConstants.TWO_DECIMALS_FORMAT.format(toFormat);
  }

  /**
   * Gets the memory heap ddr current avg as string.
   *
   * @return the memory heap ddr current avg as string
   */
  public String getMemoryHeapDdrCurrentAvgAsString() {
    return RecordUIDataMethods.getMegaBytePresentation(memoryHeapDdrCurrentAvg);
  }

  /**
   * Gets the memory heap ocm current avg as string.
   *
   * @return the memory heap ocm current avg as string
   */
  public String getMemoryHeapOcmCurrentAvgAsString() {
    return RecordUIDataMethods.getMegaBytePresentation(memoryHeapOcmCurrentAvg);
  }

  /**
   * Gets the memory heap ddr lowest avg as string.
   *
   * @return the memory heap ddr lowest avg as string
   */
  public String getMemoryHeapDdrLowestAvgAsString() {
    return RecordUIDataMethods.getMegaBytePresentation(memoryHeapDdrLowestAvg);
  }

  /**
   * Gets the memory heap ocm lowest avg as string.
   *
   * @return the memory heap ocm lowest avg as string
   */
  public String getMemoryHeapOcmLowestAvgAsString() {
    return RecordUIDataMethods.getMegaBytePresentation(memoryHeapOcmLowestAvg);
  }

  /**
   * Gets the up time avg as string.
   *
   * @return the up time avg as string
   */
  public String getUpTimeAvgAsString() {
    String upTimeString = RecordUIDataMethods.getToStringOrNonBreakingSpace(upTimeAvg);
    return RecordUIDataMethods.appendStringIfNotNonBreaking(upTimeString, "m");
  }

  /**
   * Gets the memory heap ddr percent.
   *
   * @return the memory heap ddr percent
   */
  public double getMemoryHeapDdrUsagePercent() {
    return memoryHeapDdrUsagePercent;
  }

  /**
   * Gets the memory heap ddr percent as string.
   *
   * @return the memory heap ddr percent as string
   */
  public String getMemoryHeapDdrUsagePercentAsString() {
    return formatNumberWithTwoDesimals(memoryHeapDdrUsagePercent);
  }

  /**
   * Gets the memory heap ocm percent.
   *
   * @return the memory heap ocm percent
   */
  public double getMemoryHeapOcmUsagePercent() {
    return memoryHeapOcmUsagePercent;
  }

  /**
   * Gets the memory heap ocm percent as string.
   *
   * @return the memory heap ocm percent as string
   */
  public String getMemoryHeapOcmUsagePercentAsString() {
    return formatNumberWithTwoDesimals(memoryHeapOcmUsagePercent);
  }

  /**
   * Gets the memory heap ddr pool avg as long.
   *
   * @return the memory heap ddr pool avg as long
   */
  public Long getMemoryHeapDdrPoolAvgAsLong() {
    return memoryHeapDdrPoolAvg;
  }

  /**
   * Gets the memory heap ddr pool avg as string.
   *
   * @return the memory heap ddr pool avg as string
   */
  public String getMemoryHeapDdrPoolAvgAsString() {
    return RecordUIDataMethods.getMegaBytePresentation(memoryHeapDdrPoolAvg);
  }

  /**
   * Gets the memory heap ocm pool avg as long.
   *
   * @return the memory heap ocm pool avg as long
   */
  public Long getMemoryHeapOcmPoolAvgAsLong() {
    return memoryHeapOcmPoolAvg;
  }

  /**
   * Gets the memory heap ocm pool avg as string.
   *
   * @return the memory heap ocm pool avg as string
   */
  public String getMemoryHeapOcmPoolAvgAsString() {
    return RecordUIDataMethods.getMegaBytePresentation(memoryHeapOcmPoolAvg);
  }

  /**
   * Gets the memory heap ddr lowest percent.
   *
   * @return the memory heap ddr lowest percent
   */
  public double getMemoryHeapDdrHighestUsagePercent() {
    return memoryHeapDdrHighestUsagePercent;
  }

  /**
   * Gets the memory heap ocm lowest percent.
   *
   * @return the memory heap ocm lowest percent
   */
  public double getMemoryHeapOcmHighestUsagePercent() {
    return memoryHeapOcmHighestUsagePercent;
  }

  public Long getBootProv() {
    return bootProv;
  }

  public void setBootProv(Long bootProv) {
    this.bootProv = bootProv;
  }

  public Long getBootMisc() {
    return bootMisc;
  }

  public void setBootMisc(Long bootMisc) {
    this.bootMisc = bootMisc;
  }

  public Long getBootPower() {
    return bootPower;
  }

  public void setBootPower(Long bootPower) {
    this.bootPower = bootPower;
  }

  /**
   * Gets the unit.
   *
   * @return the unit
   */
  public Unit getUnit() {
    return unit;
  }

  public RecordUIDataHardwareFilter getLimits() {
    return limits;
  }

  public long getBootTotal() {
    return bootTotal;
  }

  public String getUpTimeAvgAsReadable() {
    return DateUtils.getUpTime(upTimeAvg);
  }
}
