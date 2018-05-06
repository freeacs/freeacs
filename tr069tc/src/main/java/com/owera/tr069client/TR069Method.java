package com.owera.tr069client;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.owera.tr069client.monitor.Status;

/**
 * Method is class which contains information about  which method to perform next. 
 * It could be that this class should be a mere superclass and that subclasses also
 * should have attributes with information to be passed along with the methodcall/
 * methodresponse.
 * 
 * For now, I go for the simple solution: Just to add additional parameters
 * to this method
 * 
 * @author morten
 *
 */
public class TR069Method {
	/*
	 * The request-response methods (ACS -> CPE)
	 */
	public static final String GET_PARAMETER_VALUES = "GetParameterValues";
	public static final String GET_PARAMETER_NAMES = "GetParameterNames";
	public static final String SET_PARAMETER_VALUES = "SetParameterValues";
	public static final String DOWNLOAD = "Download";
	public static final String REBOOT = "Reboot";
	public static final String GET_RPC_METHODS = "GetRPCMethods";

	/*
	 * The response-response methods (ACS -> CPE)
	 */
	public static final String TRANSFER_COMPLETE = "TransferCompleteReponse";
	public static final String INFORM = "InformResponse";

	/*
	 * The end-of-communication response (ACS -> CPE)
	 */

	public static final String EMPTY = "Empty";

	public static final Map<String, Integer> methodMap = new HashMap<String, Integer>();
	static {
		methodMap.put(EMPTY, Status.EM);
		methodMap.put(GET_PARAMETER_VALUES, Status.GPV);
		methodMap.put(GET_PARAMETER_NAMES, Status.GPN);
		methodMap.put(SET_PARAMETER_VALUES, Status.SPV);
		methodMap.put(DOWNLOAD, Status.DO);
		methodMap.put(REBOOT, Status.RE);
		methodMap.put(GET_RPC_METHODS, Status.GRM);
		methodMap.put(TRANSFER_COMPLETE, Status.TC);
		methodMap.put(INFORM, Status.IN);

	}

	public static int getState(String method) {
		return methodMap.get(method);
	}

	public static String getMethod(String response) {
		if (response == null || response.equals(""))
			return TR069Method.EMPTY;
		for (String method : methodMap.keySet()) {
			if (response.indexOf(method) > -1)
				return method;
		}
		throw new RuntimeException("[EMPTY] Wrong response from server: " + response);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String executeMethod(String method, Arguments args, HttpHandler httpHandler, Status status) {
		try {
			Class c = Class.forName("com.owera.tr069client.messages." + method);
			Class[] parameterTypes = new Class[] { Arguments.class, HttpHandler.class, Status.class };
			Method m = c.getMethod("execute", parameterTypes);
			Object response = m.invoke(null, new Object[] { args, httpHandler, status });
			return (String) response;
		} catch (Throwable t) {
			System.err.println("An error occurred: " + t);
			return null;
		}

	}
}
