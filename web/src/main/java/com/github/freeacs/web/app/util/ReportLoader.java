package com.github.freeacs.web.app.util;

import com.github.freeacs.dbi.SyslogFilter;
import com.github.freeacs.dbi.report.RecordHardware;
import com.github.freeacs.dbi.report.RecordSyslog;
import com.github.freeacs.dbi.report.RecordVoip;
import com.github.freeacs.dbi.report.Report;
import com.github.freeacs.dbi.report.ReportHardwareGenerator;
import com.github.freeacs.dbi.report.ReportSyslogGenerator;
import com.github.freeacs.dbi.report.ReportVoipGenerator;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;

public final class ReportLoader {

    private ReportLoader(){
    }

    /**
     * Gets the syslog report.
     *
     * @param sessionId the session id
     * @param unitId the unit id
     * @param fromDate the from date
     * @param toDate the to date
     * @param syslogFilter
     * @param xapsDataSource
     * @param syslogDataSource
     * @return the syslog report the no available connection exception
     * @throws SQLException the sQL exception
     * @throws ParseException the parse exception
     */
    @SuppressWarnings("unchecked")
    public static Report<RecordSyslog> getSyslogReport(final String sessionId,
                                                       final String unitId,
                                                       final Date fromDate,
                                                       final Date toDate,
                                                       final String syslogFilter,
                                                       final DataSource xapsDataSource,
                                                       final DataSource syslogDataSource)
        throws SQLException, ParseException {

        SyslogFilter filter = new SyslogFilter();
        filter.setMessage(syslogFilter);
        ReportSyslogGenerator rg =
            new ReportSyslogGenerator(
                xapsDataSource,
                ACSLoader.getXAPS(sessionId, xapsDataSource, syslogDataSource),
                null,
                ACSLoader.getIdentity(sessionId, xapsDataSource));
        rg.setSyslogFilter(filter);
        return rg.generateFromSyslog(fromDate, toDate, unitId);
    }

    /**
     * Gets the hardware report.
     *
     * @param sessionId the session id
     * @param unitId the unit id
     * @param fromDate the from date
     * @param toDate the to date
     * @param xapsDataSource
     * @param syslogDataSource
     * @return the hardware report the no available connection exception
     * @throws SQLException the sQL exception
     */
    @SuppressWarnings("unchecked")
    public static Report<RecordHardware> getHardwareReport(final String sessionId,
                                                           final String unitId,
                                                           final Date fromDate,
                                                           final Date toDate,
                                                           final DataSource xapsDataSource,
                                                           final DataSource syslogDataSource)
        throws SQLException {
        ReportHardwareGenerator rg =
            new ReportHardwareGenerator(
                xapsDataSource,
                ACSLoader.getXAPS(sessionId, xapsDataSource, syslogDataSource),
                null,
                ACSLoader.getIdentity(sessionId, xapsDataSource));
        return rg.generateFromSyslog(fromDate, toDate, unitId);
    }

    /**
     * Get voip report for a given unit.
     *
     * @param sessionId the session id
     * @param unitId the unit id
     * @param fromDate the from date
     * @param toDate the to date
     * @param xapsDataSource
     * @param syslogDataSource
     * @return The report. Is null when report functionality is not supported by the database. the no
     *     available connection exception
     * @throws SQLException the sQL exception
     */
    @SuppressWarnings("unchecked")
    public static Report<RecordVoip> getVoipReport(final String sessionId,
                                                   final String unitId,
                                                   final Date fromDate,
                                                   final Date toDate,
                                                   final DataSource xapsDataSource,
                                                   final DataSource syslogDataSource)
        throws SQLException {
        ReportVoipGenerator rg =
            new ReportVoipGenerator(
                xapsDataSource,
                ACSLoader.getXAPS(sessionId, xapsDataSource, syslogDataSource),
                null,
                ACSLoader.getIdentity(sessionId, xapsDataSource));
        return rg.generateFromSyslog(fromDate, toDate, unitId);
    }

}
