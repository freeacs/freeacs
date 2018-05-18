package com.github.freeacs.tr069.methods;

import com.github.freeacs.tr069.HTTPReqResData;
import com.github.freeacs.tr069.exception.TR069Exception;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public class HTTPRequestAction {

	private CheckedRequestFunction processRequestMethod;

	private CheckedRequestFunction decisionMakerMethod;

	private String nextMethod;

	/*
	 * reqClass: The class must provide a process(RequestResponse) method. This method is responsible
	 * for processing the incoming request.
	 * decideClass: In case there are several options of what to do next (depending upon the 
	 * information extracted in reqClass), we need a decideClass with a process(RequestResponse) method.
	 */
	public HTTPRequestAction(CheckedRequestFunction processRequestMethod, CheckedRequestFunction decisionMakerMethod) {
		this.processRequestMethod = processRequestMethod;
		this.decisionMakerMethod = decisionMakerMethod;
	}
	
	/*
	 * reqClass: The class must provide a process(RequestResponse) method. This method is responsible
	 * for processing the incoming request.
	 * nextMethod: the response-method which should follow this incoming "request"
	 * shortname: only used in monitoring-page
	 */
	public HTTPRequestAction(CheckedRequestFunction processRequestMethod, String nextMethod) {
		this.processRequestMethod = processRequestMethod;
		this.nextMethod = nextMethod;
	}
	

	public CheckedRequestFunction getProcessRequestMethod() {
		return processRequestMethod;
	}

	public CheckedRequestFunction getDecisionMakerMethod() {
		return decisionMakerMethod;
	}

	public String getNextMethod() {
		return nextMethod;
	}

	@FunctionalInterface
	public interface CheckedRequestFunction {
		void apply(HTTPReqResData t) throws NoSuchAlgorithmException, SQLException, TR069Exception;
	}

}
