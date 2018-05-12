package com.github.freeacs.dbi.tr069;

import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.dbi.tr069.TestCase.TestCaseMethod;
import com.github.freeacs.dbi.tr069.TestCaseParameter.TestCaseParameterType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestGenerator {

	private Unittype unittype;
	private TR069DMParameterMap tr069Pmap;
	// A structure to keep a series of TR069Parameters in a list, each belonging to the same object, but on different "level/layers" in the
	// TR-069 datamodel
	// Key: Number of layers, Value: Map: Key: ParameterName, Value: TR069Parameter
	private Map<Integer, Map<String, List<UnittypeParameter>>> layerMap = new HashMap<Integer, Map<String, List<UnittypeParameter>>>();

	public TestGenerator(TR069DMParameterMap tr069Pmap, Unittype unittype) {
		this.tr069Pmap = tr069Pmap;
		this.unittype = unittype;
	}

	
	// Add parameter to the layerMap data structure, building the datastructure as new parameters are added
	private void addToLayerMap(UnittypeParameter utp) {
		String[] paramNameArray = utp.getName().split("\\.");
		if (paramNameArray.length <= 1)
			return;
		String objectName = paramNameArray[0];
		for (int i = 1; i < paramNameArray.length - 1; i++) {
			objectName += "." + paramNameArray[i];
			Map<String, List<UnittypeParameter>> layerMapI = layerMap.get(i);
			if (layerMapI == null) {
				layerMapI = new HashMap<String, List<UnittypeParameter>>();
				layerMap.put(i, layerMapI);
			}
			List<UnittypeParameter> objectParamList = layerMapI.get(objectName);
			if (objectParamList == null) {
				objectParamList = new ArrayList<UnittypeParameter>();
				layerMapI.put(objectName, objectParamList);
			}
			objectParamList.add(utp);
		}
	}

	public List<TestCase> generateSetTcListFromMasterTc(TestCase masterTc) throws Exception {
		List<TestCase> generatedTc = new ArrayList<TestCase>();
		List<TestCaseParameter> stcParams = new ArrayList<TestCaseParameter>();
		for (TestCaseParameter tcp : masterTc.getParams()) {
			if (tcp.getType() == TestCaseParameterType.SET)
				stcParams.add(tcp);
		}
		for (int i = 0; i < stcParams.size(); i++) {
			int paramsSet = stcParams.size() - i;
			TestCaseParameter tcp = stcParams.get(i);
			TR069DMParameter dmp = tr069Pmap.getParameter(tcp.getUnittypeParameter().getName());
			TestCase tc = new TestCase(unittype, TestCaseMethod.VALUE, masterTc.getTags() + "[SET-" + paramsSet + "] [" + dmp.getDatatype().getXsdType() + "]", masterTc.getExpectError());
			for (int j = i; j < stcParams.size(); j++) {
				TestCaseParameter stcParam = stcParams.get(j);
				tc.getParams()
						.add(new TestCaseParameter(TestCaseParameterType.SET, stcParam.getUnittypeParameter(), stcParam.getDataModelParameter(), stcParam.getValue(), stcParam.getNotification()));
				tc.getParams()
						.add(new TestCaseParameter(TestCaseParameterType.GET, stcParam.getUnittypeParameter(), stcParam.getDataModelParameter(), stcParam.getValue(), stcParam.getNotification()));
			}
			generatedTc.add(tc);
			/* This code causes an extra (broken) TC, found this when Morten was in India. I think it's safe to remove.
			 * 
			tc = new TestCase(unittype, TestCaseMethod.VALUE, masterTc.getTags() + " [SET-1] [" + dmp.getDatatype().getXsdType() + "]", true);
			tc.getParams().add(tcp);
			tc.getParams().add(new TestCaseParameter(TestCaseParameterType.GET, tcp.getUnittypeParameter(), tcp.getDataModelParameter(), null, tcp.getNotification()));
			generatedTc.add(tc);
			*/
		}

		return generatedTc;
	}

	public List<TestCase> generate(String type) throws Exception {
		List<TestCase> generatedTc = new ArrayList<TestCase>();
		if (type == null || type.equals("VALUE"))
			generateValueTC(generatedTc);
		if (type == null || type.equals("ATTRIBUTE"))
			generateAttributeTC(generatedTc);
		return generatedTc;
	}

	private List<TestCase> generateAttributeTC(List<TestCase> generatedTc) throws Exception {
		int count = 0;
		for (UnittypeParameter utp : unittype.getUnittypeParameters().getUnittypeParameters()) {
			TR069DMParameter p = tr069Pmap.getParameter(utp.getName());
			if (p == null) {
				continue;
			}
			generatedTc.add(generateAttributeTestCase(p, utp, 0));
			generatedTc.add(generateAttributeTestCase(p, utp, 1));
			generatedTc.add(generateAttributeTestCase(p, utp, 2));
			count += 3;
		}
		System.out.println("Generated " + count + " attribute TC's");
		return generatedTc;
	}

	private TestCase generateAttributeTestCase(TR069DMParameter dmp, UnittypeParameter utp, int notification) {
		TestCase tc = null;
		String notificationRule = dmp.getNotification();
		if (notificationRule == null)
			notificationRule = "normal";
		if (notificationRule.equals("forceEnabled") && notification < 2) {
			tc = new TestCase(unittype, TestCaseMethod.ATTRIBUTE, "[GENERATED] [RULE-" + notificationRule + "] [NOTIFY-" + notification + "] [" + dmp.getDatatype().getXsdType() + "]", true);
			tc.getParams().add(new TestCaseParameter(TestCaseParameterType.GET, utp, dmp, null, notification));
			tc.getParams().add(new TestCaseParameter(TestCaseParameterType.SET, utp, dmp, null, notification));
		} else if (!notificationRule.equals("canDeny")) {
			tc = new TestCase(unittype, TestCaseMethod.ATTRIBUTE, "[GENERATED] [RULE-" + notificationRule + "] [NOTIFY-" + notification + "] [" + dmp.getDatatype().getXsdType() + "]", false);
			tc.getParams().add(new TestCaseParameter(TestCaseParameterType.GET, utp, dmp, null, notification));
			tc.getParams().add(new TestCaseParameter(TestCaseParameterType.SET, utp, dmp, null, notification));
		} else {
			tc = new TestCase(unittype, TestCaseMethod.ATTRIBUTE, "[GENERATED] [RULE-" + notificationRule + "] [NOTIFY-" + notification + "] [" + dmp.getDatatype().getXsdType() + "]", null);
			tc.getParams().add(new TestCaseParameter(TestCaseParameterType.GET, utp, dmp, null, notification));
			tc.getParams().add(new TestCaseParameter(TestCaseParameterType.SET, utp, dmp, null, notification));
		}
		return tc;
	}

	private void generateLargeValueTC(List<TestCase> generatedTc) {
		int testPower = 1;
		int testBase = 2;
		int i = 0;
		TestCase largeTC = new TestCase(unittype, TestCaseMethod.VALUE, "[GENERATED] [LARGE]", false);
		while (i < unittype.getUnittypeParameters().getUnittypeParameters().length) {
			UnittypeParameter utp = unittype.getUnittypeParameters().getUnittypeParameters()[i++];
			TR069DMParameter dmp = tr069Pmap.getParameter(utp.getName());
			if (dmp == null) {
				continue;
			}
			if (dmp.getName().contains("ManagementServer.")) {
				continue;
			}
			largeTC.getParams().add(new TestCaseParameter(TestCaseParameterType.GET, utp, dmp, "[STRING]", 0));
			if (i >= Math.round(Math.pow(testBase, testPower))) {
				i = 0;
				testPower++;
				generatedTc.add(largeTC);
				largeTC = new TestCase(unittype, TestCaseMethod.VALUE, "[GENERATED] [LARGE]", false);
			}
		}
		System.out.println("Generated " + testPower + " large TC's");
		generatedTc.add(largeTC);
	}

	private List<TestCase> generateValueTC(List<TestCase> generatedTc) throws Exception {

		generateLargeValueTC(generatedTc);

		int simplecount = 0, boolcount = 0, stringcount = 0, templatecount = 0, complexcount = 0;

		parameters: for (UnittypeParameter utp : unittype.getUnittypeParameters().getUnittypeParameters()) {
			TR069DMParameter p = tr069Pmap.getParameter(utp.getName());
			if (p == null) {
				continue;
			}
			if (p.getName().contains("ManagementServer."))
				continue;

			Pattern pattern = Pattern.compile("(\\d+)");
			Matcher matcher = pattern.matcher(utp.getName());

			while (matcher.find()) {
				int number = Integer.parseInt(matcher.group());
				if (number != 1)
					continue parameters;
			}
			addToLayerMap(utp);

			generatedTc.add(generateSimpleTestCase(p, utp));
			simplecount++;

			if (!p.isReadOnly()) {
				if (p.getDatatype() == TR069DMType.BOOLEAN) {
					generatedTc.add(generateValueBooleanTestCase(p, utp, false));
					generatedTc.add(generateValueBooleanTestCase(p, utp, true));
					boolcount += 2;
				} else if (p.getDatatype() == TR069DMType.ALIAS) {
					generatedTc.add(generateValueStringTestCase(p, utp));
					stringcount++;
				} else if (p.getDatatype() == TR069DMType.STRING && p.getName().toUpperCase().endsWith("PASSWORD")) {
					generatedTc.add(generateValueStringTestCase(p, utp));
					stringcount++;
				} else {
					generatedTc.add(generateValueTemplateTestCase(p, utp));
					templatecount++;
				}

			}
		}
		for (Integer layer : layerMap.keySet()) {
			Map<String, List<UnittypeParameter>> layerMapI = layerMap.get(layer);
			for (String objectName : layerMapI.keySet()) {
				generatedTc.add(generateValueComplexTemplateTestCase(layer, layerMapI.get(objectName)));
				complexcount++;
			}
		}
		System.out.println("Generated " + simplecount + " simple TC's");
		System.out.println("Generated " + boolcount + " boolean TC's");
		System.out.println("Generated " + stringcount + " string TC's");
		System.out.println("Generated " + templatecount + " template TC's");
		System.out.println("Generated " + complexcount + " complex TC's");
		return generatedTc;
	}

	private TestCase generateValueComplexTemplateTestCase(Integer layer, List<UnittypeParameter> utpList) {
		TestCase tc = new TestCase(unittype, TestCaseMethod.VALUE, "[GENERATED] [TEMPLATE] [COMPLEX] [LAYER" + layer + "]", true);
		for (UnittypeParameter utp : utpList) {
			TR069DMParameter dmp = tr069Pmap.getParameter(utp.getName());
			if (dmp.isReadOnly()) {
				tc.getParams().add(new TestCaseParameter(TestCaseParameterType.GET, utp, dmp, "[STRING]", 0));
				tc.getParams().add(new TestCaseParameter(TestCaseParameterType.FAC, utp, dmp, "[STRING]", 0));
			} else
				tc.getParams().add(new TestCaseParameter(TestCaseParameterType.SET, utp, dmp, "HelloWorld-" + utp.getId(), 0));
		}
		return tc;
	}

	private TestCase generateSimpleTestCase(TR069DMParameter dmp, UnittypeParameter utp) {
		String tags = "[GENERATED] [SIMPLE] ";
		if (dmp.isReadOnly()) {
			tags += "[READONLY]";
		} else {
			tags += "[READWRITE]";
		}
		tags += " [" + dmp.getDatatype().getXsdType() + "]";
		TestCase tc = new TestCase(unittype, TestCaseMethod.VALUE, tags, null);
		tc.getParams().add(new TestCaseParameter(TestCaseParameterType.GET, utp, dmp, "[STRING]", 0));
		return tc;
	}

	private TestCase generateValueBooleanTestCase(TR069DMParameter dmp, UnittypeParameter utp, boolean state) {
		TestCase tc = new TestCase(unittype, TestCaseMethod.VALUE, "[GENERATED] [READWRITE] [" + dmp.getDatatype().getXsdType() + "] [" + (state + "").toUpperCase() + "]", null);
		tc.getParams().add(new TestCaseParameter(TestCaseParameterType.SET, utp, dmp, state + "", 0));
		tc.getParams().add(new TestCaseParameter(TestCaseParameterType.GET, utp, dmp, state + "", 0));
		return tc;
	}

	private TestCase generateValueStringTestCase(TR069DMParameter dmp, UnittypeParameter utp) {
		TestCase tc = new TestCase(unittype, TestCaseMethod.VALUE, "[GENERATED] [READWRITE] [" + dmp.getDatatype().getXsdType() + "]", null);
		tc.getParams().add(new TestCaseParameter(TestCaseParameterType.SET, utp, dmp, "HelloWorld-" + utp.getId(), 0));
		tc.getParams().add(new TestCaseParameter(TestCaseParameterType.GET, utp, dmp, "HelloWorld-" + utp.getId(), 0));
		return tc;
	}

	private TestCase generateValueTemplateTestCase(TR069DMParameter dmp, UnittypeParameter utp) {
		TestCase tc = new TestCase(unittype, TestCaseMethod.VALUE, "[GENERATED] [READWRITE] [TEMPLATE] [" + dmp.getDatatype().getXsdType() + "]", true);
		tc.getParams().add(new TestCaseParameter(TestCaseParameterType.SET, utp, dmp, "HelloWorld-" + utp.getId(), 0));
		tc.getParams().add(new TestCaseParameter(TestCaseParameterType.GET, utp, dmp, "HelloWorld-" + utp.getId(), 0));
		return tc;
	}

}
