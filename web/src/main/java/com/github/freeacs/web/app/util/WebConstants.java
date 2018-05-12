package com.github.freeacs.web.app.util;


import com.github.freeacs.web.Page;

/**
 * Static and contant web properties and variables.
 *  
 * @author Jarl Andre Hubenthal
 *
 */
public final class WebConstants {
	
	public static final String WEB_PROPS_ADMIN 				    = "administrator";
	public static final String WEB_PROPS_SUPPORT 			    = "customercare";
	public static final int PARAMETERS_NEXT_INDENTATION 	    = 25;
	public static final int PARAMETERS_START_INDENTATION 	    = 4;
	public static final String FORWARDED_CPE_INTERFACE 		    = "System.X_OWERA-COM.OPP.Forwarder.Sockets.1.URI";
	public static final String KEYROOT_INTERNET_GATEWAY_DEVICE 	= "InternetGatewayDevice.";
	public static final String KEYROOT_DEVICE 					= "Device.";
	public static final String KEYROOT_SYSTEM 					= "System.";
	public static final String ALL_ITEMS_OR_DEFAULT			    = ".";
	public static final String LOGOUT_URL 					    = Page.LOGIN.getUrl("logoff=true");
	public static final String ALL_PAGES 					    = "OWERA_ALL_PAGES";
	public static final String DB_LOGIN_URL 				    = Page.LOGIN.getUrl();
	public static final String DEFAULT_PROPERTIES_KEY 		    = "properties";
	public static final String DELETE 						    = "Delete";
	public static final String UPDATE 						    = "Update";
	public static final String UPDATE_PARAMS 				    = "Update parameters";
}