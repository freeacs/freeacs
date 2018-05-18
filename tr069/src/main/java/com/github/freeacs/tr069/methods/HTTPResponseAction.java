package com.github.freeacs.tr069.methods;

import com.github.freeacs.tr069.HTTPReqResData;
import com.github.freeacs.tr069.xml.Response;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public class HTTPResponseAction {

	private CheckedResponseFunction createResponseMethod;

	public HTTPResponseAction(CheckedResponseFunction responseMethod) {
		this.createResponseMethod = responseMethod;
	}

	public CheckedResponseFunction getCreateResponseMethod() {
		return createResponseMethod;
	}

	@FunctionalInterface
	public interface CheckedResponseFunction {
		Response apply(HTTPReqResData t) throws NoSuchAlgorithmException, SQLException;
	}

}
