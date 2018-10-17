/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.freeacs.web.app.page.report.uidata;

import com.github.freeacs.web.app.page.report.UnitListData;
import java.util.Map;

/**
 * This class acts a wrapper for all the filter logic used on the hardware unit list page.
 *
 * <p>It makes sure to populate the filters to the template map.
 *
 * @author Jarl Andre Hubenthal
 */
public class RecordUIDataHardwareFilter {
  public final Integer ddr_low;
  public static final Integer ddr_low_default = 90;

  public final Integer ddr_high;
  public static final Integer ddr_high_default = 100;

  public final Integer ocm_low;
  public static final Integer ocm_low_default = 90;

  public final Integer ocm_high;
  public static final Integer ocm_high_default = 100;

  public final Integer uptime_low;
  public static final Integer uptime_low_default = 0;

  public final Integer uptime_high;
  public static final Integer uptime_high_default = null;

  public final String operand;
  public static final String operand_default = "OR";

  public RecordUIDataHardwareFilter(UnitListData inputData, Map<String, Object> root) {
    ddr_high = inputData.getFilterDdrHigh().getInteger(ddr_high_default);
    ddr_low = inputData.getFilterDdrLow().getInteger(ddr_low_default);
    ocm_high = inputData.getFilterOcmHigh().getInteger(ocm_high_default);
    ocm_low = inputData.getFilterOcmLow().getInteger(ocm_low_default);
    uptime_high = inputData.getFilterUptimeHigh().getInteger(uptime_high_default);
    uptime_low = inputData.getFilterUptimeLow().getInteger(uptime_low_default);
    operand = inputData.getFilterOperand().getString(operand_default);
    root.put(inputData.getFilterDdrHigh().getKey(), ddr_high);
    root.put(inputData.getFilterDdrLow().getKey(), ddr_low);
    root.put(inputData.getFilterOcmHigh().getKey(), ocm_high);
    root.put(inputData.getFilterOcmLow().getKey(), ocm_low);
    root.put(inputData.getFilterUptimeHigh().getKey(), uptime_high);
    root.put(inputData.getFilterUptimeLow().getKey(), uptime_low);
    root.put(inputData.getFilterOperand().getKey(), operand);
  }

  public boolean isAndOperand() {
    return "AND".equals(operand);
  }

  public boolean isOrOperand() {
    return "OR".equals(operand);
  }

  public boolean isRecordRelevant(RecordUIDataHardware record) {
    return isMemoryRelevant(record) || isBootsRelevant(record) || isUptimeRelevant(record);
  }

  public boolean isBootsRelevant(RecordUIDataHardware record) {
    long total = record.getBootTotal();
    return total > 0;
  }

  public boolean isMemoryRelevant(RecordUIDataHardware record) {
    double heapDdrCurrentlyUsed = record.getMemoryHeapDdrUsagePercent();
    boolean isHeapDdrRelevant =
        heapDdrCurrentlyUsed > 0
            && heapDdrCurrentlyUsed < ddr_high
            && heapDdrCurrentlyUsed > ddr_low;
    double heapOcmCurrentUsed = record.getMemoryHeapOcmUsagePercent();
    boolean isHeapOcmRelevant =
        heapOcmCurrentUsed > 0 && heapOcmCurrentUsed < ocm_high && heapOcmCurrentUsed > ocm_low;
    if (isAndOperand()) {
      return isHeapDdrRelevant && isHeapOcmRelevant;
    } else if (isOrOperand()) {
      return isHeapDdrRelevant || isHeapOcmRelevant;
    } else {
      throw new IllegalArgumentException("Operand [" + operand + "] is not valid");
    }
  }

  public boolean isUptimeRelevant(RecordUIDataHardware record) {
    if (record.getCpeUptimeAvg().get() == null) {
      return false;
    }
    double cpeUptime = record.getCpeUptimeAvg().get() / record.getCpeUptimeAvg().getDividend();
    if (uptime_high != null) {
      return cpeUptime >= uptime_low && cpeUptime <= uptime_high;
    } else {
      return cpeUptime >= uptime_low;
    }
  }
}
