package com.owera.xaps.dbi;

import com.owera.common.db.ConnectionProperties;
import com.owera.common.db.ConnectionProvider;
import com.owera.common.db.NoAvailableConnectionException;
import com.owera.xaps.dbi.InsertOrUpdateStatement.Field;
import com.owera.xaps.dbi.util.XAPSVersionCheck;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * XAPSUnit is a class to help you work with units and unit parameters.
 * 
 */
public class ScriptExecutions {

	private ConnectionProperties connectionProperties;

	public ScriptExecutions(ConnectionProperties connectionProperties) {
		this.connectionProperties = connectionProperties;
	}

	/**
	 * Used by Syslog/Core server to initiate syslog-event og trigger scripts
	 * @param scriptFile
	 * @param scriptArgs
	 * @throws SQLException
	 * @throws NoAvailableConnectionException
	 */
	public void requestExecution(File scriptFile, String scriptArgs, String requestId) throws SQLException, NoAvailableConnectionException {
		Connection connection = null;
		PreparedStatement ps = null;
		if (scriptFile.getType() != FileType.SHELL_SCRIPT)
			throw new IllegalArgumentException("The file type is not " + FileType.SHELL_SCRIPT);
		try {
			connection = ConnectionProvider.getConnection(connectionProperties, true);
			InsertOrUpdateStatement ious = new InsertOrUpdateStatement("script_execution", new Field("id", (Integer) null));
			ious.addField(new Field("unit_type_id", scriptFile.getUnittype().getId()));
			ious.addField(new Field("filestore_id", scriptFile.getId()));
			ious.addField(new Field("arguments", scriptArgs));
			ious.addField(new Field("request_timestamp", new Date()));
			ious.addField(new Field("request_id", requestId));

			//			System.out.println(scriptFile.getUnittype().getId());
			//			System.out.println(scriptFile.getId());
			//			System.out.println(scriptArgs);
			//			System.out.println(requestId);

			ps = ious.makePreparedStatement(connection);
			ps.setQueryTimeout(5);
			ps.executeUpdate();
		} catch (SQLException sqle) {
			throw sqle;
		} finally {
			if (ps != null)
				ps.close();
			if (connection != null)
				ConnectionProvider.returnConnection(connection, null);
		}
	}

	/**
	 * Used by Core. Update execeutions when they start/failed/finish
	 * @param se
	 * @throws NoAvailableConnectionException
	 * @throws SQLException
	 */
	public void updateExecution(ScriptExecution se) throws NoAvailableConnectionException, SQLException {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = ConnectionProvider.getConnection(connectionProperties, true);
			InsertOrUpdateStatement ious = new InsertOrUpdateStatement("script_execution", new Field("id", se.getId()));
			ious.addField(new Field("start_timestamp", se.getStartTms()));
			ious.addField(new Field("end_timestamp", se.getEndTms()));
			ious.addField(new Field("error_message", se.getErrorMessage()));
			if (se.getEndTms() != null)
				ious.addField(new Field("exit_status", se.getErrorMessage() == null ? 0 : 1));
			ps = ious.makePreparedStatement(connection);
			ps.setQueryTimeout(5);
			ps.executeUpdate();
		} catch (SQLException sqle) {
			throw sqle;
		} finally {
			if (ps != null)
				ps.close();
			if (connection != null)
				ConnectionProvider.returnConnection(connection, null);
		}
	}

	/**
	 * Only to be used from Core (Script Daemon)
	 * @param xaps
	 * @return
	 * @throws NoAvailableConnectionException
	 * @throws SQLException
	 */
	public List<ScriptExecution> getNotStartedExecutions(XAPS xaps, int poolsize) throws NoAvailableConnectionException, SQLException {
		List<ScriptExecution> scriptExecutionList = new ArrayList<ScriptExecution>();
		if (!XAPSVersionCheck.scriptExecutionSupported)
			return scriptExecutionList;
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection = ConnectionProvider.getConnection(connectionProperties, true);
			DynamicStatement ds = new DynamicStatement();
			ds.addSql("SELECT * from script_execution WHERE start_timestamp IS NULL LIMIT " + poolsize);
			ps = ds.makePreparedStatement(connection);
			ps.setQueryTimeout(60);
			rs = ps.executeQuery();
			return getExecutionList(rs, xaps);
		} catch (SQLException sqle) {
			throw sqle;
		} finally {
			if (rs != null)
				rs.close();
			if (ps != null)
				ps.close();
			if (connection != null)
				ConnectionProvider.returnConnection(connection, null);
		}
	}

	private List<ScriptExecution> getExecutionList(ResultSet rs, XAPS xaps) throws SQLException {
		List<ScriptExecution> scriptExecutionList = new ArrayList<ScriptExecution>();
		while (rs.next()) {
			ScriptExecution se = new ScriptExecution();
			se.setArguments(rs.getString("arguments"));
			se.setRequestTms(rs.getTimestamp("request_timestamp"));
			se.setRequestId(rs.getString("request_id"));
			se.setId(rs.getInt("id"));
			Unittype unittype = xaps.getUnittype(rs.getInt("unit_type_id"));
			if (unittype != null) {
				se.setUnittype(unittype);
				se.setScriptFile(unittype.getFiles().getById(rs.getInt("filestore_id")));
			}
			se.setStartTms(rs.getTimestamp("start_timestamp"));
			se.setEndTms(rs.getTimestamp("end_timestamp"));
			se.setErrorMessage(rs.getString("error_message"));
			if (rs.getString("exit_status") != null)
				se.setExitStatus(rs.getInt("exit_status") == 1 ? true : false);
			scriptExecutionList.add(se);

		}
		return scriptExecutionList;

	}

	/**
	 * Meant to be used from Web/Shell to list ongoing executions
	 * @param unittype
	 * @param requestTmsFrom
	 * @return
	 * @throws NoAvailableConnectionException
	 * @throws SQLException
	 */
	public List<ScriptExecution> getExecutions(Unittype unittype, Date requestTmsFrom, String requestId) throws NoAvailableConnectionException, SQLException {
		List<ScriptExecution> scriptExecutionList = new ArrayList<ScriptExecution>();
		if (!XAPSVersionCheck.scriptExecutionSupported)
			return scriptExecutionList;
		XAPS xaps = unittype.getXaps();
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection = ConnectionProvider.getConnection(connectionProperties, true);
			DynamicStatement ds = new DynamicStatement();
			ds.addSqlAndArguments("SELECT * from script_execution WHERE unit_type_id = ? ", unittype.getId());
			if (requestTmsFrom != null)
				ds.addSqlAndArguments("AND request_timestamp >= ? ", requestTmsFrom);
			if (requestId != null) {
				if (requestId.contains("$") || requestId.contains("_"))
					ds.addSqlAndArguments("AND request_id LIKE ?", requestId);
				else
					ds.addSqlAndArguments("AND request_id = ?", requestId);

			}
			ps = ds.makePreparedStatement(connection);
			ps.setQueryTimeout(60);
			rs = ps.executeQuery();
			return getExecutionList(rs, xaps);
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw sqle;
		} finally {
			if (rs != null)
				rs.close();
			if (ps != null)
				ps.close();
			if (connection != null)
				ConnectionProvider.returnConnection(connection, null);
		}
	}

	public ScriptExecution getById(Unittype unittype, Integer id) throws NoAvailableConnectionException, SQLException {
		if (!XAPSVersionCheck.scriptExecutionSupported)
			return null;
		XAPS xaps = unittype.getXaps();
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection = ConnectionProvider.getConnection(connectionProperties, true);
			DynamicStatement ds = new DynamicStatement();
			ds.addSqlAndArguments("SELECT * from script_execution WHERE unit_type_id = ? ", unittype.getId());
			ds.addSqlAndArguments("AND id = ?", id);
			ps = ds.makePreparedStatement(connection);
			ps.setQueryTimeout(60);
			rs = ps.executeQuery();
			List<ScriptExecution> list = getExecutionList(rs, xaps);
			if (list.size() > 0)
				return list.get(0);
			else
				return null;
		} catch (SQLException sqle) {
			throw sqle;
		} finally {
			if (rs != null)
				rs.close();
			if (ps != null)
				ps.close();
			if (connection != null)
				ConnectionProvider.returnConnection(connection, null);
		}
	}

	/**
	 * Used by Web to identify one single ScriptExecution trigger by a trigger-release
	 * Used by TR069 to check if the script-execution is finished
	 * @param unittype
	 * @param requestTmsFrom
	 * @return
	 * @throws NoAvailableConnectionException
	 * @throws SQLException
	 */
	public ScriptExecution getExecution(Unittype unittype, String requestId) throws NoAvailableConnectionException, SQLException {
		if (!XAPSVersionCheck.scriptExecutionSupported)
			return null;
		XAPS xaps = unittype.getXaps();
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection = ConnectionProvider.getConnection(connectionProperties, true);
			DynamicStatement ds = new DynamicStatement();
			ds.addSqlAndArguments("SELECT * from script_execution WHERE unit_type_id = ? ", unittype.getId());
			if (requestId != null)
				ds.addSqlAndArguments("AND request_id = ?", requestId);
			ps = ds.makePreparedStatement(connection);
			ps.setQueryTimeout(60);
			rs = ps.executeQuery();
			List<ScriptExecution> list = getExecutionList(rs, xaps);
			if (list.size() > 0)
				return list.get(0);
			else
				return null;
		} catch (SQLException sqle) {
			throw sqle;
		} finally {
			if (rs != null)
				rs.close();
			if (ps != null)
				ps.close();
			if (connection != null)
				ConnectionProvider.returnConnection(connection, null);
		}
	}

	/**
	 * Used by Core to delete old executions
	 * @param upUntil
	 * @return
	 * @throws NoAvailableConnectionException
	 * @throws SQLException
	 */
	public int deleteExecutions(Date upUntil) throws NoAvailableConnectionException, SQLException {
		if (!XAPSVersionCheck.scriptExecutionSupported)
			return 0;
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection = ConnectionProvider.getConnection(connectionProperties, true);
			DynamicStatement ds = new DynamicStatement();
			ds.addSqlAndArguments("DELETE from script_execution WHERE request_timestamp < ?", upUntil);
			ps = ds.makePreparedStatement(connection);
			ps.setQueryTimeout(60);
			return ps.executeUpdate();
		} catch (SQLException sqle) {
			throw sqle;
		} finally {
			if (rs != null)
				rs.close();
			if (ps != null)
				ps.close();
			if (connection != null)
				ConnectionProvider.returnConnection(connection, null);
		}
	}

}
