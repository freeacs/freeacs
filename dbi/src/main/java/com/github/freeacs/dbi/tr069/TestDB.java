package com.github.freeacs.dbi.tr069;

import com.github.freeacs.dbi.*;
import com.github.freeacs.dbi.InsertOrUpdateStatement.Field;
import com.github.freeacs.dbi.tr069.TestCaseParameter.TestCaseParameterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * The class contains all SQL for the TR069-test related tables
 * 
 * @author Morten
 * 
 */
public class TestDB {

	private DataSource dataSource;
	private ACS acs;
	private TR069DMParameterMap tr069DMMap;
	private static Logger logger = LoggerFactory.getLogger(TestDB.class);

	public TestDB(ACS acs) {
		try {
			if (this.tr069DMMap == null)
				this.tr069DMMap = TR069DMLoader.load();
		} catch (Exception e) {
			throw new RuntimeException("Could not load TR069 Datamodel");
		}
		this.dataSource = acs.getConnectionProperties();
		this.acs = acs;
	}
	

	public void deleteTestCase(TestCase tc) throws SQLException {
		boolean wasAutoCommit = false;
		Connection c = dataSource.getConnection();
		wasAutoCommit = c.getAutoCommit();
		c.setAutoCommit(false);
		PreparedStatement ps = null;
		SQLException sqle = null;
		try {
			DynamicStatement ds = new DynamicStatement();
			ds.addSqlAndArguments(
					"DELETE FROM test_case_param WHERE case_id = ?", tc.getId());
			ps = ds.makePreparedStatement(c);
			ps.executeUpdate();
			ps.close();

			TestCaseFiles tcf = tc.getFiles();
			if (tcf != null) {
				Files files = tcf.getInputFile().getUnittype().getFiles();
				ACS acs = tcf.getInputFile().getUnittype().getAcs();
				files.deleteFile(tcf.getInputFile(), acs);
				files.deleteFile(tcf.getOutputFile(), acs);
			}
			
			ds = new DynamicStatement();
			ds.addSqlAndArguments("DELETE FROM test_case_files WHERE case_id = ?", tc.getId());
			ps = ds.makePreparedStatement(c);
			ps.executeUpdate();
			ps.close();
			
			ds = new DynamicStatement();
			ds.addSqlAndArguments("DELETE FROM test_case WHERE id = ?", tc.getId());
			ps = ds.makePreparedStatement(c);
			ps.executeUpdate();
			ps.close();

		} catch (SQLException sqlex) {
			sqle = sqlex;
			throw sqlex;
		} finally {
			if (ps != null)
				ps.close();
			c.setAutoCommit(wasAutoCommit);
			c.close();
		}

	}

	public TestCase addOrChangeTestCase(TestCase tc) throws SQLException{
		boolean wasAutoCommit = false;
		Connection c = dataSource.getConnection();
		wasAutoCommit = c.getAutoCommit();
		c.setAutoCommit(false);
		DynamicStatement ds;
		PreparedStatement ps1 = null, ps2 = null;
		SQLException sqle = null;
		ResultSet gk = null;
		try {
			
			// Insert a new test case, or update an existing one.
			InsertOrUpdateStatement ious = new InsertOrUpdateStatement(
					"test_case", new Field("id", tc.getId()));
			ious.addField(new Field("unit_type_id", tc.getUnittype().getId()));
			ious.addField(new Field("method", tc.getMethod().toString()));
			ious.addField(new Field("tag", tc.getTags()));
			Integer expectError = null;
			if (tc.getExpectError() != null)
				expectError = tc.getExpectError() ? 1 : 0;
			ious.addField(new Field("expect_error", expectError));
			ps1 = ious.makePreparedStatement(c);
			int rowsUpdated = ps1.executeUpdate();
			if (rowsUpdated == 0 && !ious.isInsert()) {
				ious.forceInsert();
				ps1 = ious.makePreparedStatement(c);
				ps1.executeUpdate();
			} else if (ious.isInsert()) {
				gk = ps1.getGeneratedKeys();
				if (gk.next()) {
					tc.setId(gk.getInt(1));
					ds = new DynamicStatement();
					ds.addSql("UPDATE test_case SET tag = ? WHERE id = ? LIMIT 1");
					ds.addArguments(tc.getTags() + "[ID-" + tc.getId() + "]", tc.getId());
					ps2 = ds.makePreparedStatement(c);
					ps2.execute();
					ps2.close();
				}
				gk.close();
			}
			ps1.close();

			// In the event that the test case ID had related entries in the
			// test_case_param table, we delete these since we can no longer
			// guarantee that their ID reference is relevant.
			ds = new DynamicStatement();
			ds.addSqlAndArguments(
					"DELETE FROM test_case_param WHERE case_id = ?", tc.getId());
			ps1 = ds.makePreparedStatement(c);
			ps1.executeUpdate();
			ps1.close();

			// Similar with the test_case_files table.
			ds = new DynamicStatement();
			ds.addSqlAndArguments(
					"DELETE FROM test_case_files WHERE case_id = ?", tc.getId());
			ps1 = ds.makePreparedStatement(c);
			ps1.executeUpdate();
			ps1.close();
			
			// Instead, we re-add the test case parameters. This ensures their
			// ID reference gets updated properly.
			for (TestCaseParameter tcp : tc.getParams()) {
				InsertOrUpdateStatement iousParam = new InsertOrUpdateStatement(
						"test_case_param", new Field("id", tcp.getId()));
				iousParam.addField(new Field("case_id", tc.getId()));
				iousParam.addField(new Field("type", tcp.getType().toString()));
				iousParam.addField(new Field("unit_type_param_id", tcp
						.getUnittypeParameter().getId()));
				iousParam.addField(new Field("value", tcp.getValue()));
				iousParam.addField(new Field("notification", tcp
						.getNotification()));
				ps1 = iousParam.makePreparedStatement(c);
				ps1.executeUpdate();
				gk = ps1.getGeneratedKeys();
				if (gk.next())
					tcp.setId(gk.getInt(1));
				gk.close();
				ps1.close();
			}
			
			// Same thing as above with test_case_files.
			if (tc.getFiles() != null) {
				ious = new InsertOrUpdateStatement("test_case_files",
						new Field("id", tc.getFiles().getId()));
				ious.addField(new Field("case_id", tc.getId()));
				ious.addField(new Field("input_file_id", tc.getFiles()
						.getInputFile().getId()));
				if (tc.getFiles().getOutputFile() != null)
					ious.addField(new Field("output_file_id", tc.getFiles()
							.getOutputFile().getId()));
				ps1 = ious.makePreparedStatement(c);
				rowsUpdated = ps1.executeUpdate();
				ps1.close();
			}
			return tc;
		} catch (SQLException sqlex) {
			sqle = sqlex;
			throw sqlex;
		} finally {
			if (ps1 != null)
				ps1.close();
			c.setAutoCommit(wasAutoCommit);
			c.close();
		}
	}

	public TestCase getTestCase(Unittype unittype, int testCaseId) throws SQLException {
		Connection c = dataSource.getConnection();
		PreparedStatement ps = null;
		SQLException sqle = null;
		ResultSet rs = null;
		Map<Integer, TestCase> map = new HashMap<Integer, TestCase>();
		try {
			DynamicStatement ds = new DynamicStatement();
			
			ds.addSql("SELECT tc.id, tc.method, tc.tag, tc.expect_error, tcp.type," +
					" tcp.unit_type_param_id, tcp.value, tcp.notification, tcf.input_file_id," +
					" tcf.output_file_id, tcp.id FROM" +
					" (test_case tc  LEFT JOIN test_case_param tcp  ON tc.id = tcp.case_id)" +
					" LEFT JOIN  test_case_files tcf ON  tc.id = tcf.case_id WHERE tc.id = ?" +
					" AND tc.unit_type_id = ? ORDER BY tc.id ASC");
			ds.addArguments(testCaseId, unittype.getId());
			
			ps = ds.makePreparedStatement(c);
			rs = ps.executeQuery();
			while (rs.next()) {
				int tcId = rs.getInt("tc.id");
				TestCase tc = map.get(tcId);
				if (tc == null) {
					String tags = rs.getString("tc.tag");
					String expectErrorStr = rs.getString("expect_error");
					Boolean expectError = null;
					if (expectErrorStr != null)
						expectError = expectErrorStr.equals("1") ? true : false;
					tc = new TestCase(unittype, TestCase.TestCaseMethod.valueOf(rs.getString("tc.method")), tags, expectError);
					tc.setId(tcId);
					map.put(tcId, tc);
				}
				int tcpId = rs.getInt("tcp.id");
				if (tcpId != 0) {
					TestCaseParameterType tcpType = TestCaseParameterType.valueOf(rs.getString("tcp.type"));
					UnittypeParameter utp = unittype.getUnittypeParameters().getById(rs.getInt("tcp.unit_type_param_id"));
					String value = rs.getString("tcp.value");
					int notification = rs.getInt("tcp.notification");
					TR069DMParameter dmp = tr069DMMap.getParameter(utp.getName());
					TestCaseParameter tcp = new TestCaseParameter(tcpType, utp, dmp, value, notification);
					tcp.setId(tcpId);
					tc.getParams().add(tcp);
				}
				int inputFileId = rs.getInt("input_file_id");
				if (inputFileId != 0) {
					if (tc.getFiles() == null) {
						tc.setFiles(new TestCaseFiles());
					}
					tc.getFiles().setInputFile(unittype.getFiles().getById(inputFileId));
					int outputFileId = rs.getInt("output_file_id");
					if (outputFileId != 0) {
						tc.getFiles().setOutputFile(unittype.getFiles().getById(outputFileId));
					}
				}
				
			}
			if (map.size() == 0)
				return null;
			else
				return map.get(testCaseId);
		} catch (SQLException sqlex) {
			sqle = sqlex;
			throw sqlex;
		} finally {
			if (ps != null)
				ps.close();

		}

	}

	/**
	 * This method is only used by TR-069 server to get a list of complete
	 * TestCases - that is test cases with all test case parameters. The method
	 * is slow, since it must fire one SQL for each test case in the list
	 * 
	 * @param unittype
	 * @param method
	 * @param paramFilter
	 * @param tagFilter
	 * @return
	 * @throws SQLException
	 *
	 */
	public List<TestCase> getCompleteTestCases(Unittype unittype,
                                               TestCase.TestCaseMethod method, String paramFilter, String tagFilter)
			throws SQLException {
		List<TestCase> testCaseList = getUncompleteTestCases(unittype, method,
				paramFilter, tagFilter);
		List<TestCase> retList = new ArrayList<TestCase>();
		for (TestCase tc : testCaseList) {
			retList.add(getTestCase(unittype, tc.getId()));
		}
		return retList;
	}

	/**
	 * This method will not return complete Test Case objects, since not all
	 * test case parameters are found. The method is useful to produce a list of
	 * test cases available for the given filters.
	 * 
	 * @param unittype
	 * @param method
	 * @param paramFilter
	 * @param tagFilter
	 * @return
	 * @throws SQLException
	 *
	 */
	public List<TestCase> getUncompleteTestCases(Unittype unittype,
                                                 TestCase.TestCaseMethod method, String paramFilter, String tagFilter)
			throws SQLException {
		Connection c = dataSource.getConnection();
		PreparedStatement ps = null;
		SQLException sqle = null;
		ResultSet rs = null;
		Map<Integer, TestCase> map = new HashMap<Integer, TestCase>();
		try {
			DynamicStatement ds = new DynamicStatement();
			
			ds.addSqlAndArguments("SELECT * " +
//					"tc.id, tc.method, tc.tag, tc.expect_error, tcp.id, tcp.type, " +
//					"tcp.unit_type_param_id, utp.name, tcf.input_file_id, tcf.output_file_id " +
					"FROM ((test_case tc " +
					"LEFT JOIN test_case_param tcp " +
					"ON tc.id = tcp.case_id) " +
					"LEFT JOIN unit_type_param utp " +
					"ON tcp.unit_type_param_id = utp.unit_type_param_id) " +
					"LEFT JOIN test_case_files tcf " +
					"ON tc.id = tcf.case_id " +
					"WHERE tc.unit_type_id = ? ", unittype.getId());
//					"GROUP BY tcp.case_id");
			
//			ds.addSql("SELECT * FROM" +
//					" (test_case tc LEFT JOIN" +
//					" (test_case_param tcp LEFT JOIN unit_type_param utp ON" +
//					" tcp.unit_type_param_id = utp.unit_type_param_id)" +
//					" ON tc.id = tcp.case_id" +
//					" AND tc.unit_type_id = 49)" +
//					" LEFT JOIN" +
//					" (test_case_files tcf JOIN filestore fs ON tcf.input_file_id = fs.id)" +
//					" ON tc.id = tcf.case_id" +
//					" WHERE true "); //ugly hack to put the following AND-clauses after a WHERE instead of a ON.

			if (paramFilter != null)
				ds.addSqlAndArguments("AND utp.name LIKE ? ", "%" + paramFilter + "%");
			if (tagFilter != null) {
				List<String> tags = TestCase.getTagList(tagFilter);
				for (String tag : tags) 
					ds.addSqlAndArguments("AND tc.tag LIKE ? ", "%[" + tag + "]%");
			}
//			ds.addSqlAndArguments("AND tc.unit_type_id = ? ", unittype.getId());
			if (method != null)
				ds.addSqlAndArguments("AND tc.method = ? ", method.toString());
			ds.addSql("GROUP BY tcp.case_id ");
			
			ds.addSql("ORDER BY tc.id ASC");
			
			ps = ds.makePreparedStatement(c);
			logger.debug(ds.getDebugMessage());
			
			rs = ps.executeQuery();
			while (rs.next()) {
				int tcId = rs.getInt("tc.id");
				TestCase tc = map.get(tcId);
				if (tc == null) {
					String tags = rs.getString("tc.tag");
					String expectErrorStr = rs.getString("expect_error");
					Boolean expectError = null;
					if (expectErrorStr != null)
						expectError = expectErrorStr.equals("1") ? true : false;
					tc = new TestCase(unittype, TestCase.TestCaseMethod.valueOf(rs
							.getString("tc.method")), tags, expectError);
					tc.setId(tcId);
					map.put(tcId, tc);
				}
				int tcpId = rs.getInt("tcp.id");
				if (tcpId != 0) {
					TestCaseParameterType tcpType = TestCaseParameterType
							.valueOf(rs.getString("tcp.type"));
					UnittypeParameter utp = unittype.getUnittypeParameters()
							.getById(rs.getInt("tcp.unit_type_param_id"));
					String value = rs.getString("tcp.value");
					int notification = rs.getInt("tcp.notification");
					TR069DMParameter dmp = tr069DMMap.getParameter(utp.getName());
					TestCaseParameter tcp = new TestCaseParameter(tcpType, utp,
							dmp, value, notification);
					tcp.setId(tcpId);
					tc.getParams().add(tcp);
				}
				int tcfId = rs.getInt("tcf.id");
				if (tcfId != 0) {
					TestCaseFiles tcFiles = new TestCaseFiles();
					tcFiles.setId(rs.getInt("tcf.id"));
					tcFiles.setInputFile(unittype.getFiles().getById(rs.getInt("tcf.input_file_id")));
					tcFiles.setOutputFile(unittype.getFiles().getById(rs.getInt("tcf.output_file_id")));
					tc.setFiles(tcFiles);
				}
			}
			return new ArrayList<TestCase>(map.values());
		} catch (SQLException sqlex) {
			sqle = sqlex;
			throw sqlex;
		} finally {
			if (ps != null)
				ps.close();

		}
	}

	public void addOrChangeTestHistory(TestHistory history)
			throws SQLException {
		Connection c = dataSource.getConnection();
		PreparedStatement ps = null;
		SQLException sqle = null;
		ResultSet gk = null;
		try {
			InsertOrUpdateStatement ious = new InsertOrUpdateStatement(
					"test_history", new Field("id", history.getId()));
			ious.addField(new Field("unit_type_id", history.getUnittype()
					.getId()));
			ious.addField(new Field("unit_id", history.getUnitId()));
			ious.addField(new Field("case_id", history.getTestCaseId()));
			ious.addField(new Field("start_timestamp", history
					.getStartTimestamp()));
			if (history.getEndTimestamp() != null)
				ious.addField(new Field("end_timestamp", history
						.getEndTimestamp()));
			if (history.getFailed() == null)
				ious.addField(new Field("failed", 0));
			else
				ious.addField(new Field("failed", history.getFailed()));
			if (history.getExpectError() == null)
				ious.addField(new Field("expect_error", false));
			else
				ious.addField(new Field("expect_error", history
						.getExpectError()));
			String result = history.getResult();
			if (history.getResult() != null
					&& history.getResult().length() > 4096) {
				result = result.substring(0, 4090) + "...";
			}
			ious.addField(new Field("result", result));
			ps = ious.makePreparedStatement(c);
			ps.executeUpdate();
			if (ious.isInsert()) {
				gk = ps.getGeneratedKeys();
				if (gk.next())
					history.setId(gk.getInt(1));
				gk.close();
			}
		} catch (SQLException sqlex) {
			sqle = sqlex;
			throw sqlex;
		} finally {
			if (ps != null)
				ps.close();

		}
	}

	public int deleteHistory(TestHistory filter) throws SQLException {
		Connection c = dataSource.getConnection();
		PreparedStatement ps = null;
		SQLException sqle = null;
		try {
			DynamicStatement ds = new DynamicStatement();
			ds.addSqlAndArguments("DELETE FROM test_history WHERE ");
			if (filter.getId() != null)
				ds.addSqlAndArguments("id = ? AND ", filter.getId());
			if (filter.getUnittype() != null)
				ds.addSqlAndArguments("unit_type_id = ? AND ", filter
						.getUnittype().getId());
			if (filter.getFailed() != null) {
				int failedInt = (filter.getFailed() ? 1 : 0);
				ds.addSqlAndArguments("failed = ? AND ", failedInt);
			}
			if (filter.getExpectError() != null) {
				int expectErrorInt = (filter.getExpectError() ? 1 : 0);
				ds.addSqlAndArguments("expect_error = ? AND ", expectErrorInt);

			}
			if (filter.getStartTimestamp() != null)
				ds.addSqlAndArguments("start_timestamp >= ? AND ",
						filter.getStartTimestamp());
			if (filter.getEndTimestamp() != null)
				ds.addSqlAndArguments("start_timestamp <= ? AND ",
						filter.getEndTimestamp());
			if (filter.getTestCaseId() != null)
				ds.addSqlAndArguments("case_id = ? AND ",
						filter.getTestCaseId());
			if (filter.getUnitId() != null)
				ds.addSqlAndArguments("unit_id = ? AND ", filter.getUnitId());
			ds.cleanupSQLTail();
			ps = ds.makePreparedStatement(c);
			return ps.executeUpdate();
		} catch (SQLException sqlex) {
			sqle = sqlex;
			throw sqlex;
		} finally {
			if (ps != null)
				ps.close();
		}

	}

	public List<TestHistory> getHistory(TestHistory filter)
			throws SQLException {
		Connection c = dataSource.getConnection();
		PreparedStatement ps = null;
		SQLException sqle = null;
		ResultSet rs = null;
		try {
			DynamicStatement ds = new DynamicStatement();
			ds.addSql("SELECT * FROM test_history WHERE ");
			if (filter.getId() != null)
				ds.addSqlAndArguments("id = ? AND ", filter.getId());
			if (filter.getUnittype() != null)
				ds.addSqlAndArguments("unit_type_id = ? AND ", filter
						.getUnittype().getId());
			if (filter.getFailed() != null) {
				int failedInt = (filter.getFailed() ? 1 : 0);
				ds.addSqlAndArguments("failed = ? AND ", failedInt);
			}
			if (filter.getExpectError() != null) {
				int expectErrorInt = (filter.getExpectError() ? 1 : 0);
				ds.addSqlAndArguments("expect_error = ? AND ", expectErrorInt);

			}
			if (filter.getStartTimestamp() != null)
				ds.addSqlAndArguments("start_timestamp >= ? AND ",
						filter.getStartTimestamp());
			if (filter.getEndTimestamp() != null)
				ds.addSqlAndArguments("start_timestamp <= ? AND ",
						filter.getEndTimestamp());
			if (filter.getTestCaseId() != null)
				ds.addSqlAndArguments("case_id = ? AND ",
						filter.getTestCaseId());
			if (filter.getUnitId() != null)
				ds.addSqlAndArguments("unit_id = ? AND ", filter.getUnitId());
			if (filter.getResult() != null)
				ds.addSqlAndArguments("result LIKE ? AND ", filter.getResult());
			
			ds.cleanupSQLTail();
			ps = ds.makePreparedStatement(c);
			rs = ps.executeQuery();
			List<TestHistory> historyList = new ArrayList<TestHistory>();
			while (rs.next()) {
				String unitId = rs.getString("unit_id");
				Integer testCaseId = rs.getInt("case_id");
				Integer unittypeId = rs.getInt("unit_type_id");
				Unittype unittype = acs.getUnittype(unittypeId);
				boolean expectError = rs.getInt("expect_error") == 1 ? true
						: false;
				if (unittype == null)
					continue; // no access to this unittype with this user
				Date startTimestamp = rs.getTimestamp("start_timestamp");
				TestHistory th = new TestHistory(unittype, startTimestamp,
						unitId, testCaseId, expectError);
				th.setId(rs.getInt("id"));
				th.setEndTimestamp(rs.getTimestamp("end_timestamp"));
				th.setFailed(rs.getInt("failed") == 1 ? true : false);
				th.setResult(rs.getString("result"));
				historyList.add(th);
			}
			return historyList;
		} catch (SQLException sqlex) {
			sqle = sqlex;
			throw sqlex;
		} finally {
			if (ps != null)
				ps.close();

		}
	}

	public TR069DMParameterMap getTr069DMMap() {
		return tr069DMMap;
	}

}
