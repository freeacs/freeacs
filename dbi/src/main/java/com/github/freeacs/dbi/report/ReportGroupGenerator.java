package com.github.freeacs.dbi.report;

import com.github.freeacs.dbi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;


public class ReportGroupGenerator extends ReportGenerator {

	private static Logger logger = LoggerFactory.getLogger(ReportGroupGenerator.class);

	public ReportGroupGenerator(DataSource mainDataSource, DataSource syslogDataSource, ACS acs, String logPrefix, Identity id) {
		super(mainDataSource, syslogDataSource, acs, logPrefix, id);
	}

	public Report<RecordGroup> generateGroupReport(PeriodType periodType, Date start, Date end, List<Unittype> uts, Group g) throws SQLException, IOException {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		SQLException sqle = null;
		try {
			Report<RecordGroup> report = new Report<RecordGroup>(RecordGroup.class, periodType);
			connection = mainDataSource.getConnection();
			
			logger.info(logPrefix + "Reads from report_group table from " + start + " to " + end);
			DynamicStatement ds = selectReportSQL("report_group", periodType, start, end, uts, null);
			if (g != null) {
				ds.addSqlAndArguments(" and group_name = ?", g.getName());
			}
			ps = ds.makePreparedStatement(connection);
			rs = ps.executeQuery();
			int counter = 0;
			while (rs.next()) {
				counter++;
				start = rs.getTimestamp("timestamp_");
				String unittypeName = rs.getString("unit_type_name");
				String groupName = rs.getString("group_name");
				RecordGroup recordTmp = new RecordGroup(start, periodType, unittypeName, groupName);
				Key key = recordTmp.getKey();
				RecordGroup record = report.getRecord(key);
				if (record == null)
					record = recordTmp;
				record.getUnitCount().set(rs.getInt("unit_count"));
				report.setRecord(key, record);
			}
			logger.info(logPrefix + "Have read " + counter + " rows, last tms was " + start + ", report is now " + report.getMap().size() + " entries");
			return report;
		} catch (SQLException sqlex) {
			sqle = sqlex;
			throw sqlex;
		} finally {
			if (rs != null)
				rs.close();
			if (ps != null)
				ps.close();
			if (connection != null) {
				connection.close();
			}
		}
	}

}
