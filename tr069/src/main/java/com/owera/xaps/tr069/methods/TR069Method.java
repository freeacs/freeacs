package com.owera.xaps.tr069.methods;

import java.util.HashMap;
import java.util.Map;

import com.owera.xaps.base.Log;

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

//	public static Map<String, TR069MethodAssociations> TR069MethodMap = new HashMap<String, TR069MethodAssociations>();

	/* Map of all (SOAP/HTTP-)request actions and what to do next */
	public static Map<String, HTTPRequestAction> requestMap = new HashMap<String, HTTPRequestAction>();
	/* Map of all (SOAP/HTTP-)response actions and what to return */
	public static Map<String, HTTPResponseAction> responseMap = new HashMap<String, HTTPResponseAction>();
	/* Map of all abbreviations - only used in event-logging */
	public static Map<String, String> abbrevMap = new HashMap<String, String>();

	static {
		try {
			abbrevMap.put(EMPTY, "EM");
			requestMap.put(EMPTY, new HTTPRequestAction(DoNotProcessReq.class, EMDecision.class));
			responseMap.put(EMPTY, new HTTPResponseAction("buildEM"));

			abbrevMap.put(GET_RPC_METHODS, "GRM");
			abbrevMap.put(GET_RPC_METHODS_RES, "GRM");
			requestMap.put(GET_RPC_METHODS, new HTTPRequestAction(DoNotProcessReq.class, GET_RPC_METHODS_RES));
			requestMap.put(GET_RPC_METHODS_RES, new HTTPRequestAction(DoNotProcessReq.class, GET_PARAMETER_VALUES));
			responseMap.put(GET_RPC_METHODS, new HTTPResponseAction("buildGRMReq"));
			responseMap.put(GET_RPC_METHODS_RES, new HTTPResponseAction("buildGRMRes"));

			abbrevMap.put(GET_PARAMETER_NAMES, "GPN");
			requestMap.put(GET_PARAMETER_NAMES, new HTTPRequestAction(GPNres.class, GET_PARAMETER_VALUES));
			responseMap.put(GET_PARAMETER_NAMES, new HTTPResponseAction("buildGPN"));

			abbrevMap.put(INFORM, "IN");
			requestMap.put(INFORM, new HTTPRequestAction(INreq.class, INFORM));
			responseMap.put(INFORM, new HTTPResponseAction("buildIN"));

			abbrevMap.put(GET_PARAMETER_VALUES, "GPV");
			requestMap.put(GET_PARAMETER_VALUES, new HTTPRequestAction(GPVres.class, GPVDecision.class));
			responseMap.put(GET_PARAMETER_VALUES, new HTTPResponseAction("buildGPV"));
			
			abbrevMap.put(SET_PARAMETER_VALUES, "SPV");
			requestMap.put(SET_PARAMETER_VALUES, new HTTPRequestAction(SPVres.class, SPVDecision.class));
			responseMap.put(SET_PARAMETER_VALUES, new HTTPResponseAction("buildSPV"));
			
			abbrevMap.put(GET_PARAMETER_ATTRIBUTES, "GPA");
			requestMap.put(GET_PARAMETER_ATTRIBUTES, new HTTPRequestAction(GPAres.class, GPADecision.class));
			responseMap.put(GET_PARAMETER_ATTRIBUTES, new HTTPResponseAction("buildGPA"));
			
			abbrevMap.put(SET_PARAMETER_ATTRIBUTES, "SPA");
			requestMap.put(SET_PARAMETER_ATTRIBUTES, new HTTPRequestAction(SPAres.class, SPADecision.class));
			responseMap.put(SET_PARAMETER_ATTRIBUTES, new HTTPResponseAction("buildSPA"));

			abbrevMap.put(TRANSFER_COMPLETE, "TC");
			requestMap.put(TRANSFER_COMPLETE, new HTTPRequestAction(TCreq.class, TCDecision.class));
			responseMap.put(TRANSFER_COMPLETE, new HTTPResponseAction("buildTC"));

			abbrevMap.put(DOWNLOAD, "DO");
			requestMap.put(DOWNLOAD, new HTTPRequestAction(DOres.class, EMPTY));
			responseMap.put(DOWNLOAD, new HTTPResponseAction("buildDO"));
			
			abbrevMap.put(FAULT, "FA");
			requestMap.put(FAULT, new HTTPRequestAction(FAres.class, FADecision.class));

			abbrevMap.put(REBOOT, "RE");
			requestMap.put(REBOOT, new HTTPRequestAction(REres.class, EMPTY));
			responseMap.put(REBOOT, new HTTPResponseAction("buildRE"));

			abbrevMap.put(FACTORY_RESET, "FR");
			requestMap.put(FACTORY_RESET, new HTTPRequestAction(FRres.class, EMPTY));
			responseMap.put(FACTORY_RESET, new HTTPResponseAction("buildFR"));

			abbrevMap.put(CUSTOM, "CU");
			responseMap.put(CUSTOM, new HTTPResponseAction("buildCU"));
		} catch (Throwable t) {
			Log.fatal(TR069Method.class, "The buildup of TR069 Method Associations failed. TR069 server must stop.", t);
			System.exit(1);
		}
	}

}
