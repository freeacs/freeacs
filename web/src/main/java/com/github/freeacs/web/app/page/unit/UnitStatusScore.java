package com.github.freeacs.web.app.page.unit;

import com.github.freeacs.dbi.SyslogEntry;
import com.github.freeacs.web.app.page.report.uidata.RecordUIDataHardware;
import java.util.List;

/** The Class UnitStatusScore. */
public class UnitStatusScore {
  /** The Constant TOP_STATUS. */
  private static final int TOP_STATUS = 10;

  /** The service window effect. */
  private Integer serviceWindowEffect = 0;

  /** The voip line effect. */
  private Integer voipLineEffect = 0;

  /** The total score effect. */
  private Double totalScoreEffect = 0d;

  /** The hardware effect. */
  private Double hardwareEffect = 0d;

  /** The syslog effect. */
  private Double syslogEffect = 0d;

  /** The Constant SYSLOG_SEVERITY_HIGH_WITHIN_SERVICE_WINDOW_PENALTY. */
  private static final double SYSLOG_SEVERITY_HIGH_WITHIN_SERVICE_WINDOW_PENALTY = 0.2d;

  /** The Constant SYSLOG_SEVERITY_HIGH_OUTSIDE_SERVICE_WINDOW_PENALTY. */
  private static final double SYSLOG_SEVERITY_HIGH_OUTSIDE_SERVICE_WINDOW_PENALTY = 0.1d;

  /** The Constant HARDWARE_WITHIN_SERVICE_WINDOW_PENALTY. */
  private static final double HARDWARE_WITHIN_SERVICE_WINDOW_PENALTY = 0.5d;

  /** The Constant HARDWARE_OUTSIDE_SERVICE_WINDOW_PENALTY. */
  private static final double HARDWARE_OUTSIDE_SERVICE_WINDOW_PENALTY = 0.25d;

  /**
   * Instantiates a new unit status score.
   *
   * @param totalScore the total score
   * @param hardwareReport the hardware report
   * @param entries the entries
   * @param isWithinServiceWindow the is within service window
   * @param is1LinesHasProblems the is1 lines has problems
   * @param is2LinesHasProblems the is2 lines has problems
   */
  public UnitStatusScore(
      Double totalScore,
      List<RecordUIDataHardware> hardwareReport,
      List<SyslogEntry> entries,
      boolean isWithinServiceWindow,
      boolean is1LinesHasProblems,
      boolean is2LinesHasProblems) {
    if (!isWithinServiceWindow) {
      serviceWindowEffect = 7;
      if (is1LinesHasProblems) {
        voipLineEffect = 2;
      } else if (is2LinesHasProblems) {
        voipLineEffect = 3;
      }
      setSyslogEffect(entries, SYSLOG_SEVERITY_HIGH_OUTSIDE_SERVICE_WINDOW_PENALTY);
    } else {
      if (is1LinesHasProblems) {
        voipLineEffect = 4;
      } else if (is2LinesHasProblems) {
        voipLineEffect = 6;
      } else if (totalScore != null) {
        this.totalScoreEffect = (1f - totalScore / 100f) * 6;
      }
      setSyslogEffect(entries, SYSLOG_SEVERITY_HIGH_WITHIN_SERVICE_WINDOW_PENALTY);
    }

    setHardwareEffect(hardwareReport, isWithinServiceWindow);
  }

  /**
   * Gets the hardware effect.
   *
   * @return the hardware effect
   */
  public double getHardwareEffect() {
    return this.hardwareEffect;
  }

  /**
   * Sets the hardware effect.
   *
   * @param hardwareReport the hardware report
   * @param isWithinServiceWindow the is within service window
   */
  public void setHardwareEffect(
      List<RecordUIDataHardware> hardwareReport, boolean isWithinServiceWindow) {
    for (RecordUIDataHardware uiDataRecord : hardwareReport) {
      if (uiDataRecord.getBootTotal() > 0) {
        if (uiDataRecord.getBootWatchdogCount().get() > 0) {
          this.hardwareEffect +=
              isWithinServiceWindow
                  ? HARDWARE_WITHIN_SERVICE_WINDOW_PENALTY
                  : HARDWARE_OUTSIDE_SERVICE_WINDOW_PENALTY;
        }
      } else if (uiDataRecord.isMemoryRelevant()) {
        this.hardwareEffect +=
            isWithinServiceWindow
                ? HARDWARE_WITHIN_SERVICE_WINDOW_PENALTY
                : HARDWARE_OUTSIDE_SERVICE_WINDOW_PENALTY;
      }
    }
  }

  /**
   * Gets the syslog effect.
   *
   * @return the syslog effect
   */
  public double getSyslogEffect() {
    return this.syslogEffect;
  }

  /**
   * Sets the syslog effect.
   *
   * @param entries the entries
   * @param penalty the penalty
   */
  public void setSyslogEffect(List<SyslogEntry> entries, double penalty) {
    for (SyslogEntry entry : entries) {
      if (entry.getSeverity() <= 3) {
        this.syslogEffect += penalty;
      }
    }
  }

  /**
   * Gets the status.
   *
   * @return the status
   */
  public Double getStatus() {
    double status =
        (double) TOP_STATUS
            - getServiceWindowEffect()
            - getTotalScoreEffect()
            - getVoipLineEffect()
            - hardwareEffect
            - syslogEffect;
    if (status < 0) {
      return 0d;
    }
    return status;
  }

  /**
   * Gets the service window effect.
   *
   * @return the service window effect
   */
  public Integer getServiceWindowEffect() {
    return serviceWindowEffect;
  }

  /**
   * Gets the voip line effect.
   *
   * @return the voip line effect
   */
  public Integer getVoipLineEffect() {
    return voipLineEffect;
  }

  /**
   * Gets the total score effect.
   *
   * @return the total score effect
   */
  public Double getTotalScoreEffect() {
    return totalScoreEffect;
  }
}
