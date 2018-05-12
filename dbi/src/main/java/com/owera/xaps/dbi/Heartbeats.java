package com.owera.xaps.dbi;

import com.owera.common.db.ConnectionProvider;
import com.owera.common.db.NoAvailableConnectionException;
import com.owera.xaps.dbi.InsertOrUpdateStatement.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;


public class Heartbeats {
	private static Logger logger = LoggerFactory.getLogger(Heartbeats.class);

	private Map<String, Heartbeat> nameMap;
	private Map<Integer, Heartbeat> idMap;
	private Unittype unittype;

	public Heartbeats(Map<Integer, Heartbeat> idMap, Map<String, Heartbeat> nameMap, Unittype unittype) {
		this.idMap = idMap;
		this.nameMap = nameMap;
		this.unittype = unittype;
	}

	
	public Heartbeat getById(Integer id) {
		return idMap.get(id);
	}

	public Heartbeat getByName(String name) {
		return nameMap.get(name);
	}

	public Heartbeat[] getHeartbeats() {
		return nameMap.values().toArray(new Heartbeat[] {});
	}

	@Override
	public String toString() {
		return "Contains " + idMap.size() + " heartbeats";
	}

	private void addOrChangeHeartbeatImpl(Heartbeat heartbeat, XAPS xaps) throws SQLException, NoAvailableConnectionException {
		Connection c = ConnectionProvider.getConnection(xaps.connectionProperties, true);
		SQLException sqlex = null;
		PreparedStatement ps = null;
		try {
			InsertOrUpdateStatement ious = new InsertOrUpdateStatement("heartbeat", new Field("id", heartbeat.getId()));
			ious.addField(new Field("name", heartbeat.getName()));
			ious.addField(new Field("unit_type_id", heartbeat.getUnittype().getId()));
			ious.addField(new Field("heartbeat_expression", heartbeat.getExpression().toString()));
			ious.addField(new Field("heartbeat_group_id", heartbeat.getGroup().getId()));
			ious.addField(new Field("heartbeat_timeout_hour", heartbeat.getTimeoutHours()));
			ps = ious.makePreparedStatement(c);
			ps.setQueryTimeout(60);
			ps.executeUpdate();
			if (ious.isInsert()) {
				ResultSet gk = ps.getGeneratedKeys();
				if (gk.next())
					heartbeat.setId(gk.getInt(1));
				logger.info("Inserted heartbeat " + heartbeat.getId());
				if (xaps.getDbi() != null)
					xaps.getDbi().publishAdd(heartbeat, heartbeat.getUnittype());
			} else {
				logger.info("Updated heartbeat " + heartbeat.getId());
				if (xaps.getDbi() != null)
					xaps.getDbi().publishChange(heartbeat, unittype);
			}
		} catch (SQLException sqle) {
			sqlex = sqle;
			throw sqle;
		} finally {
			if (ps != null)
				ps.close();
			if (c != null)
				ConnectionProvider.returnConnection(c, sqlex);
		}
	}

	public void addOrChangeHeartbeat(Heartbeat heartbeat, XAPS xaps) throws SQLException, NoAvailableConnectionException {
		if (!xaps.getUser().isUnittypeAdmin(unittype.getId()))
			throw new IllegalArgumentException("Not allowed action for this user");
		heartbeat.validateInput(true);
		heartbeat.validate();
		addOrChangeHeartbeatImpl(heartbeat, xaps);
		idMap.put(heartbeat.getId(), heartbeat);
		nameMap.put(heartbeat.getName(), heartbeat);
	}

	private void deleteHeartbeatImpl(Heartbeat heartbeat, XAPS xaps) throws SQLException, NoAvailableConnectionException {
		PreparedStatement ps = null;
		Connection c = ConnectionProvider.getConnection(xaps.connectionProperties, true);
		SQLException sqlex = null;
		try {
			DynamicStatement ds = new DynamicStatement();
			ds.addSqlAndArguments("DELETE FROM heartbeat WHERE id = ? ", heartbeat.getId());
			ps = ds.makePreparedStatement(c);
			ps.setQueryTimeout(60);
			ps.executeUpdate();
			
			logger.info("Deleted heartbeat " + heartbeat.getId());
			if (xaps.getDbi() != null)
				xaps.getDbi().publishDelete(heartbeat, unittype);
		} catch (SQLException sqle) {
			sqlex = sqle;
			throw sqle;
		} finally {
			if (ps != null)
				ps.close();
			if (c != null)
				ConnectionProvider.returnConnection(c, sqlex);
		}
	}

	/**
	 * The first time this method is run, the flag is set. The second time this
	 * method is run, the parameter is removed from the name- and id-Map.
	 * 
	 * @throws NoAvailableConnectionException
	 * @throws SQLException
	 */
	public void deleteHeartbeat(Heartbeat heartbeat, XAPS xaps) throws SQLException, NoAvailableConnectionException {
		if (!xaps.getUser().isUnittypeAdmin(unittype.getId()))
			throw new IllegalArgumentException("Not allowed action for this user");
		deleteHeartbeatImpl(heartbeat, xaps);
		idMap.remove(heartbeat.getId());
		nameMap.remove(heartbeat.getName());
	}
}
