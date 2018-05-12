package com.github.freeacs.tr069;


/**
 * There is really no good reason to make a class for providing these namespaces. The only
 * half-good reason is that I have seen CPEs with various namespace-style, and that the
 * xml-specification of TR-069 changed from 2004 to 2006. Change in namespace-name should
 * not affect the parser or anything, but to be sure I have extracted this information, so
 * it will be easy to change IF needs be.
 * 
 * April 2008: I really feel we should remove this...
 * June 2009: I feel the same still..but never fix something that works;)
 */
public class Namespace {
	public static String getSoapEnvNS() {
		return "soapenv";
	}

	public static String getSoapEncNS() {
		return "soapenc";
	}
}
