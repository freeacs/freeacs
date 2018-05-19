package com.github.freeacs.tr069.methods;


import java.util.HashMap;
import java.util.Map;

public class TR069Method {
	public static final String EMPTY = "Empty";

	public static final String INFORM = "Inform";

	public static final String GET_PARAMETER_VALUES = "GetParameterValues";

	public static final String SET_PARAMETER_VALUES = "SetParameterValues";

	public static final String TRANSFER_COMPLETE = "TransferComplete";

	public static final String DOWNLOAD = "Download";

	public static final String FAULT = "Fault";

	public static final String GET_PARAMETER_NAMES = "GetParameterNames";

	public static final String GET_RPC_METHODS = "GetRPCMethods";

	public static final String GET_RPC_METHODS_RES = "GetRPCMethodsResponse";

	public static final String REBOOT = "Reboot";

	public static final String CUSTOM = "Custom";

	public static final String FACTORY_RESET = "FactoryReset";
	
	public static final String GET_PARAMETER_ATTRIBUTES = "GetParameterAttributes";
	
	public static final String SET_PARAMETER_ATTRIBUTES = "SetParameterAttributes";

	/* Map of all (SOAP/HTTP-)request actions and what to do next */
	public static Map<String, HTTPRequestAction> requestMap = new HashMap<String, HTTPRequestAction>();
	/* Map of all (SOAP/HTTP-)response actions and what to return */
	public static Map<String, HTTPResponseAction> responseMap = new HashMap<String, HTTPResponseAction>();
	/* Map of all abbreviations - only used in event-logging */
	public static Map<String, String> abbrevMap = new HashMap<String, String>();

	static {
		abbrevMap.put(EMPTY, "EM");
		requestMap.put(EMPTY, new HTTPRequestAction(DoNotProcessReq::process, EMDecision::process));
		responseMap.put(EMPTY, new HTTPResponseAction(HTTPResponseCreator::buildEM));

		abbrevMap.put(GET_RPC_METHODS, "GRM");
		abbrevMap.put(GET_RPC_METHODS_RES, "GRM");
		requestMap.put(GET_RPC_METHODS, new HTTPRequestAction(DoNotProcessReq::process, makeSimpleDecision(GET_RPC_METHODS_RES)));
		requestMap.put(GET_RPC_METHODS_RES, new HTTPRequestAction(DoNotProcessReq::process, makeSimpleDecision(GET_PARAMETER_VALUES)));
		responseMap.put(GET_RPC_METHODS, new HTTPResponseAction(HTTPResponseCreator::buildGRMReq));
		responseMap.put(GET_RPC_METHODS_RES, new HTTPResponseAction(HTTPResponseCreator::buildGRMRes));

		abbrevMap.put(GET_PARAMETER_NAMES, "GPN");
		requestMap.put(GET_PARAMETER_NAMES, new HTTPRequestAction(GPNres::process, makeSimpleDecision(GET_PARAMETER_VALUES)));
		responseMap.put(GET_PARAMETER_NAMES, new HTTPResponseAction(HTTPResponseCreator::buildGPN));

		abbrevMap.put(INFORM, "IN");
		requestMap.put(INFORM, new HTTPRequestAction(INreq::process, makeSimpleDecision(INFORM)));
		responseMap.put(INFORM, new HTTPResponseAction(HTTPResponseCreator::buildIN));

		abbrevMap.put(GET_PARAMETER_VALUES, "GPV");
		requestMap.put(GET_PARAMETER_VALUES, new HTTPRequestAction(GPVres::process, GPVDecision::process));
		responseMap.put(GET_PARAMETER_VALUES, new HTTPResponseAction(HTTPResponseCreator::buildGPV));

		abbrevMap.put(SET_PARAMETER_VALUES, "SPV");
		requestMap.put(SET_PARAMETER_VALUES, new HTTPRequestAction(SPVres::process, SPVDecision::process));
		responseMap.put(SET_PARAMETER_VALUES, new HTTPResponseAction(HTTPResponseCreator::buildSPV));

		abbrevMap.put(GET_PARAMETER_ATTRIBUTES, "GPA");
		requestMap.put(GET_PARAMETER_ATTRIBUTES, new HTTPRequestAction(GPAres::process, GPADecision::process));
		responseMap.put(GET_PARAMETER_ATTRIBUTES, new HTTPResponseAction(HTTPResponseCreator::buildGPA));

		abbrevMap.put(SET_PARAMETER_ATTRIBUTES, "SPA");
		requestMap.put(SET_PARAMETER_ATTRIBUTES, new HTTPRequestAction(SPAres::process, SPADecision::process));
		responseMap.put(SET_PARAMETER_ATTRIBUTES, new HTTPResponseAction(HTTPResponseCreator::buildSPA));

		abbrevMap.put(TRANSFER_COMPLETE, "TC");
		requestMap.put(TRANSFER_COMPLETE, new HTTPRequestAction(TCreq::process, TCDecision::process));
		responseMap.put(TRANSFER_COMPLETE, new HTTPResponseAction(HTTPResponseCreator::buildTC));

		abbrevMap.put(DOWNLOAD, "DO");
		requestMap.put(DOWNLOAD, new HTTPRequestAction(DOres::process, makeSimpleDecision(EMPTY)));
		responseMap.put(DOWNLOAD, new HTTPResponseAction(HTTPResponseCreator::buildDO));

		abbrevMap.put(FAULT, "FA");
		requestMap.put(FAULT, new HTTPRequestAction(FAres::process, FADecision::process));

		abbrevMap.put(REBOOT, "RE");
		requestMap.put(REBOOT, new HTTPRequestAction(REres::process, makeSimpleDecision(EMPTY)));
		responseMap.put(REBOOT, new HTTPResponseAction(HTTPResponseCreator::buildRE));

		abbrevMap.put(FACTORY_RESET, "FR");
		requestMap.put(FACTORY_RESET, new HTTPRequestAction(FRres::process, makeSimpleDecision(EMPTY)));
		responseMap.put(FACTORY_RESET, new HTTPResponseAction(HTTPResponseCreator::buildFR));

		abbrevMap.put(CUSTOM, "CU");
		responseMap.put(CUSTOM, new HTTPResponseAction(HTTPResponseCreator::buildCU));
	}

	private static HTTPRequestAction.CheckedRequestFunction makeSimpleDecision(String getRpcMethodsRes) {
		return (reqRes) -> reqRes.getResponse().setMethod(getRpcMethodsRes);
	}

}
