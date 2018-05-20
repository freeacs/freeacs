package com.github.freeacs.ws.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Properties {

	public static String REDIRECT_URL;

	@Value("${redirect-url:#{null}}")
	public static void setRedirectUrl(String url) {
		REDIRECT_URL = url;
	}
}
