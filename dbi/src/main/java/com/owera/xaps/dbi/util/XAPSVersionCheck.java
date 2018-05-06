package com.owera.xaps.dbi.util;

import com.owera.common.db.ConnectionProperties;
import com.owera.common.db.ConnectionProvider;
import com.owera.common.db.NoAvailableConnectionException;
import com.owera.common.log.Logger;

import java.sql.*;

public class XAPSVersionCheck {

	private static Logger logger = new Logger();

	// Marks the beginning of 2013R1
	public static boolean triggerSupported = false;
	// Improved user/permission system
	public static boolean adminSupported = false;
	// Added scriptExecution
	public static boolean scriptExecutionSupported = false;
	// Added unit_param_session table
	public static boolean unitParamSessionSupported = false;
	// Added new table: heartbeat
	public static boolean heartbeatSupported = false;
	// Major rework of syslog event table
	public static boolean syslogEventReworkSupported = false;
	// File supports targetName and owner, subtype is removed, 
	public static boolean fileReworkSupported = false;

	private static boolean databaseChecked = false;

	private static boolean existsColum(ResultSetMetaData rsmd, String columnName) {
		try {
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				if (columnName.equalsIgnoreCase(rsmd.getColumnName(i))) {
					return true;
				}
			}
		} catch (Throwable t) {
			System.err.println("An error occurred in existsColumn " + t);
			t.printStackTrace();
			return false;
		}
		return false;
	}

	public static void versionCheck(ConnectionProperties connectionProperties) throws SQLException, NoAvailableConnectionException {
		if (databaseChecked) // possible to force re-check of database
			return;
		Connection c = ConnectionProvider.getConnection(connectionProperties);
		Statement s = null;
		ResultSet rs = null;
		SQLException sqle = null;
		try {
			try {
				s = c.createStatement();
				s.setQueryTimeout(60);
				rs = s.executeQuery("SELECT * FROM trigger_ WHERE id = -1");
				triggerSupported = true;
				rs.close();
			} catch (Throwable t) {
				triggerSupported = false;
			}

			/* implies both an operator column and a parameter type column (and removal of is_equal column) */
			s = c.createStatement();
			s.setQueryTimeout(10);
			rs = s.executeQuery("SELECT * FROM user_ WHERE id = -1");
			adminSupported = XAPSVersionCheck.existsColum(rs.getMetaData(), "is_admin");
			rs.close();

			try {
				s = c.createStatement();
				s.setQueryTimeout(60);
				rs = s.executeQuery("SELECT * FROM script_execution WHERE id = -1");
				scriptExecutionSupported = true;
				rs.close();
			} catch (Throwable t) {
				scriptExecutionSupported = false;
			}

			try {
				s = c.createStatement();
				s.setQueryTimeout(60);
				rs = s.executeQuery("SELECT * FROM unit_param_session WHERE unit_id = -1");
				unitParamSessionSupported = true;
				rs.close();
			} catch (Throwable t) {
				unitParamSessionSupported = false;
			}

			try {
				/* implies both an operator column and a parameter type column (and removal of is_equal column) */
				s = c.createStatement();
				s.setQueryTimeout(10);
				rs = s.executeQuery("SELECT * FROM heartbeat WHERE id = -1");
				heartbeatSupported = true;
				rs.close();
			} catch (Throwable t) {
				heartbeatSupported = false;
			}

			s = c.createStatement();
			s.setQueryTimeout(10);
			rs = s.executeQuery("SELECT * FROM syslog_event WHERE id = -1");
			syslogEventReworkSupported = XAPSVersionCheck.existsColum(rs.getMetaData(), "filestore_id");
			rs.close();

			s = c.createStatement();
			s.setQueryTimeout(10);
			rs = s.executeQuery("SELECT * FROM filestore WHERE id = -1");
			fileReworkSupported = XAPSVersionCheck.existsColum(rs.getMetaData(), "owner");
			rs.close();

			if (logger.isDebugEnabled()) {
				String msg = "";
				msg += "TriggerSupported: " + triggerSupported + ", AdminSupported: " + adminSupported;
				msg += ", ScriptExecutionSupported: " + scriptExecutionSupported + ", UnitParamSessionSupported: " + unitParamSessionSupported;
				msg += ", HeartbeatSupported: " + heartbeatSupported + ", SyslogEventGroupSupported: " + syslogEventReworkSupported;
				msg += ", FileReworkSupported: " + fileReworkSupported;
				logger.debug(msg);
			}
			databaseChecked = true;
		} catch (Throwable t) {
			if (t instanceof SQLException)
				sqle = (SQLException) t;
		} finally {
			if (rs != null)
				rs.close();
			if (s != null)
				s.close();
			ConnectionProvider.returnConnection(c, sqle);
		}

	}

	public static void setDatabaseChecked(boolean databaseChecked) {
		XAPSVersionCheck.databaseChecked = databaseChecked;
	}
}
