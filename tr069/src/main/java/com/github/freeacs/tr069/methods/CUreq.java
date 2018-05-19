package com.github.freeacs.tr069.methods;

import com.github.freeacs.base.Log;
import com.github.freeacs.tr069.exception.TR069Exception;
import com.github.freeacs.tr069.test.system1.*;
import com.github.freeacs.tr069.test.system2.TestUnit;
import com.github.freeacs.tr069.test.system2.TestUnitCache;
import com.github.freeacs.tr069.xml.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class CUreq extends Body {

	private String keyRoot;
	private String unitId;
	private static Map<String, List<ParameterValueStruct>> paramMap = new TreeMap<String, List<ParameterValueStruct>>();

	public CUreq(String keyRoot, String unitId) {
		this.keyRoot = keyRoot;
		this.unitId = unitId;
	}

	private static void populateParamsMap(String unitId, String gpnFile, String gpvFile) throws IOException, TR069Exception {

		// process gpnFile
		FileReader fr = new FileReader(gpnFile);
		BufferedReader br = new BufferedReader(fr);
		StringBuffer sb = new StringBuffer();
		String line = null;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		br.close();
		fr.close();

		Parser parser = new Parser(sb.toString());
		ParameterList parameterList = parser.getParameterList();
		List<ParameterInfoStruct> pisList = parameterList.getParameterInfoList();

		// process gpvFile
		fr = new FileReader(gpvFile);
		br = new BufferedReader(fr);
		sb = new StringBuffer();
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		br.close();
		fr.close();
		parser = new Parser(sb.toString());
		parameterList = parser.getParameterList();
		List<ParameterValueStruct> pvsList = parameterList.getParameterValueList();

		List<ParameterValueStruct> newPvsList = new ArrayList<ParameterValueStruct>();

		for (ParameterInfoStruct pis : pisList) {
			if (pis.isWritable()) {
				for (ParameterValueStruct pvs : pvsList) {
					if (pvs.getName().equals(pis.getName())) {
						if (pvs.getName().contains("ManagementServer."))
							continue;
						if (pvs.getName().contains("X_FREEACS-COM.TFTP.SoftwareVersion"))
							continue;
						if (pvs.getName().contains("X_FREEACS-COM.TFTP.RefreshInterval"))
							continue;
						if (pvs.getName().contains("X_FREEACS-COM.Web."))
							continue;
						newPvsList.add(pvs);
					}
				}

			}
		}
		paramMap.put(unitId, newPvsList);

	}

	private static String fail = "fail";
	private static String uncertain = "uncertain";
	private static String allowed = "allowed";

	private static Map<String, String[]> typeMap = new HashMap<String, String[]>();
	static {
		typeMap.put("xsd:boolean" + fail, new String[] { "on", "2", "-1", "" });
		typeMap.put("xsd:boolean" + allowed, new String[] { "0", "1", "true", "false" });
		typeMap.put(
				"xsd:string" + fail,
				new String[] { "�29502-||`\\+9389bc shd afjaf shd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjafshd afjaf" });
		typeMap.put("xsd:string" + uncertain, new String[] { "Test", "Hello World", "123", "12345678901234567",
				"A long sentence which surely will be too long for most parameters because it's more than 16 char long", "", "192.168.0.1", " 192.168.0.1", "192.168.0.1 ", "192. 168.0.1",
				"192.168.0.", "192.168.0", "500.168.0.1", "192.168.0.1,192.168.0.2", "192.168.0.1, 192.168.0.2" });
		typeMap.put("xsd:int" + fail, new String[] { "�2147483649", "2147483648", "string", "" });
		typeMap.put("xsd:int" + uncertain, new String[] { "0", "1", "2", "-1", "256", "65536", "�2147483648", "2147483647" });
		typeMap.put("xsd:unsignedInt" + fail, new String[] { "-1", "4294967296", "string", "" });
		typeMap.put("xsd:unsignedInt" + uncertain, new String[] { "0", "1", "2", "256", "65536", "4294967295" });
		typeMap.put("xsd:dateTime" + fail, new String[] { "2010-05-32T00:00:00", "2010-05-30T25:61:00", "string", "-1", "false", "" });
		typeMap.put("xsd:dateTime" + uncertain, new String[] { "0001-01-01T00:00:00Z", "2010-05-30T00:00:00Z" });
	}

	private static String generateValue(ParameterValueStruct pvs, ParameterValueStruct nextPvs, KillDatabaseObject kdo) {
		String[] failMap = typeMap.get(pvs.getType() + fail);
		String[] uncertainMap = typeMap.get(pvs.getType() + uncertain);
		String[] allowedMap = typeMap.get(pvs.getType() + allowed);
		int offset = 0;
		int testIndex = kdo.getTestIndex() + 1; // next test
		if (failMap != null) {
			if (failMap.length > testIndex - offset) {
				kdo.setExpectedResult(fail);
				kdo.setTestIndex(testIndex);
				return failMap[testIndex - offset];
			} else {
				offset += failMap.length;
			}
		}
		if (uncertainMap != null) {
			if (uncertainMap.length > testIndex - offset) {
				kdo.setExpectedResult(uncertain);
				kdo.setTestIndex(testIndex);
				return uncertainMap[testIndex - offset];
			} else {
				offset += uncertainMap.length;
			}
		}
		if (allowedMap != null) {
			if (allowedMap.length > testIndex - offset) {
				kdo.setExpectedResult(allowed);
				kdo.setTestIndex(testIndex);
				return allowedMap[testIndex - offset];
			} else {
				offset += allowedMap.length;
			}
		}

		// We have to skip to next parameter and start on testIndex 0.
		if (nextPvs == null)
			return null;
		kdo.setParamIndex(kdo.getParamIndex() + 1);
		kdo.setTestIndex(0);
		kdo.setExpectedResult(fail);
		return typeMap.get(nextPvs.getType() + fail)[kdo.getTestIndex()]; // we always have one fail-test - so this code is safe to run.
	}

	private String paramsTest() throws IOException, TR069Exception {
		KillDatabase.refresh();
		String killrow = KillDatabase.database.select(unitId);
		KillDatabaseObject kdo = new KillDatabaseObject(killrow);
		if (kdo.getGpnFile().equals("null")) {
			kdo.setParamIndex(0);
			kdo.setTestIndex(0);
			KillDatabase.database.insert(unitId, kdo.toString());
			GPNreq gpnReq = new GPNreq(keyRoot, false);
			return gpnReq.toXmlImpl();
		} else if (kdo.getGpvFile().equals("null")) {
			kdo.setParamIndex(0);
			kdo.setTestIndex(0);
			KillDatabase.database.insert(unitId, kdo.toString());
			ParameterValueStruct pvs = new ParameterValueStruct(keyRoot, "dummy");
			List<ParameterValueStruct> pvsList = new ArrayList<ParameterValueStruct>();
			pvsList.add(pvs);
			GPVreq gpvReq = new GPVreq(pvsList);
			return gpvReq.toXmlImpl();
		} else {
			if (paramMap.get(unitId) == null) {
				populateParamsMap(unitId, kdo.getGpnFile(), kdo.getGpvFile());
			}
			List<ParameterValueStruct> params = paramMap.get(unitId);
			ParameterValueStruct pvs = null;
			ParameterValueStruct nextPvs = null;
			if (params.size() > kdo.getParamIndex())
				pvs = params.get(kdo.getParamIndex());
			if (params.size() > kdo.getParamIndex() + 1)
				nextPvs = params.get(kdo.getParamIndex() + 1);
			String value = generateValue(pvs, nextPvs, kdo);
			if (value == null) {
				TestDatabaseObject tdo = new TestDatabaseObject(TestDatabase.database.select(unitId));
				tdo.setRun("false");
				TestDatabase.database.insert(unitId, tdo.toString());
				return "";
			} else {
				ParameterValueStruct usedPvs = params.get(kdo.getParamIndex());
				ParameterValueStruct newPvs = new ParameterValueStruct(usedPvs.getName(), value, usedPvs.getType());
				List<ParameterValueStruct> pvsList = new ArrayList<ParameterValueStruct>();
				pvsList.add(newPvs);
				SPVreq spvReq = new SPVreq(pvsList, "dummy");
				kdo.setTestRunning(true);
				KillDatabase.database.insert(unitId, kdo.toString());
				printReport(unitId, newPvs, kdo);
				return spvReq.toXmlImpl();
			}
		}
	}

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");

	private static void printReport(String unitId, ParameterValueStruct pvs, KillDatabaseObject kdo) {
		try {
			FileWriter fw = new FileWriter("kill-report-" + unitId + ".txt", true);
			FileWriter fwDetails = new FileWriter("kill-report-details-" + unitId + ".txt", true);
			String tms = sdf.format(new Date());
			fw.write("\n"+tms + " Param:" + pvs.getName() + ", Type:" + pvs.getType() + ", Value:" + pvs.getValue() + ", Expected:" + kdo.getExpectedResult() + ", ");
			fwDetails.write("\n"+tms + " Param:" + pvs.getName() + ", Type:" + pvs.getType() + ", Value:" + pvs.getValue() + ", Expected:" + kdo.getExpectedResult() + "\n");
			fw.close();
			fwDetails.close();
		} catch (Throwable t) {
			Log.warn(CUreq.class, "Error occurred in printReport(): " + t);
		}
	}

	@Override
	public String toXmlImpl() {
		try {
			String row = TestDatabase.database.select(unitId);
			TestDatabaseObject tdo = new TestDatabaseObject(row);
			String xml = "";
			if (TestUnitCache.get(unitId) != null) {
				TestUnit tu = TestUnitCache.get(unitId);
				com.github.freeacs.dbi.File inputFile = tu.getCurrentCase().getFiles().getInputFile();
				xml = new String(inputFile.getContent());
			} else {
				if (tdo.getStep().equals("Kill")) {
					xml = paramsTest();
				} else {
					try {
						xml = TestServlet.getContent(new File("tests/modified/" + unitId + "-" + tdo.getStep()), tdo.getDeviceType());
					} catch (IOException ioe2) {
						xml = TestServlet.getContent(new File("tests/" + tdo.getStep()), tdo.getDeviceType());
					}
				}
			}
			return xml.replaceAll("InternetGatewayDevice.", keyRoot);
		} catch (Exception ioe) {
			Log.error(CUreq.class, "Could not find the test data: " + ioe);
			return "";
		}
	}

}
