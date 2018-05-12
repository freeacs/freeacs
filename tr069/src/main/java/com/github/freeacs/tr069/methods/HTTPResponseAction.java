package com.github.freeacs.tr069.methods;

import com.github.freeacs.tr069.HTTPReqResData;
import com.owera.xaps.tr069.HTTPReqResData;

import java.lang.reflect.Method;

public class HTTPResponseAction {

	private Method createResponseMethod;

	public HTTPResponseAction(String responseMethod) throws NoSuchMethodException {
		setCreateResponseMethod(responseMethod);
	}

	public Method getCreateResponseMethod() {
		return createResponseMethod;
	}

	public void setCreateResponseMethod(String methodName) throws NoSuchMethodException {
		if (methodName != null)
			this.createResponseMethod = HTTPResponseCreator.class.getDeclaredMethod(methodName, HTTPReqResData.class);
	}

}
