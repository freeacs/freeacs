package com.owera.xaps.tr069.methods;

import java.lang.reflect.Method;

import com.owera.xaps.tr069.HTTPReqResData;


@SuppressWarnings("rawtypes")
public class HTTPRequestAction {

	private Method processRequestMethod;

	private Method decisionMakerMethod;

	private String nextMethod;

	/*
	 * reqClass: The class must provide a process(RequestResponse) method. This method is responsible
	 * for processing the incoming request.
	 * decideClass: In case there are several options of what to do next (depending upon the 
	 * information extracted in reqClass), we need a decideClass with a process(RequestResponse) method.
	 */
	public HTTPRequestAction(Class reqClass, Class decideClass) throws NoSuchMethodException {
		setProcessRequestMethod(reqClass);
		setDecisionMakerMethod(decideClass);
	}
	
	/*
	 * reqClass: The class must provide a process(RequestResponse) method. This method is responsible
	 * for processing the incoming request.
	 * nextMethod: the response-method which should follow this incoming "request"
	 * shortname: only used in monitoring-page
	 */
	public HTTPRequestAction(Class reqClass, String nextMethod) throws NoSuchMethodException {
		setProcessRequestMethod(reqClass);
		this.nextMethod = nextMethod;
	}
	

	public Method getProcessRequestMethod() {
		return processRequestMethod;
	}

	public Method getDecisionMakerMethod() {
		return decisionMakerMethod;
	}

	@SuppressWarnings("unchecked")
	public void setProcessRequestMethod(Class requestProcessor) throws NoSuchMethodException {
		this.processRequestMethod = requestProcessor.getMethod("process", HTTPReqResData.class);
	}

	@SuppressWarnings("unchecked")
	public void setDecisionMakerMethod(Class decisionMakerClass) throws NoSuchMethodException {
		this.decisionMakerMethod = decisionMakerClass.getMethod("process", HTTPReqResData.class);
	}

	public String getNextMethod() {
		return nextMethod;
	}

	public void setNextMethod(String nextMethod) {
		this.nextMethod = nextMethod;
	}
}
