package com.github.freeacs.web.app.page.report.uidata;

import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.report.RecordHardware;
import com.github.freeacs.web.app.page.AbstractWebPage;
import freemarker.template.TemplateModelException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
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
  @Setter
  @Getter
  private Long bootProv;

  /** The boot misc. */
  @Setter
  @Getter
  private Long bootMisc;

  @Setter
  @Getter
  private Long bootPower;

  /** The memory heap ddr current avg. */
  private Long memoryHeapDdrCurrentAvg;

  /** The memory heap ddr pool avg. */
  private Long memoryHeapDdrPoolAvg;

  /** The memory heap ddr percent. */
  @Getter
  private final double memoryHeapDdrUsagePercent;

  /** The memory heap ocm current avg. */
  private Long memoryHeapOcmCurrentAvg;

    /** The memory heap ocm pool avg. */
  private Long memoryHeapOcmPoolAvg;

  /** The memory heap ocm percent. */
  @Getter
  private final double memoryHeapOcmUsagePercent;

  /** The row background style. */
  @Getter
  private String rowBackgroundStyle = "";

  /** The unit. */
  @Getter
  private final Unit unit;

  @Getter
  private final RecordUIDataHardwareFilter limits;

  @Getter
  private final long bootTotal;

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
    if (getMemoryHeapDdrPoolAvg().get() != null) {
      memoryHeapDdrPoolAvg =
          getMemoryHeapDdrPoolAvg().get() / getMemoryHeapDdrPoolAvg().getDividend();
    }
    if (getMemoryHeapOcmCurrentAvg().get() != null) {
      memoryHeapOcmCurrentAvg =
          getMemoryHeapOcmCurrentAvg().get() / getMemoryHeapOcmCurrentAvg().getDividend();
    }
    if (getMemoryHeapOcmPoolAvg().get() != null) {
      memoryHeapOcmPoolAvg =
          getMemoryHeapOcmPoolAvg().get() / getMemoryHeapOcmPoolAvg().getDividend();
    }
    memoryHeapDdrUsagePercent = getPercentageUsed(memoryHeapDdrCurrentAvg, memoryHeapDdrPoolAvg);
    memoryHeapOcmUsagePercent = getPercentageUsed(memoryHeapOcmCurrentAvg, memoryHeapOcmPoolAvg);
    try {
      String score = "100";
      if (getBootMessage() != null) {
        score = "70";
      }
      rowBackgroundStyle =
          new AbstractWebPage.RowBackgroundColorMethod().exec(List.of(score));
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

  /**
   * Gets the table of memory unused.
   *
   * @return the table of memory unused
   */
  public static String getTableOfMemoryUnused(List<RecordUIDataHardware> records) {
    StringBuilder html = new StringBuilder("<table>");
    for (RecordUIDataHardware record : records) {
      html.append("<tr><td border=1>")
              .append(record.getTmsAsString())
              .append("<br />Memory used:<br />Heap(Ocm: ")
              .append(record.getMemoryHeapOcmUsagePercentAsString())
              .append("%, Ddr: ")
              .append(record.getMemoryHeapDdrUsagePercentAsString())
              .append("%)</td></tr>");
    }
    html.append("</table>");
    return html.toString();
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
    if (!string.isEmpty()) {
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
   * Gets the memory heap ddr percent as string.
   *
   * @return the memory heap ddr percent as string
   */
  public String getMemoryHeapDdrUsagePercentAsString() {
    return formatNumberWithTwoDesimals(memoryHeapDdrUsagePercent);
  }

    /**
   * Gets the memory heap ocm percent as string.
   *
   * @return the memory heap ocm percent as string
   */
  public String getMemoryHeapOcmUsagePercentAsString() {
    return formatNumberWithTwoDesimals(memoryHeapOcmUsagePercent);
  }

}
