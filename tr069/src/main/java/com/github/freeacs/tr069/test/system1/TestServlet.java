package com.github.freeacs.tr069.test.system1;

import com.github.freeacs.tr069.test.system1.html.Element;
import com.github.freeacs.tr069.test.system1.html.InputRadioMaker;
import com.github.freeacs.tr069.test.system1.html.SelectMaker;
import com.github.freeacs.tr069.test.system1.html.TextAreaElement;
import com.github.freeacs.common.util.FileDatabase;
import com.github.freeacs.common.util.NaturalComparator;
import com.github.freeacs.tr069.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;

public class TestServlet extends HttpServlet {

	private static final long serialVersionUID = 847405010989042998L;

	public static String getContent(File f, String deviceType) throws IOException {
		FileReader fr = new FileReader(f);
		char[] contentArr = new char[100000];
		fr.read(contentArr);
		String content = new String(contentArr);
		int endOfXmlPos = content.indexOf("</cwmp");
		endOfXmlPos = content.indexOf(">", endOfXmlPos) + 1;
		content = content.substring(0, endOfXmlPos);
		String xml = content.substring(content.indexOf("<cwmp"));
		for (int i = 1; i < 100; i++) {
			if (xml.indexOf("$VAR" + i) > -1) {
				int varPos = content.indexOf(deviceType + i);
				int eqPos = content.indexOf("=", varPos);
				int nlPos = content.indexOf("\n", eqPos);
				String var = content.substring(eqPos + 1, nlPos).trim();
				xml = xml.replace("$VAR" + i, var);
			} else
				break;
		}
		fr.close();
		return xml;
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doGet(req, res);
	}

	private static void intro(HttpServletRequest req, Element body) {
		body.add("The xAPS Test Verfication Process is a really simple way of testing your TR-069 device and at ");
		body.add("the same time make sure it is interoperable with xAPS. The tests are taken from PD-128v9, which ");
		body.add("is the test document used at TR-069 plugfests. However, only the most basic tests are part of ");
		body.add("of this process; none of the real-world tests are run, which shows that the emphasis in this ");
		body.add("is to get a decent level of interoperability, not to really test if the device can be used for ");
		body.add("all kinds of business purposes. To get a \"complete\" status you need to sucessfully complete all ");
		body.add("the tests.");
		body.p();

		body.add("<b>How to proceed:</b> Connect your CPE to " + req.getRequestURL().substring(0, req.getRequestURL().length() - 5) + " ");
		body.add("using the same ACS username as you have entered (or are about to enter) below. The ACS will then ");
		body.add("accept both your ACS username and password and immediately respond with the first test. You then ");
		body.add("decide which tests to run. If you so choose, you can set all tests to be run automatically, to ");
		body.add("quickly go through all tests. Just remember to lower your periodic inform interval. ");
		body.add("If your device is not an IAD, change this setting to either VoIP (ATA) or SetTopBox (STB).");
		body.p();
	}

	private TestDatabaseObject input(HttpServletRequest req, Element body, String username) throws Exception {
		KillDatabase.refresh();
		Element form = body.form("test", "post", "form1");
		Element table = form.table();

		// Refresh
		Element tr = table.tr();
		tr.td();
		tr.td();
		tr.td().input("refresh", "submit", "Refresh page");

		// ACS username
		tr = table.tr();
		tr.td("ACS username:");
		String row = TestDatabase.database.select(username);
		TestDatabaseObject tdo = null;
		Element td = tr.td();
		td.add(username);
		td.input("username", "hidden", username);
		if (row != null)
			tdo = new TestDatabaseObject(row);
		else
			tdo = new TestDatabaseObject();
		tr.td().input("logout", "submit", "Logout");

		// Reset everything
		if (req.getParameter("test-reset") != null) {
			tdo = new TestDatabaseObject();
			File dir = getFileFromResource("tests/results");
			File[] files = dir.listFiles();
			for (File file : files) {
				int pos = file.getName().indexOf(username);
				String restOfName = file.getName().substring(pos + username.length() + 1);
				File f = getFileFromResource("tests/" + restOfName);
				if (f.exists() && file.getName().startsWith(username)) {
					file.delete();
				}
			}
			String killDboStr = KillDatabase.database.select(username);
			if (killDboStr != null) {
				KillDatabaseObject kdo = new KillDatabaseObject(killDboStr);
				kdo.setGpnFile(null);
				kdo.setGpvFile(null);
				kdo.setParamIndex(0);
				kdo.setTestIndex(0);
				kdo.setTestRunning(false);
				KillDatabase.database.insert(username, kdo.toString());
			}
		}

		// Test run
		tr = table.tr();
		tr.td("Test process active:");
		if (req.getParameter("run-update") != null)
			tdo.setRun(req.getParameter("run"));
		tr.td().add(tdo.getRun());
		if (tdo.getRun().equals("false")) {
			tr.td().input("run-update", "submit", "Start process");
			tr.td().input("run", "hidden", "true");
		} else {
			tr.td().input("run-update", "submit", "Stop process");
			tr.td().input("run", "hidden", "false");
		}

		// Test type
		InputRadioMaker irm = new InputRadioMaker("testtype");
		irm.addButton("Auto", "Auto");
		irm.addButton("Manual", "Manual");
		irm.addButton("Kill", "Paramstest");
		if (req.getParameter("testtype-update") != null) {
			tdo.setTestType(req.getParameter("testtype"));
			if (req.getParameter("testtype").equals("Auto"))
				tdo.setStep(tdo.getLastStep());
			if (req.getParameter("testtype").equals("Kill")) {
				tdo.setStep("Kill");
				String killRow = KillDatabase.database.select(username);
				if (killRow == null) {
					KillDatabase.database.insert(username, new KillDatabaseObject().toString());
				}

			}
		}
		irm.setCheckedValue(tdo.getTestType());
		tr = table.tr();
		tr.td("Test type:");
		tr.td().add(irm);
		tr.td().input("testtype-update", "submit", "Change Test Type");

		if (tdo.getStep().equals("Kill")) {
			tr = table.tr();
			tr.td();
			td = tr.td();
			td.add("The params test will run automatically.<br>Results will be published in these files on the server:");
			td.add("<br><ul>kill-report-" + username + ".txt</ul><ul>kill-report-details-" + username + ".txt</ul><br>");
		} else {

			// Device type
			irm = new InputRadioMaker("devicetype");
			irm.addButton("IAD", "IAD");
			irm.addButton("VoIP", "VoIP");
			irm.addButton("STB", "STB");
			if (req.getParameter("devicetype-update") != null)
				tdo.setDeviceType(req.getParameter("devicetype"));
			irm.setCheckedValue(tdo.getDeviceType());
			tr = table.tr();
			tr.td("Device type:");
			tr.td().add(irm);
			tr.td().input("devicetype-update", "submit", "Change Device Type");

			// Next step
			tr = table.tr();
			tr.td("Next test:");
			File dir = getFileFromResource("tests");
			String[] files = dir.list();
			Arrays.sort(files, new NaturalComparator());
			String[] onlyFiles = new String[files.length - 2];
			for (int i = 0; i < onlyFiles.length; i++) {
				onlyFiles[i] = files[i];
			}
			SelectMaker sm = new SelectMaker(onlyFiles, "toString", "toString", "step");
			String stepToShow = null;
			if (req.getParameter("step-update") != null) {
				tdo.setStep(req.getParameter("step"));
				sm.setSelectedKeyword(tdo.getStep());
				stepToShow = tdo.getStep();
			} else if (req.getParameter("step-inspect") != null) {
				tdo.setStep(req.getParameter("step"));
				sm.setSelectedKeyword(tdo.getStep());
				stepToShow = tdo.getStep();
			} else if ("Manual".equals(req.getParameter("testtype"))) {
				sm.setSelectedKeyword(tdo.getStep());
				stepToShow = tdo.getStep();
			} else if (req.getParameter("step-reset") != null) {
				tdo.setStep(tdo.getLastStep());
				sm.setSelectedKeyword(tdo.getStep());
				stepToShow = tdo.getStep();
				//		} else if (req.getParameter("step") != null) {
				//			sm.setSelectedKeyword(req.getParameter("step"));
				//			stepToShow = req.getParameter("step");
			} else {
				sm.setSelectedKeyword(tdo.getStep());
				stepToShow = tdo.getStep();
			}
			tr.td().add(sm.makeHtml());
			if (tdo.getTestType().equals("Auto")) {
				td = tr.td();
				td.input("step-inspect", "submit", "Inspect test");
				if (!stepToShow.equals(tdo.getLastStep())) {
					td.add("&nbsp;&nbsp;");
					td.input("step-reset", "submit", "Reset to next auto-step");
				}
			} else
				tr.td().input("step-update", "submit", "Change test");

			// Status
			tr = table.tr();
			tr.td("Status:");
			tr.td(tdo.getStatus());

			// XML
			tr = table.tr();
			tr.td("XML:");
			File orgTestFile = getFileFromResource("tests/" + stepToShow);
			String orgTestXml = getContent(orgTestFile, tdo.getDeviceType());
			String xml = orgTestXml;
			TextAreaElement tae = tr.td().textarea("xml", 60, 10, false);
			new File("tests/modified").mkdirs();
			File modTestFile = getFileFromResource("tests/modified/" + username + "-" + stepToShow);
			String modTestXml = null;
			if (modTestFile.exists()) {
				modTestXml = getContent(modTestFile, tdo.getDeviceType());
			}
			boolean modTestFileWritten = false;
			if (req.getParameter("xml-update") != null) {
				xml = req.getParameter("xml");
				if (xml != null) {
					xml = xml.replaceAll("\\r", "");
					tae.addText(xml);
					if (!xml.equals(orgTestXml) && !xml.equals(modTestXml)) {
						FileWriter fw = new FileWriter(modTestFile);
						fw.write(xml);
						fw.close();
						modTestFileWritten = true;
					}
				} else {
					if (modTestXml != null)
						tae.addText(modTestXml);
					else
						tae.addText(orgTestXml);
				}
			} else if (req.getParameter("xml-delete") != null) {
				modTestFile.delete();
				tae.addText(orgTestXml);
			} else {
				if (modTestXml != null)
					tae.addText(modTestXml);
				else
					tae.addText(orgTestXml);
			}
			td = tr.td();
			td.input("xml-update", "submit", "Change XML");
			if ((modTestFileWritten || modTestXml != null) && req.getParameter("xml-delete") == null) {
				td.add("&nbsp;&nbsp;");
				td.input("xml-delete", "submit", "Revert to original XML");
			}

			// Result
			File result = getFileFromResource("tests/results/" + username + "-" + stepToShow);
			tr = table.tr();
			tr.td("Result:");
			tae = tr.td().textarea("xml", 60, 10, false);
			if (result.exists()) {
				FileReader fr = new FileReader(result);
				BufferedReader br = new BufferedReader(fr);
				String line = null;
				String resultStr = "";
				while ((line = br.readLine()) != null) {
					resultStr += line + "\n";
				}
				br.close();
				tae.addText(resultStr);
			}
			tr.td().input("xml-update", "submit", "Update result");
		}
		// Reset test
		tr = table.tr();
		tr.td();
		tr.td();
		tr.td().input("test-reset", "submit", "Reset everything");

		return tdo;

	}

	public static File getFileFromResource(String name) {
		return Optional.ofNullable(TestServlet.class.getResource("/" + name)).map(URL::getFile).map(File::new).orElseGet(() -> new File(name));
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		try {
			PrintWriter out = res.getWriter();
			if (TestDatabase.database == null)
				TestDatabase.database = new FileDatabase("acs-TR069-test.dat");
			Element html = new Element("html");
			html.head().title("xAPS Test Verification Process");
			Element body = html.body();
			body.h(1, "xAPS Test Verification Process");
			if (!Properties.DISCOVERY_MODE || !Properties.DEBUG_TEST_MODE) {
				body.add("The server is not set up in both discovery and test mode, thus it will not support this process.");
				body.add("Change configuration (or contact xAPS Administrator) to enable xAPS Test Verification Process.");
				out.println(html.toString(""));
				return;
			}
			intro(req, body);
			String username = null;
			if (req.getParameter("logout") == null)
				username = req.getParameter("username");
			if (username == null || username.trim().equals("")) {
				body.add("ACS username is required");
				Element form = body.form("test", "post", "form1");
				Element table = form.table();
				Element tr = table.tr();
				tr.td().input("username", "text");
				tr.td().input("login", "submit", "Login");
				username = null;
			} else {
				username = username.trim();
				TestDatabaseObject tdo = input(req, body, username);
				TestDatabase.database.insert(username, tdo.toString());
			}
			out.print(html.toString(""));
		} catch (Throwable t) {
			throw new ServletException(t);
		}
	}
}