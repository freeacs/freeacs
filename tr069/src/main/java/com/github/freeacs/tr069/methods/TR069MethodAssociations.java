package com.github.freeacs.tr069.methods;

import com.github.freeacs.tr069.HTTPReqResData;

import java.lang.reflect.Method;

@SuppressWarnings("rawtypes")
public class TR069MethodAssociations {

	private Method processRequestMethod;

	private Method createResponseMethod;

	private Method decisionMakerMethod;

	private String nextMethod;

	private String shortname;

	public TR069MethodAssociations(Class reqClass, String nextMethod, Class decideClass, String responseMethod, String shortname) throws NoSuchMethodException {
		setProcessRequestMethod(reqClass);
		setDecisionMakerMethod(decideClass, nextMethod);
		setCreateResponseMethod(responseMethod);
		this.shortname = shortname;
	}

	//	public TR069MethodAssociations(Method requestM, Method responseM, Method decisionM, String shortName) {
	//		this.processRequestMethod = requestM;
	//		this.createResponseMethod = responseM;
	//		this.decisionMakerMethod = decisionM;
	//		this.shortname = shortName;
	//	}

	public Method getProcessRequestMethod() {
		return processRequestMethod;
	}

	public Method getCreateResponseMethod() {
		return createResponseMethod;
	}

	public Method getDecisionMakerMethod() {
		return decisionMakerMethod;
	}

	public String getShortname() {
		return shortname;
	}

	@SuppressWarnings("unchecked")
	public void setProcessRequestMethod(Class requestProcessor) throws NoSuchMethodException {
		this.processRequestMethod = requestProcessor.getMethod("process", HTTPReqResData.class);
	}

	public void setCreateResponseMethod(String methodName) throws NoSuchMethodException {
		if (methodName != null)
			this.createResponseMethod = HTTPResponseCreator.class.getDeclaredMethod(methodName, HTTPReqResData.class);
	}

	@SuppressWarnings("unchecked")
	public void setDecisionMakerMethod(Class decisionMakerClass, String nextMethod) throws NoSuchMethodException {
		if (decisionMakerClass != null)
			this.decisionMakerMethod = decisionMakerClass.getMethod("process", HTTPReqResData.class);
		else
			this.nextMethod = nextMethod;
	}

	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	public String getNextMethod() {
		return nextMethod;
	}

	public void setNextMethod(String nextMethod) {
		this.nextMethod = nextMethod;
	}
}
