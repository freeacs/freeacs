package com.owera.xaps.web.app.page.report;

import java.util.Arrays;
import java.util.List;

import com.owera.xaps.dbi.report.Record;
import com.owera.xaps.dbi.report.RecordGatewayTR;
import com.owera.xaps.dbi.report.RecordGroup;
import com.owera.xaps.dbi.report.RecordHardware;
import com.owera.xaps.dbi.report.RecordHardwareTR;
import com.owera.xaps.dbi.report.RecordJob;
import com.owera.xaps.dbi.report.RecordProvisioning;
import com.owera.xaps.dbi.report.RecordSyslog;
import com.owera.xaps.dbi.report.RecordUnit;
import com.owera.xaps.dbi.report.RecordVoip;
import com.owera.xaps.dbi.report.RecordVoipTR;


/**
 * Defines the request parameter type value for each report type and also the corresponding methods.
 * 
 * @author Jarl Andre Hubenthal
 *
 */
public enum ReportType {
	
	/** The UNIT. */
	UNIT ("Unit",removeGetFromBeginningOfStrings(Record.getCounterAndAveragesMethods(RecordUnit.class))),
	
	/** The VOIP. */
	VOIP ("Voip",removeGetFromBeginningOfStrings(Record.getCounterAndAveragesMethods(RecordVoip.class))),
	
	/** The VOIP TR. */
	VOIPTR ("VoipTR",removeGetFromBeginningOfStrings(Record.getCounterAndAveragesMethods(RecordVoipTR.class))),
	
	/** The GROUP. */
	GROUP ("Group",removeGetFromBeginningOfStrings(Record.getCounterAndAveragesMethods(RecordGroup.class))),
	
	/** The JOB. */
	JOB ("Job",removeGetFromBeginningOfStrings(Record.getCounterAndAveragesMethods(RecordJob.class))),
	
	/** The PROV. */
	PROV ("Prov",removeGetFromBeginningOfStrings(Record.getCounterAndAveragesMethods(RecordProvisioning.class))),
	
	/** The SYS. */
	SYS ("Syslog",removeGetFromBeginningOfStrings(Record.getCounterAndAveragesMethods(RecordSyslog.class))),
	
	/** The HARDWARE. */
	HARDWARE("Hardware",removeGetFromBeginningOfStrings(Record.getCounterAndAveragesMethods(RecordHardware.class))),
	
	/** The HARDWARETR. */
	HARDWARETR("HardwareTR",removeGetFromBeginningOfStrings(Record.getCounterAndAveragesMethods(RecordHardwareTR.class))),
	
	/** The GATEWAYTR. */
	GATEWAYTR("GatewayTR",removeGetFromBeginningOfStrings(Record.getCounterAndAveragesMethods(RecordGatewayTR.class))),
	
	/** The NONE. */
	NONE ("","");
	
	/** The name. */
	private String name;
	
	/** The methods. */
	private List<String> methods;
	
	/**
	 * Instantiates a new report type.
	 *
	 * @param name the name
	 * @param methods the methods
	 */
	private ReportType(String name,String... methods){
		this.name=name;
		this.methods = Arrays.asList(methods);
	}
	
	/**
	 * Gets the methods.
	 *
	 * @return the methods
	 */
	public List<String> getMethods(){
		return this.methods;
	}
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName(){
		return this.name;
	}
	
	/**
	 * Gets the enum.
	 *
	 * @param name the name
	 * @return the enum
	 */
	public static ReportType getEnum(String name){
		if(name==null)
			return NONE;
		
		for(ReportType rp: values()){
			if(rp.getName().equals(name))
				return rp;
		}
		
		return NONE;
	}
	
	/**
	 * Removes the get from beginning of strings.
	 *
	 * @param arr the arr
	 * @return the string[]
	 */
	private static String[] removeGetFromBeginningOfStrings(String[] arr){
		for(int i=0;i<arr.length;i++){
			if(arr[i].startsWith("get")){
				arr[i] = arr[i].substring(3);
			}
		}
		return arr;
	}
}
