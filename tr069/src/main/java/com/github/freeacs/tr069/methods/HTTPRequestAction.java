package com.github.freeacs.tr069.methods;

import com.github.freeacs.tr069.HTTPReqResData;
import com.github.freeacs.tr069.exception.TR069Exception;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public class HTTPRequestAction {

	private CheckedRequestFunction processRequestMethod;

	private CheckedRequestFunction decisionMakerMethod;

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


	public CheckedRequestFunction getProcessRequestMethod() {
		return processRequestMethod;
	}

	public CheckedRequestFunction getDecisionMakerMethod() {
		return decisionMakerMethod;
	}

	@FunctionalInterface
	public interface CheckedRequestFunction {
		void apply(HTTPReqResData t) throws NoSuchAlgorithmException, SQLException, TR069Exception;
	}

}
