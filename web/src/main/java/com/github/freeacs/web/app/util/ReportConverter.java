package com.github.freeacs.web.app.util;

import com.github.freeacs.dbi.report.PeriodType;
import com.github.freeacs.dbi.report.RecordHardware;
import com.github.freeacs.dbi.report.RecordSyslog;
import com.github.freeacs.dbi.report.RecordVoip;
import com.github.freeacs.dbi.report.Report;

public final class ReportConverter {

    private ReportConverter() {
    }

    /**
     * Converts a voip report to a given period type.
     *
     * @param report the report
     * @param type the type
     * @return The changed report if period type is different, or the same report unchanged.
     */
    public static Report<RecordVoip> convertVoipReport(Report<RecordVoip> report, PeriodType type) {
        if (type != null && report.getPeriodType().getTypeInt() != type.getTypeInt()) {
            report = com.github.freeacs.dbi.report.ReportConverter.convertVoipReport(report, type);
        }
        return report;
    }

    /**
     * Convert hardware report.
     *
     * @param report the report
     * @param type the type
     * @return the report
     */
    public static Report<RecordHardware> convertHardwareReport(
        Report<RecordHardware> report, PeriodType type) {
        if (type != null && report.getPeriodType().getTypeInt() != type.getTypeInt()) {
            report = com.github.freeacs.dbi.report.ReportConverter.convertHardwareReport(report, type);
        }
        return report;
    }

    /**
     * Convert syslog report.
     *
     * @param report the report
     * @param type the type
     * @return the report
     */
    public static Report<RecordSyslog> convertSyslogReport(
        Report<RecordSyslog> report, PeriodType type) {
        if (type != null && report.getPeriodType().getTypeInt() != type.getTypeInt()) {
            report = com.github.freeacs.dbi.report.ReportConverter.convertSyslogReport(report, type);
        }
        return report;
    }

}
