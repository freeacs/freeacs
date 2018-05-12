package com.owera.common.util;

import java.util.Map;
import java.util.TreeMap;

public class ObjectGraph {

	private Map<String, Object> objectMap = new TreeMap<String, Object>(new ObjectGraphComparator());
	private String className;
	private String objectName;
	private long memUsage = 0;
	private long instanceCount = 0;

	public void addMemUsage(long memoryUsage) {
		memUsage += memoryUsage;
	}

	public long getInstanceCount() {
		return instanceCount;
	}

	public long getMemUsage() {
		return memUsage;
	}

	public void setInstanceCount(long l) {
		instanceCount = l;
	}

	public void setMemUsage(long l) {
		memUsage = l;
	}

	public Map<String, Object> getObjectMap() {
		return objectMap;
	}

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String string) {
		objectName = string;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String string) {
		className = string;
	}

	public String toString() {
		return toString(this, "\t");
	}

	public String toString(ObjectGraph og, String tabs) {
		Object[] values = og.getObjectMap().values().toArray();
		String retVal = "";
		if (og.getInstanceCount() > 1)
			retVal += og.getObjectName() + " " + og.getClassName() + "[" + og.getInstanceCount() + "] (" + og.getMemUsage() + " bytes)";
		else
			retVal += og.getObjectName() + " " + og.getClassName() + " (" + og.getMemUsage() + " bytes)";
		for (int i = 0; i < values.length; i++) {
			ObjectGraph newOg = (ObjectGraph) values[i];
			retVal += "\n" + tabs + toString(newOg, tabs + "\t");
		}
		return retVal;
	}

}
