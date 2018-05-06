package com.owera.xaps.dbi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Map;

import com.owera.common.db.ConnectionProvider;
import com.owera.common.db.NoAvailableConnectionException;
import com.owera.common.log.Logger;
import com.owera.xaps.dbi.InsertOrUpdateStatement.Field;

public class Unittypes {
	private Logger logger = new Logger();
	private Map<String, Unittype> nameMap;
	private Map<Integer, Unittype> idMap;

	public Unittypes(Map<String, Unittype> nameMap, Map<Integer, Unittype> idMap) {
		this.nameMap = nameMap;
		this.idMap = idMap;
	}

	public Unittype getByName(String name) {
		return nameMap.get(name);
	}

	/**
	 * Only to be used internally (to shape XAPS object according to permissions)
	 * @param id
	 * @return
	 */
	protected void removePermission(Unittype unittype) {
		nameMap.remove(unittype.getName());
		idMap.remove(unittype.getId());
	}

	public Unittype getById(Integer id) {
		return idMap.get(id);
	}

	public Unittype[] getUnittypes() {
		Unittype[] unittypes = new Unittype[nameMap.size()];
		nameMap.values().toArray(unittypes);
		return unittypes;
	}

	@Override
	public String toString() {
		return "Contains " + nameMap.size() + " unittypes (" + super.toString() + ")";
	}

	private void addOrChangeUnittypeImpl(Unittype unittype, XAPS xaps) throws SQLException, NoAvailableConnectionException {
		Connection c = ConnectionProvider.getConnection(xaps.connectionProperties, true);
		SQLException sqlex = null;
		PreparedStatement s = null;
		try {
			InsertOrUpdateStatement ious = new InsertOrUpdateStatement("unit_type", new Field("unit_type_id", unittype.getId()));
			ious.addField(new Field("unit_type_name", unittype.getName()));
			ious.addField(new Field("description", unittype.getDescription()));
			ious.addField(new Field("protocol", unittype.getProtocol().toString()));
			s = ious.makePreparedStatement(c);
			s.setQueryTimeout(60);
			s.executeUpdate();
			if (ious.isInsert()) {
				ResultSet gk = s.getGeneratedKeys();
				if (gk.next())
					unittype.setId(gk.getInt(1));
				int changedSystemParameters = unittype.ensureValidSystemParameters(xaps);
				logger.notice("Added unittype " + unittype.getName() + ", changed/added " + changedSystemParameters);
				if (xaps.getDbi() != null)
					xaps.getDbi().publishAdd(unittype, unittype);
			} else {
				int changedSystemParameters = unittype.ensureValidSystemParameters(xaps);
				logger.notice("Updated unittype " + unittype.getName() + ", changed/added " + changedSystemParameters);
				if (xaps.getDbi() != null)
					xaps.getDbi().publishChange(unittype, unittype);
			}
		} catch (SQLException sqle) {
			sqlex = sqle;
			throw sqle;
		} finally {
			if (s != null)
				s.close();
			if (c != null)
				ConnectionProvider.returnConnection(c, sqlex);
		}
	}

	public void addOrChangeUnittype(Unittype unittype, XAPS xaps) throws NoAvailableConnectionException, SQLException {
		if (unittype.getId() == null && !xaps.getUser().isAdmin())
			throw new IllegalArgumentException("Not allowed action for this user");
		if (!xaps.getUser().isUnittypeAdmin(unittype.getId()))
			throw new IllegalArgumentException("Not allowed action for this user");
		addOrChangeUnittypeImpl(unittype, xaps);
		unittype.setXaps(xaps);
		nameMap.put(unittype.getName(), unittype);
		idMap.put(unittype.getId(), unittype);
		if (unittype.getOldName() != null) {
			nameMap.remove(unittype.getOldName());
			unittype.setOldName(null);
		}
		Profiles profiles = unittype.getProfiles();
		if (profiles.getProfiles().length == 0)
			profiles.addOrChangeProfile(new Profile("Default", unittype), xaps);

	}

	private int deleteUnittypeImpl(Unittype unittype, XAPS xaps) throws SQLException, NoAvailableConnectionException {
		Statement s = null;
		String sql = null;
		Connection c = ConnectionProvider.getConnection(xaps.connectionProperties, true);
		SQLException sqlex = null;
		try {
			s = c.createStatement();
			sql = "DELETE FROM unit_type WHERE ";
			sql += "unit_type_id = " + unittype.getId();
			s.setQueryTimeout(60);
			int rowsDeleted = s.executeUpdate(sql);

			logger.notice("Deleted unittype " + unittype.getName());
			if (xaps.getDbi() != null)
				xaps.getDbi().publishDelete(unittype, unittype);
			return rowsDeleted;
		} catch (SQLException sqle) {
			sqlex = sqle;
			throw sqle;
		} finally {
			if (s != null)
				s.close();
			if (c != null)
				ConnectionProvider.returnConnection(c, sqlex);
		}
	}

	/**
	 * The first time this method is run, the flag is set. The second time this
	 * method is run, the parameter is removed from the nameMap. Setting the
	 * cascade argument = true will also delete all unittype parameters and
	 * enumerations for all these parameters.
	 * 
	 * @param profileParameter
	 * @throws NoAvailableConnectionException
	 * @throws SQLException
	 */
	public int deleteUnittype(Unittype unittype, XAPS xaps, boolean cascade) throws SQLException, NoAvailableConnectionException {
		if (!xaps.getUser().isUnittypeAdmin(unittype.getId()))
			throw new IllegalArgumentException("Not allowed action for this user");
		if (cascade) {
			UnittypeParameters utParams = unittype.getUnittypeParameters();
			UnittypeParameter[] utParamsArr = utParams.getUnittypeParameters();
			//			System.out.println("Will delete " + utParamsArr.length + " unittype parameters from unittype " + unittype.getName());
			Profile defaultProfile = unittype.getProfiles().getByName("Default");
			// Delete the defaultProfile if this is the only profile in existence and if the profile has no profile parameters
			if (defaultProfile != null && unittype.getProfiles().getProfiles().length == 1 && defaultProfile.getProfileParameters().getProfileParameters().length == 0)
				unittype.getProfiles().deleteProfile(defaultProfile, xaps, false);
			utParams.deleteUnittypeParameters(Arrays.asList(utParamsArr), xaps);
			Groups groups = unittype.getGroups();
			for (Group g : groups.getGroups()) {
				groups.deleteGroup(g, xaps);
			}

			SyslogEvents syslogEvents = unittype.getSyslogEvents();
			for (SyslogEvent sg : syslogEvents.getSyslogEvents()) {
				if (sg.getUnittype() != null)
					syslogEvents.deleteSyslogEventImpl(sg, xaps);
			}

		}
		int rowsDeleted = deleteUnittypeImpl(unittype, xaps);
		nameMap.remove(unittype.getName());
		idMap.remove(unittype.getId());
		return rowsDeleted;
	}
}
