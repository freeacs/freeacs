package com.github.freeacs.tr069.methods;

import com.github.freeacs.base.Log;
import com.github.freeacs.base.NoDataAvailableException;
import com.github.freeacs.common.util.NaturalComparator;
import com.github.freeacs.dbi.tr069.*;
import com.github.freeacs.dbi.tr069.TR069DMParameter.StringType;
import com.github.freeacs.dbi.tr069.TestCaseParameter.TestCaseParameterType;
import com.github.freeacs.tr069.*;
import com.github.freeacs.tr069.Properties;
import com.github.freeacs.tr069.exception.TR069Exception;
import com.github.freeacs.tr069.exception.TR069ExceptionShortMessage;
import com.github.freeacs.tr069.test.system1.KillDatabase;
import com.github.freeacs.tr069.test.system1.KillDatabaseObject;
import com.github.freeacs.tr069.test.system1.TestDatabase;
import com.github.freeacs.tr069.test.system1.TestDatabaseObject;
import com.github.freeacs.tr069.test.system2.TestException;
import com.github.freeacs.tr069.test.system2.TestUnit;
import com.github.freeacs.tr069.test.system2.TestUnitCache;
import com.github.freeacs.tr069.test.system2.Util;
import com.github.freeacs.tr069.xml.ParameterAttributeStruct;
import com.github.freeacs.tr069.xml.ParameterValueStruct;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RequestProcessor will parse the xml from the CPE. Any vital information will be stored in the SessionData or
 * RequestResponse objects. Some logging.
 * @author Morten
 *
 */
public class HTTPRequestProcessor {

	private static TR069DMParameterMap tr069ParameterMap = null;

	public static TR069DMParameterMap getTR069ParameterMap() throws Exception {
		if (tr069ParameterMap == null) {
			tr069ParameterMap = TR069DMLoader.load();
		}
		return tr069ParameterMap;
	}

	private static void verifyKillResponse(String xml, String unitId) {
		try {
			KillDatabaseObject kdo = new KillDatabaseObject(KillDatabase.database.select(unitId));
			kdo.setTestRunning(false);
			if (xml.contains("GetParameterNamesResponse")) {
				FileWriter gpnW = new FileWriter("GPN-" + unitId + ".txt");
				gpnW.write(xml);
				gpnW.close();
				kdo.setGpnFile("GPN-" + unitId + ".txt");
			} else if (xml.contains("GetParameterValuesResponse")) {
				FileWriter gpvW = new FileWriter("GPV-" + unitId + ".txt");
				gpvW.write(xml);
				gpvW.close();
				kdo.setGpvFile("GPV-" + unitId + ".txt");

			} else {
				FileWriter fw = new FileWriter("kill-report-" + unitId + ".txt", true);
				FileWriter fwDetails = new FileWriter("kill-report-details-" + unitId + ".txt", true);
				if (xml.contains("Fault")) {
					fw.write("failed");
					fwDetails.write("\nCPE-response was Fault:\n" + xml + "\n");
				} else {
					fw.write("ok");
					fwDetails.write("\nCPE-response was OK:\n" + xml + "\n");
				}
				fw.close();
				fwDetails.close();
			}
			KillDatabase.database.insert(unitId, kdo.toString());
		} catch (Throwable t) {
			Log.warn(HTTPRequestProcessor.class, "Error occurred in verifyKillResponse: " + t);
		}
	}

	/**
	 * Verify response from device according to the new, 2.generation test-system.
	 * The verification is based on several sources, like CWMP fault, what parameter values
	 * might be expected.
	 * @param reqRes
	 *
	 * @throws SQLException 
	 */
	private static void verifyResponseNEW(String requestMethodName, HTTPReqResData reqRes) throws Exception {
		// TODO:TF - verify result - completed
		// TR069 Test Framework:
		// Get TestUnit-object from TestUnitCache
		// Get STEPS from Unit
		// Get CurrentStep from TestUnit-object
		// Verify results - update TestCaseResult object if necessary (using TestDB)
		// 1. CWMP FAULT -> fail 
		// 2. Device parameters do not match GET/FAC-parameters from tc -> fail
		// 3. Device parameters do not match datamodel -> fail
		// 4. Exception in TR-069 server -> fail
		if (requestMethodName.equals(TR069Method.FAULT)) {
			Log.debug(HTTPRequestProcessor.class, "Fault on the client side has been discovered - will process");
			FAres.process(reqRes);
		} else
			Log.debug(HTTPRequestProcessor.class, "No fault on the client side - moving on");
		SessionData sessionData = reqRes.getSessionData();
		TestUnit tu = TestUnitCache.get(sessionData.getUnitId());
		tu.setUnit(reqRes.getSessionData().getUnit()); // Update TestUnit with latest Unit-information
		TestCase tc = tu.getCurrentCase();
		TestHistory history = tu.getHistory();
		if (history == null) { // Make a history-entry if not specified
			history = new TestHistory(sessionData.getUnittype(), new Date(sessionData.getStartupTmsForSession()), sessionData.getUnitId(), tc.getId(), tc.getExpectError());
			tu.setHistory(history);
		}
		TestDB testDB = new TestDB(sessionData.getDbAccessSession().getAcs());
		Log.debug(HTTPRequestProcessor.class, "Have retrieved the TestDB object");
		try {
			try {
				tr069ParameterMap = getTR069ParameterMap();
			} catch (Exception e) {
				throw new TestException("Could not load TR-069 data model - aborting test");
			}
			if (reqRes.getThrowable() != null) {
				history.setFailed(true);
				history.addResult("TR-069 Server failed in step " + tu.getSteps().getCurrentStep() + ":\n" + reqRes.getThrowable().getMessage());
			}
			boolean validParameter = true;
			if (reqRes.getRequest().getFault() != null) {
				if (reqRes.getRequest().getFault().getFaultCode() != null && reqRes.getRequest().getFault().getFaultCode().trim().equals("9005")) {
					validParameter = false;
				} else {
					history.setFailed(true);
					String faultMsg = reqRes.getRequest().getFault().toString().trim().replaceAll("\\n", ",").replaceAll("\\s+:\\s+", ":");
					history.addResult("A CWMP Fault occurred after executing step " + tu.getSteps().getCurrentStep() + ": " + faultMsg);
				}
			}
			if (validParameter && sessionData.getPreviousResponseMethod().equals(TR069Method.GET_PARAMETER_VALUES)) {
				GPVres.process(reqRes);
				Map<String, String> deviceParamMap = buildDeviceParamMapFromValues(sessionData.getFromCPE());
				for (TestCaseParameter tcp : tc.getParams()) {
					if (tcp.getType() != TestCaseParameterType.GET)
						continue;
					String deviceValue = deviceParamMap.get(tcp.getUnittypeParameter().getName());
					if (deviceValue == null) {
						history.setFailed(true);
						history.addResult("GETVALUE from device on parameter " + tcp.getUnittypeParameter().getName() + " returned no value at all");
						continue;
					}
					TR069DMParameter tr069Parameter = tr069ParameterMap.getParameter(tcp.getUnittypeParameter().getName());
					if (tcp.getType() == tu.getSteps().getCompareType() && !validAccordingToTestCase(tcp, deviceValue, tr069Parameter)) {
						history.setFailed(true);
						history.addResult("GETVALUE from device on parameter " + tcp.getUnittypeParameter().getName() + " returned \"" + lengthCheck(deviceValue) + "\", expected (test case "
								+ tcp.getType() + ") : " + tcp.getValue());
					}
					String valid = validAccordingToDataModel(tr069Parameter, deviceValue);
					if (!valid.equals("OK")) {
						history.setFailed(true);
						history.addResult("GETVALUE from device on parameter " + tcp.getUnittypeParameter().getName() + " returned \"" + lengthCheck(deviceValue) + "\", violates TR-069 data model: "
								+ valid);
					}
				}
			} else if (validParameter && sessionData.getPreviousResponseMethod().equals(TR069Method.GET_PARAMETER_ATTRIBUTES)) {
				GPAres.process(reqRes);
				Map<String, Integer> deviceParamMap = buildDeviceParamMapFromAttributes(sessionData.getAttributesFromCPE());
				for (TestCaseParameter tcp : tc.getParams()) {
					if (tcp.getType() != TestCaseParameterType.GET)
						continue;
					Integer notification = deviceParamMap.get(tcp.getUnittypeParameter().getName());
					if (notification == null) {
						history.setFailed(true);
						history.addResult("GETATTRIBUTE from device on parameter " + tcp.getUnittypeParameter().getName() + " returned no notification at all");
						continue;
					}
					TR069DMParameter tr069Parameter = tr069ParameterMap.getParameter(tcp.getUnittypeParameter().getName());
					String valid = validAccordingToTestCase(tcp, notification, tr069Parameter);
					if (!valid.equals("OK")) {
						history.setFailed(true);
						history.addResult("GETATTRIBUTE from device on parameter " + tcp.getUnittypeParameter().getName() + " returned " + notification + ", expected: " + valid);
					}
					valid = validAccordingToDataModel(tr069Parameter, notification);
					if (!valid.equals("OK")) {
						history.setFailed(true);
						history.addResult("GETATTRIBUTE from device on parameter " + tcp.getUnittypeParameter().getName() + " returned " + notification + ", violates TR-069 data model: " + valid);
					}
				}
			}
			if (tu.getSteps().lastStep()) { // Extra checks on the last step
				if (tc.getExpectError() != null && !tc.getExpectError() && history.getFailed() != null && history.getFailed()) {
					history.setFailed(false); // will overwrite the previously set flag
				}
			}
		} catch (TestException te) {
			Log.debug(HTTPRequestProcessor.class, "TestException occurred, will be logged to history");
			history.setFailed(true);
			history.addResult("TR-069 Server failed in step " + tu.getSteps().getCurrentStep() + ":\n" + reqRes.getThrowable().getMessage());
		}
		Log.debug(HTTPRequestProcessor.class, "History has result " + history.getResult() + ", should be written to database");
		testDB.addOrChangeTestHistory(history);
	}

	private static String lengthCheck(String value) {
		if (value.length() > 64)
			return value.substring(0, 64) + "...";
		else
			return value;
	}

	private static Map<String, String> buildDeviceParamMapFromValues(List<ParameterValueStruct> deviceParams) {
		Map<String, String> deviceParamMap = new HashMap<String, String>();
		for (ParameterValueStruct pvs : deviceParams) {
			deviceParamMap.put(pvs.getName(), pvs.getValue());
		}
		return deviceParamMap;
	}

	private static Map<String, Integer> buildDeviceParamMapFromAttributes(List<ParameterAttributeStruct> deviceParams) {
		Map<String, Integer> deviceParamMap = new HashMap<String, Integer>();
		for (ParameterAttributeStruct pas : deviceParams) {
			deviceParamMap.put(pas.getName(), pas.getNotifcation());
		}
		return deviceParamMap;
	}

	private static String validAccordingToTestCase(TestCaseParameter tcp, int notification, TR069DMParameter param) {
		if (tcp.getNotification() > -1 && notification != tcp.getNotification()) {
			if (param.getNotification() != null && param.getNotification().equals("canDeny")) {
				return "OK";
			} else {
				return "" + tcp.getNotification();
			}
		}
		return "OK";
	}

	/**
	 * Checks to see whether the parameter returned in a GET-method from the device
	 * is equal to the parameter expected by the test-case
	 * @param tcp
	 * @param deviceValue
	 * @return
	 */
	private static boolean validAccordingToTestCase(TestCaseParameter tcp, String deviceValue, TR069DMParameter dmp) {
		if (tcp.getValue() == null && deviceValue == null)
			return true;
		if (tcp.getValue() == null && deviceValue != null)
			return true;
		String tcpVal = tcp.getValue();
		if (tcpVal.equalsIgnoreCase("[STRING]"))
			return true;
		if (deviceValue == null || deviceValue.trim().equals("")) {
			if (tcpVal.equalsIgnoreCase("[EMPTY]"))
				return true;
			else
				return false;
		}
		if (tcpVal.equals(deviceValue))
			return true;
		if (tcpVal.equalsIgnoreCase("[NUMBER]")) {
			try {
				Integer.parseInt(deviceValue);
				return true;
			} catch (NumberFormatException nfe) {
				return false;
			}
		}
		if (dmp.getDatatype() == TR069DMType.BOOLEAN) {
			if (tcp.getValue().equals("false") && deviceValue.equals("0"))
				return true;
			if (tcp.getValue().equals("true") && deviceValue.equals("1"))
				return true;
		}
		return false;
	}

	private static String validAccordingToDataModel(TR069DMParameter param, Integer notification) {
		if (param == null)
			return "OK";
		if (notification < 0 || notification > 2)
			return "Notification is outside limits (0-2)";
		if (param.getNotification() != null && param.getNotification().equals("forceEnabled") && notification != 2)
			return "ForceEnabled is on, notification must be 2 (Active)";
		return "OK";
	}

	private static boolean validAccordingToEnumeration(TR069DMParameter param, String deviceValue) {
		boolean enumMatch = false;
		for (StringType st : param.getEnumeration()) {
			if (st.getValue() != null && st.getValue().equals(deviceValue))
				enumMatch = true;
			if (st.getPattern() != null && Pattern.matches(st.getPattern(), deviceValue))
				enumMatch = true;
		}
		return enumMatch;
	}

	/**
	 * Checks to see whether the parameter returned in a GET-method from the device
	 * is equal to the parameter expected by the TR069 data model (loaded from XML schema
	 * published by Broadband Forum).
	 * @param param
	 * @param deviceValue
	 * @return
	 */
	private static String validAccordingToDataModel(TR069DMParameter param, String deviceValue) {
		if (param == null)
			return "OK"; // No TR-069 parameter officially defined - no validation possible
		TR069DMType dt = param.getDatatype();
		if (param.getEnumeration() != null && param.getEnumeration().size() > 0) { // Probably only found for String-type data
			if (deviceValue == null)
				return "Did not match enumeration - no value";
			if (param.isList()) {
				for (String s : deviceValue.split(",")) {
					boolean enumMatch = validAccordingToEnumeration(param, s.trim());
					if (!enumMatch)
						return "Did not match enumeration";
				}
				return "OK";
			} else {
				if (validAccordingToEnumeration(param, deviceValue))
					return "OK";
				else
					return "Did not match enumeration";
			}
		}

		if (dt == TR069DMType.STRING || dt == TR069DMType.ALIAS || dt == TR069DMType.HEXBINARY || dt == TR069DMType.BASE64 || //
				dt == TR069DMType.MACADDRESS || dt == TR069DMType.IPADDRESS || dt == TR069DMType.IPV6ADDRESS || dt == TR069DMType.IPV4ADDRESS || //
				dt == TR069DMType.IPPREFIX || dt == TR069DMType.IPV4PREFIX || dt == TR069DMType.IPV6PREFIX) {
			if (deviceValue == null || deviceValue.trim().equals(""))
				return "OK";
			if (param.getRange().getMin() != null && deviceValue.length() < param.getRange().getMin())
				return "Size too small (" + deviceValue.length() + " < " + param.getRange().getMin() + ")";
			if (param.getRange().getMax() != null && deviceValue.length() > param.getRange().getMax())
				return "Size too big (" + deviceValue.length() + " > " + param.getRange().getMax() + ")";
			if (dt == TR069DMType.HEXBINARY && !Pattern.matches("[a-fA-F-0-9]*", deviceValue))
				return "Not hexBinary";
			if (dt == TR069DMType.BASE64 && !Pattern.matches("[0-9a-zA-Z\\+/=]*", deviceValue))
				return "Not base64";
			//			if (!param.hasSpecificRange() && (deviceValue.length() < dt.getMin() || deviceValue.length() > dt.getMax())) { // Default range
			//				return "Size outside range";
			//			}
		} else if (dt == TR069DMType.BOOLEAN) {
			if (!deviceValue.equals("0") && !deviceValue.equals("1") && !deviceValue.equals("false") && !deviceValue.equals("true"))
				return "Not a boolean";
		} else if (dt == TR069DMType.DATETIME) {
			// Currently no validation of datetime - may perhaps look into SimpleDateFormat.parse()
		} else if (dt == TR069DMType.INTEGER || dt == TR069DMType.INT || dt == TR069DMType.LONG || dt == TR069DMType.UNSIGNEDINT || // 
				dt == TR069DMType.UNSIGNEDLONG || dt == TR069DMType.STATSCOUNTER32 || dt == TR069DMType.STATSCOUNTER64 || dt == TR069DMType.DBM1000) {
			try {
				int deviceInt = Integer.parseInt(deviceValue);
				if (param.getRange().getMin() != null && deviceInt < param.getRange().getMin())
					return "Number too low";
				if (param.getRange().getMax() != null && deviceInt > param.getRange().getMax())
					return "Number too high";
			} catch (NumberFormatException nfe) {
				// We might very well try to parse a long, unsigned int, unsignedlong or statscounter64 - but this may fail
				// from time to time - the range object does not support more than Integer-range anyway, so we ignore this 
			}
			//			if (!param.hasSpecificRange()) {
			//				try {
			//					long deviceLong = Long.parseLong(deviceValue);
			//					if (deviceLong < dt.getMin() || deviceLong > dt.getMax()) // Default range
			//						return "Number outside range";
			//				} catch (NumberFormatException nfe) {
			//					// Even a Long might not be big enough for a unisignedLong and statscounter64
			//				}
			//			}
		}
		return "OK";
	}

	/**
	 * Verify response from device according to the old, 1.generation test-system.
	 * The verification is based on whether or not a test signals a CWMP fault or 
	 * not. For the kill-test is slightly different.
	 * @param reqRes
	 */
	private static void verifyResponseOLD(HTTPReqResData reqRes) {
		String xml = reqRes.getRequest().getXml();
		String unitId = reqRes.getSessionData().getUnitId();
		String row = TestDatabase.database.select(unitId);
		TestDatabaseObject tdo = new TestDatabaseObject(row);
		if (tdo.getTestType().equals("Kill"))
			verifyKillResponse(xml, unitId);
		else {
			try {
				FileWriter fw = new FileWriter("tests/results/" + unitId + "-" + tdo.getStep());
				fw.write(xml);
				fw.close();
			} catch (IOException e) {
				Log.warn(HTTPRequestProcessor.class, "Failed to write to result file: " + e);
			}
			if (xml.indexOf("Fault") == -1 || ((tdo.getStep().equals("7.5.4.txt") || tdo.getStep().equals("7.7.3.txt")) && xml.indexOf("Fault") > -1)) {
				tdo.addOk(tdo.getStep());
				if (tdo.getTestType().equalsIgnoreCase("Auto")) {
					String[] files = (new File("tests")).list();
					Arrays.sort(files, new NaturalComparator());
					boolean match = false;
					boolean nextStepFound = false;
					for (String file : files) {
						if (match && !(new File(file).isDirectory())) {
							tdo.setStep(file);
							nextStepFound = true;
							break;
						}
						if (tdo.getStep().equals(file)) {
							match = true;
						}
					}
					if (!nextStepFound) {
						tdo.setRun("false");
					}
				}
				try {
					TestDatabase.database.insert(unitId, tdo.toString());
				} catch (IOException e) {
					Log.warn(HTTPRequestProcessor.class, "Failed to write to test database: " + e);
				}
			}
		}
	}

	/** 
	 * Process the request from the CPE. For some CPE-requests processing is simple (do-nothing), for
	 * others it may involve more logic. Keep in mind that a HTTP-request from the CPE, might actually
	 * be a TR-69 response!
	 * 
	 * If in Testmode, then the request is processed and validated in an entirely different way than
	 * normal provisioning! 
	 * @param reqRes
	 * @throws TR069Exception
	 */
	public static void processRequest(HTTPReqResData reqRes) throws TR069Exception {
		try {
			String requestMethodName = extractMethodName(reqRes.getRequest().getXml());
			if (requestMethodName == null)
				requestMethodName = TR069Method.EMPTY;
			reqRes.getRequest().setMethod(requestMethodName);

			if (reqRes.getSessionData().isTestMode()) {
				Log.debug(HTTPRequestProcessor.class, "Will verify response according to test setup");
				if (Util.testEnabled(reqRes, false)) {
					verifyResponseNEW(requestMethodName, reqRes);
				} else {
					if (reqRes.getRequest().getXml().length() > 1) {
						// TR069 Plugfest/Kill Test
						verifyResponseOLD(reqRes);
					}
					requestMethodName = TR069Method.EMPTY;
					reqRes.getRequest().setMethod(requestMethodName);
				}
			} else {
				Log.debug(HTTPRequestProcessor.class, "Will process method " + requestMethodName + " (incoming request/response from CPE)");
				HTTPRequestAction reqAction = TR069Method.requestMap.get(requestMethodName);
				if (reqAction != null) {
					reqRes.getRequest().setXml(HTTPReqData.XMLFormatter.filter(reqRes.getRequest().getXml()));
					reqAction.getProcessRequestMethod().apply(reqRes);
				} else {
					throw new UnknownMethodException(requestMethodName);
				}
			}
		} catch (Throwable t) {
			if (t instanceof InvocationTargetException && t.getCause() != null)
				t = t.getCause();
			if (t instanceof TR069Exception)
				throw (TR069Exception) t;
			if (t instanceof NoDataAvailableException)
				throw new TR069Exception("Device was not found in database - can only provision device if in server is in discovery mode and device supports basic authentication",
						TR069ExceptionShortMessage.NODATA);
			else
				throw new TR069Exception("Could not process HTTP-request (from TR-069 client)", TR069ExceptionShortMessage.MISC, t);
		} finally {
			if (reqRes.getRequest().getMethod() == null) {
				reqRes.getRequest().setMethod(TR069Method.EMPTY);
				reqRes.getRequest().setXml("");
			}
			if (Log.isConversationLogEnabled()) {
				String unitId = reqRes.getSessionData().getUnitId();
				String xml = reqRes.getRequest().getXml();
				if (Properties.isPrettyPrintQuirk(reqRes.getSessionData()))
					xml = HTTPReqData.XMLFormatter.prettyprint(reqRes.getRequest().getXml());
				Log.conversation(reqRes.getSessionData(), "============== FROM CPE ( " + Optional.ofNullable(unitId).orElseGet(() -> "Unknown") + " ) TO ACS ===============\n" + xml);
			}
		}
	}

	private static final Pattern methodNamePattern = Pattern.compile(":Body.*>\\s*<cwmp:(\\w+)(>|/>)", Pattern.DOTALL);

	/**
	 * Fastest way to extract the method name without actually parsing the XML - the method name is crucial to
	 * the next steps in TR-069 processing
	 *
	 * The TR-069 Method is found after the first "<cwmp:" after ":Body"
	 *
	 * @param reqStr (TR-069 XML)
	 * @return TR-069 methodName
	 */
	static String extractMethodName(String reqStr) {
		String methodStr = getMethodStr(reqStr);
		if (methodStr != null && methodStr.endsWith("Response"))
			methodStr = methodStr.substring(0, methodStr.length() - 8);
		return methodStr;
	}

	private static String getMethodStr(String reqStr) {
		Matcher matcher = methodNamePattern.matcher(reqStr);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}
}
