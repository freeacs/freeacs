package com.owera.xaps.dbi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.owera.common.db.ConnectionProvider;
import com.owera.common.db.NoAvailableConnectionException;
import com.owera.common.log.Logger;
import com.owera.xaps.dbi.DynamicStatement.NullString;


public class GroupParameters {
	private static Logger logger = new Logger();
	private Map<String, GroupParameter> nameMap;
	private Map<Integer, GroupParameter> idMap;
	private Group group;

	public GroupParameters(Map<String, GroupParameter> nameMap, Map<Integer, GroupParameter> idMap, Group group) {
		this.nameMap = nameMap;
		this.idMap = idMap;
		this.group = group;
	}

	public GroupParameter getByName(String name) {
		return nameMap.get(name);
	}

	public GroupParameter getById(Integer id) {
		return idMap.get(id);
	}

	@Override
	public String toString() {
		return "Contains " + nameMap.size() + " group parameters";
	}

	public GroupParameter[] getGroupParameters() {
		GroupParameter[] pArr = new GroupParameter[nameMap.size()];
		nameMap.values().toArray(pArr);
		return pArr;
	}

	public List<Parameter> getParameters() {
		List<Parameter> params = new ArrayList<Parameter>();
		for (Entry<String, GroupParameter> entry : nameMap.entrySet()) {
			params.add(entry.getValue().getParameter());
		}
		return params;
	}

	/**
	 * This method is about getting a list of all parameters, and is therefore placed here.
	 * But this class has no knowledge about group, so we have to inject it.
	 * 
	 * @param group injected group
	 * @return List<Parameter> All parameters including parents
	 */
	public List<Parameter> getAllParameters(Group group) {
		List<Parameter> groupParams = new ArrayList<Parameter>();
		groupParams.addAll(group.getGroupParameters().getParameters());
		Group parent = group;
		while ((parent = parent.getParent()) != null) {
			groupParams.addAll(parent.getGroupParameters().getParameters());
		}
		return groupParams;
	}

	private void addOrChangeGroupParameterImpl(GroupParameter groupParameter, Group group, XAPS xaps) throws SQLException, NoAvailableConnectionException {
		Connection c = ConnectionProvider.getConnection(xaps.connectionProperties, true);
		SQLException sqlex = null;
		PreparedStatement ps = null;
		try {
			Parameter parameter = groupParameter.getParameter();
			DynamicStatement ds = new DynamicStatement();
			//			if (XAPSVersionCheck.groupParamTypeSupported) {
			if (groupParameter.getId() == null) {
				ds.addSql("INSERT INTO group_param (group_id, unit_type_param_id, operator, data_type, value) VALUES (?, ?, ?, ?, ?)");
				ds.addArguments(groupParameter.getGroup().getId(), parameter.getUnittypeParameter().getId());
				ds.addArguments(parameter.getOp().getOperatorSign());
				ds.addArguments(parameter.getType().getType());
				ds.addArguments((parameter.valueWasNull() || parameter.getValue() == null) ? new NullString() : parameter.getValue());
				ps = ds.makePreparedStatement(c, "id");
				ps.setQueryTimeout(60);
				ps.executeUpdate();
				ResultSet gk = ps.getGeneratedKeys();
				if (gk.next())
					groupParameter.setId(gk.getInt(1));
				logger.notice("Added group parameter " + groupParameter.getName());
				if (xaps.getDbi() != null)
					xaps.getDbi().publishAdd(groupParameter, group.getUnittype());
			} else {
				ds.addSql("UPDATE group_param SET ");
				ds.addSql("value = ?, operator = ?, data_type = ? WHERE id = ?");
				ds.addArguments((parameter.valueWasNull() || parameter.getValue() == null) ? new NullString() : parameter.getValue());
				ds.addArguments(parameter.getOp().getOperatorSign());
				ds.addArguments(parameter.getType().getType());
				ds.addArguments(groupParameter.getId());
				ps = ds.makePreparedStatement(c);
				ps.setQueryTimeout(60);
				ps.executeUpdate();
				logger.notice("Updated group parameter " + groupParameter.getName());
				if (xaps.getDbi() != null)
					xaps.getDbi().publishChange(groupParameter, group.getUnittype());
			}
			//			} else {
			//				if (groupParameter.getId() == null) {
			//					ds.addSql("INSERT INTO group_param (group_id, unit_type_param_id, is_equal, value) VALUES (?, ?, ?, ?)");
			//					ds.addArguments(groupParameter.getGroup().getId(), parameter.getUnittypeParameter().getId());
			//					ds.addArguments(parameter.getOp().equals(Operator.EQ) ? 1 : 0);
			//					ds.addArguments((parameter.valueWasNull() || parameter.getValue() == null) ? new NullString() : parameter.getValue());
			//					ps = ds.makePreparedStatement(c, "id");
			//					ps.setQueryTimeout(60);
			//					ps.executeUpdate();
			//					ResultSet gk = ps.getGeneratedKeys();
			//					if (gk.next())
			//						groupParameter.setId(gk.getInt(1));
			//					LogContext.set(group.getUnittype(), group.getTopParent().getProfile(), xaps);
			//					logger.notice("Added group parameter " + groupParameter.getName());
			//					if (xaps.getDbi() != null)
			//						xaps.getDbi().publishAdd(groupParameter, group.getUnittype());
			//				} else {
			//					ds.addSql("UPDATE group_param SET ");
			//					ds.addSql("value = ?, is_equal = ? WHERE id = ?");
			//					ds.addArguments((parameter.valueWasNull() || parameter.getValue() == null) ? new NullString() : parameter.getValue());
			//					ds.addArguments(parameter.getOp().equals(Operator.EQ) ? 1 : 0);
			//					ds.addArguments(groupParameter.getId());
			//					ps = ds.makePreparedStatement(c);
			//					ps.setQueryTimeout(60);
			//					ps.executeUpdate();
			//					LogContext.set(group.getUnittype(), group.getTopParent().getProfile(), xaps);
			//					logger.notice("Updated group parameter " + groupParameter.getName());
			//					if (xaps.getDbi() != null)
			//						xaps.getDbi().publishChange(groupParameter, group.getUnittype());
			//				}
			//			}
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

	public void addOrChangeGroupParameter(GroupParameter groupParameter, XAPS xaps) throws SQLException, NoAvailableConnectionException {
		Groups.checkPermission(group, xaps);
		//		if (groupParameter.getParameter().getUnittypeParameter().getFlag().isInspection())
		//			throw new IllegalArgumentException("The unit type parameter is an inspection parameter - cannot be set on a group");
		if (groupParameter.getId() == null) {
			// The group parameter may still already exist. This situation may occur if listparamsforexport is invoked by shell
			// and the setparam on that list is ran more than once. To avoid creating the same parameter over, we'll check
			// for group parameters with the exact same criteria. If found, then id is set in this object no further operation is done
			for (GroupParameter gp : getGroupParameters()) {
				Parameter p1 = gp.getParameter();
				Parameter p2 = groupParameter.getParameter();
				if (p1.getUnittypeParameter().getName().equals(p2.getUnittypeParameter().getName()) && p1.getOp() == p2.getOp() && p1.getValue().equals(p2.getValue())) {
					groupParameter.setId(gp.getId());
					return;
				}
			}
			
		}
		addOrChangeGroupParameterImpl(groupParameter, group, xaps);
		nameMap.put(groupParameter.getName(), groupParameter);
		idMap.put(groupParameter.getId(), groupParameter);
	}

	private void deleteGroupParameterImpl(GroupParameter groupParameter, Group group, XAPS xaps) throws SQLException, NoAvailableConnectionException {
		Statement s = null;
		String sql = null;
		//		if (!XAPSVersionCheck.groupSupported)
		//			return;
		Connection c = ConnectionProvider.getConnection(xaps.connectionProperties, true);
		SQLException sqlex = null;
		try {
			s = c.createStatement();
			sql = "DELETE FROM group_param WHERE ";
			sql += "id = " + groupParameter.getId();
			s.setQueryTimeout(60);
			s.executeUpdate(sql);
			logger.notice("Deleted group parameter " + groupParameter.getName());
			if (xaps.getDbi() != null)
				xaps.getDbi().publishDelete(groupParameter, group.getUnittype());
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
	 * method is run, the parameter is removed from the name- and id-Map.
	 * 
	 * @param groupParameter
	 * @throws NoAvailableConnectionException 
	 * @throws SQLException 
	 */
	public void deleteGroupParameter(GroupParameter groupParameter, XAPS xaps) throws SQLException, NoAvailableConnectionException {
		Groups.checkPermission(group, xaps);
		deleteGroupParameterImpl(groupParameter, group, xaps);
		nameMap.remove(groupParameter.getName());
		idMap.remove(groupParameter.getId());
	}

	/* Will only update the object model, not the database, used by XAPS.read() and Groups.refreshGroupParameters() */
	protected void addOrChangeGroupParameter(GroupParameter groupParameter) {
		nameMap.put(groupParameter.getName(), groupParameter);
		idMap.put(groupParameter.getId(), groupParameter);
	}

	/* Will only update the object model, not the database, used by Groups.refreshGroupParameters() */
	protected void deleteGroupParameter(GroupParameter groupParameter) {
		nameMap.remove(groupParameter.getName());
		idMap.remove(groupParameter.getId());
	}
}
