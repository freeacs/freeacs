package com.github.freeacs.shell.menu;

import com.github.freeacs.dbi.Message;
import com.github.freeacs.dbi.SyslogConstants;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.dbi.tr069.*;
import com.github.freeacs.dbi.tr069.TestCase.TestCaseMethod;
import com.github.freeacs.dbi.tr069.TestCaseParameter.TestCaseParameterType;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.shell.Context;
import com.github.freeacs.shell.Session;
import com.github.freeacs.shell.output.Heading;
import com.github.freeacs.shell.output.Line;
import com.github.freeacs.shell.output.Listing;
import com.github.freeacs.shell.output.OutputHandler;
import com.github.freeacs.shell.util.Validation;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

public class TestMenu {

	private Session session;
	private Context context;

	public TestMenu(Session session) {
		this.session = session;
		this.context = session.getContext();
	}

	public static void addOrChangeTestHistory(Unittype unittype, TestHistory testHistory) throws SQLException {
		TestDB testDB = new TestDB(unittype.getAcs());
		testDB.addOrChangeTestHistory(testHistory);
	}

	public static int deleteTestHistory(Unittype unittype, TestHistory filter) throws SQLException {
		TestDB testDB = new TestDB(unittype.getAcs());
		return testDB.deleteHistory(filter);
	}

	public static List<TestHistory> listTestHistory(Unittype unittype, TestHistory filter) throws SQLException {
		TestDB testDB = new TestDB(unittype.getAcs());
		return testDB.getHistory(filter);
	}

	public static int importDirectory(Unittype unittype, String directory) throws IOException, SQLException {
		TestDB testDB = new TestDB(unittype.getAcs());
		TestCaseFileHandler tcfh = new TestCaseFileHandler(unittype);
		List<TestCase> testCaseList = tcfh.parseDirectory(directory);
		for (TestCase tc : testCaseList)
			testDB.addOrChangeTestCase(tc);
		return testCaseList.size();
	}

	public static TestCase importFile(Unittype unittype, String filename) throws IOException, SQLException {
		TestDB testDB = new TestDB(unittype.getAcs());
		TestCaseFileHandler tcfh = new TestCaseFileHandler(unittype);
		TestCase tc = tcfh.parseFile(filename);
		testDB.addOrChangeTestCase(tc);
		return tc;
	}

	public static int exportTestCase(Unittype unittype, String directory, int testCaseId) throws IOException, SQLException {
		TestDB testDB = new TestDB(unittype.getAcs());
		TestCase tc = testDB.getTestCase(unittype, testCaseId);
		if (tc == null)
			return 0;
		TestCaseFileHandler tcfh = new TestCaseFileHandler(unittype);
		tcfh.toFile(directory, tc);
		return 1;
	}

	public static int exportTestCase(Unittype unittype, String directory, TestCaseMethod method, String paramFilter, String tagFilter) throws SQLException, IOException {
		TestDB testDB = new TestDB(unittype.getAcs());
		Collection<TestCase> testCases = testDB.getCompleteTestCases(unittype, method, paramFilter, tagFilter);
		TestCaseFileHandler tcfh = new TestCaseFileHandler(unittype);
		for (TestCase tc : testCases)
			tcfh.toFile(directory, tc);
		return testCases.size();
	}

	public static TestCase getTestCase(Unittype unittype, int testCaseId) throws SQLException {
		TestDB testDB = new TestDB(unittype.getAcs());
		return testDB.getTestCase(unittype, testCaseId);
	}

	public static List<TestCase> listTestCases(Unittype unittype, TestCaseMethod method, String paramFilter, String tagFilter) throws SQLException {
		TestDB testDB = new TestDB(unittype.getAcs());
		return testDB.getUncompleteTestCases(unittype, method, paramFilter, tagFilter);
	}

	public static Map<String, Integer> listTestCaseTags(Unittype unittype, TestCaseMethod method, String paramFilter, String tagFilter) throws SQLException {
		TestDB testDB = new TestDB(unittype.getAcs());
		List<TestCase> tcList = testDB.getUncompleteTestCases(unittype, method, paramFilter, tagFilter);
		Map<String, Integer> tagMap = new HashMap<String, Integer>();
		for (TestCase tc : tcList) {
			List<String> tagList = TestCase.getTagList(tc.getTags());
			for (String tag : tagList) {
				Integer i = tagMap.get(tag);
				if (i == null)
					i = new Integer(0);
				i = i + 1;
				tagMap.put(tag, i);
			}
		}
		return tagMap;
	}

	public static int deleteTestCases(Unittype unittype, TestCaseMethod method, String paramFilter, String tagFilter, Session session) throws SQLException {
		TestDB testDB = new TestDB(unittype.getAcs());
		List<TestCase> testCases = testDB.getUncompleteTestCases(unittype, method, paramFilter, tagFilter);
		int count = 0;
		for (TestCase tc : testCases) {
			count++;
			testDB.deleteTestCase(tc);
			if (count % 100 == 0)
				session.println("Deleted " + count + " Test Cases");
		}
		return testCases.size();
	}

	public static int deleteDuplicateTestCases(Unittype unittype) throws SQLException {
		return deleteDuplicateTestCasesImpl(unittype, TestCaseMethod.VALUE) + deleteDuplicateTestCasesImpl(unittype, TestCaseMethod.ATTRIBUTE);
	}

	private static int deleteDuplicateTestCasesImpl(Unittype unittype, TestCaseMethod method) throws SQLException {
		TestDB testDB = new TestDB(unittype.getAcs());
		Collection<TestCase> testCases = testDB.getCompleteTestCases(unittype, method, null, null);
		Map<Integer, TestCase> deleteMap = new HashMap<Integer, TestCase>();
		for (TestCase tc1 : testCases) {
			for (TestCase tc2 : testCases) {
				if (tc1.equals(tc2) && tc1.getId().intValue() != tc2.getId()) {
					if (tc1.getId().intValue() > tc2.getId())
						deleteMap.put(tc1.getId(), tc1);
					else
						deleteMap.put(tc2.getId(), tc2);
				}
			}
		}
		for (TestCase tc : deleteMap.values()) {
			testDB.deleteTestCase(tc);
		}
		return deleteMap.size();
	}

	public void validateFlags(OutputHandler oh) {
		Unittype unittype = context.getUnittype();
		TestDB testDB = new TestDB(unittype.getAcs());
		TR069DMParameterMap tr069DMMamp = testDB.getTr069DMMap();
		Listing listing = oh.getListing();
		listing.setHeading("Unittype Parameter Name", "Read-From-Device-Flag", "TR069-DM-Flag");
		for (UnittypeParameter utp : unittype.getUnittypeParameters().getUnittypeParameters()) {
			TR069DMParameter tr069Param = tr069DMMamp.getParameter(utp.getName());
			if (tr069Param != null) {
				if (utp.getFlag().isReadOnly() && !tr069Param.isReadOnly()) 
					listing.addLine(new Line(utp.getName(),utp.getFlag().getFlag(),"RW"));
				if (utp.getFlag().isReadWrite() && tr069Param.isReadOnly()) 
					listing.addLine(new Line(utp.getName(),utp.getFlag().getFlag(),"R"));
			}
		}
	}

	public void showtc(String[] args, OutputHandler oh) throws SQLException {
		Validation.numberOfArgs(args, 2);
		Integer id = Util.autoboxInteger(args[1]);
		if (id == null)
			throw new IllegalArgumentException("The id was not a number");
		TestDB testDB = new TestDB(session.getAcs());
		TestCase tc = testDB.getTestCase(context.getUnittype(), id);
		if (tc == null)
			throw new IllegalArgumentException("No Test Case was found");
		session.println(tc.toString());
	}

	public void exporttcdir(String args[]) throws SQLException, IOException {
		Validation.numberOfArgs(args, 2);

		TestCaseMethod method = null;
		String paramFilter = null;
		String tagFilter = null;
		if (args.length > 2) {
			if (!args[2].equals("NULL"))
				method = TestCaseMethod.valueOf(args[2]);
		}
		if (args.length > 3)
			paramFilter = Util.autoboxString(args[3]);
		if (args.length > 4)
			tagFilter = Util.autoboxString(args[4]);

		int exported = TestMenu.exportTestCase(context.getUnittype(), args[1], method, paramFilter, tagFilter);
		session.println("Exported " + exported + " Test Cases to " + args[1]);
	}

	public void exporttcfile(String[] args) throws IOException, SQLException {
		Validation.numberOfArgs(args, 3);
		Integer id = Util.autoboxInteger(args[2]);
		if (id == null)
			throw new IllegalArgumentException("The id was not a number");
		int exported = TestMenu.exportTestCase(context.getUnittype(), args[1], id);
		if (exported == 0)
			throw new IllegalArgumentException("The test case Id was not found");
		else
			session.println("Exported test case " + id);
	}

	public void generatetc(String args[]) throws Exception {
		TestDB testDB = new TestDB(session.getAcs());
		TestGenerator tg = new TestGenerator(TR069DMLoader.load(), context.getUnittype());
		List<TestCase> generatedTc = null;
		if (args.length > 1) {
			String arg = args[1];
			if (arg.equals("VALUE") || arg.equals("ATTRIBUTE")) {
				generatedTc = tg.generate(arg);
			} else {
				Integer testCaseId = Util.autoboxInteger(args[1]);
				if (testCaseId != null) {
					TestCase masterTc = testDB.getTestCase(context.getUnittype(), testCaseId);
					generatedTc = tg.generateSetTcListFromMasterTc(masterTc);
				} else {
					session.println("The Test Case id " + testCaseId + " was not valid/recognized");
				}
			}
		} else {
			generatedTc = tg.generate(null);
		}
		int count = 0;
		for (TestCase tc : generatedTc) {
			count++;
			testDB.addOrChangeTestCase(tc);
			if (count % 100 == 0)
				session.println("Generated " + count + " Test Cases");
		}
		session.println("Generated " + generatedTc.size() + " Test Cases");
	}

	public void importtcdir(String args[]) throws IOException, SQLException {
		Validation.numberOfArgs(args, 2);
		int imported = TestMenu.importDirectory(context.getUnittype(), args[1]);
		session.println("Imported " + imported + " Test Cases from " + args[1]);
	}

	public void importtcfile(String args[]) throws IOException, SQLException {
		Validation.numberOfArgs(args, 2);
		TestCase tc = TestMenu.importFile(context.getUnittype(), args[1]);
		session.println("File " + args[1] + " was imported with id " + tc.getId());
	}

	public void listtctags(String args[], OutputHandler oh) throws SQLException {
		TestCaseMethod method = null;
		String paramFilter = null;
		String tagFilter = null;
		if (args.length > 1) {
			if (!args[1].equals("NULL"))
				method = TestCaseMethod.valueOf(args[1]);
		}
		if (args.length > 2)
			paramFilter = Util.autoboxString(args[2]);
		if (args.length > 3)
			tagFilter = Util.autoboxString(args[3]);
		Map<String, Integer> tagMap = TestMenu.listTestCaseTags(context.getUnittype(), method, paramFilter, tagFilter);
		Listing listing = oh.getListing();
		listing.setHeading("Tag", "TestCases");
		for (Entry<String, Integer> entry : tagMap.entrySet()) {
			if (entry.getValue() > 1)
				listing.addLine(entry.getKey(), "" + entry.getValue());
		}

	}

	public void listtc(String args[], OutputHandler oh) throws SQLException {
		TestCaseMethod method = null;
		String paramFilter = null;
		String tagFilter = null;
		if (args.length > 1) {
			if (!args[1].equals("NULL"))
				method = TestCaseMethod.valueOf(args[1]);
		}
		if (args.length > 2)
			paramFilter = Util.autoboxString(args[2]);
		if (args.length > 3)
			tagFilter = Util.autoboxString(args[3]);

		List<TestCase> testCaseList = TestMenu.listTestCases(context.getUnittype(), method, paramFilter, tagFilter);
		Listing listing = oh.getListing();
		listing.setHeading("Id", "Method", "Tags", "ExpectError", "Parameter/File");

		for (TestCase tc : testCaseList) {
			Line line = new Line();
			line.addValue(tc.getId());
			line.addValue(tc.getMethod().toString());
			line.addValue(tc.getTags());
			if (tc.getExpectError() == null)
				line.addValue("NULL");
			else
				line.addValue(tc.getExpectError() ? "true" : "false");
			for (TestCaseParameter tcp : tc.getParams()) {
				if (tcp.getType() != TestCaseParameterType.FAC) {
					line.addValue("Parameter: " + tcp.getUnittypeParameter().getName());
					break;
				}
			}
			if (tc.getFiles() != null) {
				if (tc.getFiles().getInputFile() != null)
					line.addValue("Inputfile: " + tc.getFiles().getInputFile().getName());
			}
			listing.addLine(line);
		}
	}

	/** TR069 Test Case methods 
	 *
	 * @throws SQLException */
	public void deltc(String args[]) throws SQLException {
		TestCaseMethod method = null;
		String paramFilter = null;
		String tagFilter = null;
		if (args.length > 1) {
			if (!args[1].equals("NULL"))
				method = TestCaseMethod.valueOf(args[1]);
		}
		if (args.length > 2)
			paramFilter = Util.autoboxString(args[2]);
		if (args.length > 3)
			tagFilter = Util.autoboxString(args[3]);
		int deleted = TestMenu.deleteTestCases(context.getUnittype(), method, paramFilter, tagFilter, session);
		session.println("Deleted " + deleted + " Test Cases");
	}

	public void deltcduplicates(String args[]) throws SQLException {
		int deleted = TestMenu.deleteDuplicateTestCases(context.getUnittype());
		session.println("Deleted " + deleted + " duplicate Test Cases");
	}

	public void deltesthistory(String args[]) throws SQLException {
		Date startTms = null;
		String unitId = null;
		if (context.getUnit() != null)
			unitId = context.getUnit().getId();
		if (args.length > 1)
			startTms = Util.autoboxDate(args[1]);
		TestHistory th = new TestHistory(context.getUnittype(), startTms, unitId, null, null);
		if (args.length > 2)
			th.setEndTimestamp(Util.autoboxDate(args[2]));
		TestMenu.deleteTestHistory(context.getUnittype(), th);
	}

	public void listTestHistory(String[] args, OutputHandler oh) throws SQLException {

		TestHistory filter = new TestHistory();
		for (int i = 1; i < args.length; i++) {
			String[] cmdParts = args[i].split("=", 2);
			if (cmdParts.length != 2)
				continue;
			String cmd = cmdParts[0].toLowerCase();
			if (cmd.contains("starttms")) {
				filter.setStartTimestamp(Util.autoboxDate(cmdParts[1]));
			} else if (cmd.contains("endtms")) {
				filter.setEndTimestamp(Util.autoboxDate(cmdParts[1]));
			} else if (cmd.contains("result")) {
				filter.setResult(cmdParts[1]);
			} else if (cmd.contains("expecterror")) {
				filter.setExpectError(Boolean.parseBoolean(cmdParts[1]));
			} else if (cmd.contains("failed")) {
				filter.setFailed(Boolean.parseBoolean(cmdParts[1]));
			}
		}
		List<TestHistory> testHistoryList = TestMenu.listTestHistory(context.getUnittype(), filter);

		if (context.getUnit() != null)
			filter.setUnitId(context.getUnit().getId());
		Listing listing = oh.getListing();
		Line headingLine = new Line("Start", "End", "TestCase", "Unitid", "Failed", "ExpectError", "Result");
		listing.setHeading(new Heading(headingLine));
		for (TestHistory th : testHistoryList) {
			Line line = new Line();
			line.addValue(Util.outputFormatExtended.format(th.getStartTimestamp()));
			if (th.getEndTimestamp() != null)
				line.addValue(Util.outputFormatExtended.format(th.getEndTimestamp()));
			else
				line.addValue("N/A");
			line.addValue(th.getTestCaseId());
			line.addValue(th.getUnitId());
			line.addValue(th.getFailed() ? "true" : "false");
			line.addValue(th.getExpectError() ? "true" : "false");
			if (th.getResult() != null) { // splits the result over several lines, to avoid very long lines 
				String[] resultArray = th.getResult().split("\n");
				for (int i = 0; i < resultArray.length; i++) {
					if (i > 0)
						line = new Line("", "", "", "", "", "");
					line.addValue(resultArray[i]);
					listing.addLine(line);
				}
			} else
				listing.addLine(line);
		}
	}

	public void testModeChange(boolean enable) throws SQLException {
		if (enable) {
			session.getAcsUnit().addOrChangeUnitParameter(context.getUnit(), SystemParameters.TEST_ENABLE, "1");
			session.println("Test is enabled - you may initiate connection from device by sending a 'kick' command");
		} else {
			session.getAcsUnit().addOrChangeUnitParameter(context.getUnit(), SystemParameters.TEST_ENABLE, "0");
			// will be read by MessageListenerTask in TR-069 Server - the change will be picked up by the TR-069 server within max 11 seconds (average 5-6 sec)
			session.getDbi().publishMessage("N/A", Message.MTYPE_PUB_TR069_TEST_END, Message.OTYPE_UNIT, context.getUnit().getId(), SyslogConstants.FACILITY_TR069);
			session.println("Test is disabled - the TR-069 server will be notified within 10 seconds");
		}
	}

}
