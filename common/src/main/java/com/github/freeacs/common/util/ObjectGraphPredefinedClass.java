package com.github.freeacs.common.util;

import java.util.Stack;

@SuppressWarnings("rawtypes")
public class ObjectGraphPredefinedClass {
	private long initalMemoryUsage;
	private long additionalMemoryUsage;
	private Class clazz;
	private String objectName;
	private int hashCodeOfParentObject;

	public ObjectGraphPredefinedClass(Class c, long init, long add) {
		initalMemoryUsage = init;
		additionalMemoryUsage = add;
		clazz = c;
	}

	public void makeObjectGraph(String objectName, Stack<Object> s, ObjectGraph og) {
		ObjectGraph newOg = new ObjectGraph();
		newOg.setInstanceCount(1);
		boolean firstCreation = false;
		if (this.objectName == null) {
			this.objectName = objectName;
			if (s != null && s.size() > 0) {
				Object o = s.pop();
				hashCodeOfParentObject = o.hashCode();
				s.push(o);
			}
			firstCreation = true;
		} else {
			int aHashCode = 0;
			if (s != null && s.size() > 0) {
				Object o = s.pop();
				aHashCode = o.hashCode();
				s.push(o);
			}
			if (aHashCode == hashCodeOfParentObject && this.objectName.equals(objectName)) {
				firstCreation = true;
			}
		}
		if (firstCreation)
			newOg.setMemUsage(ObjectGraphBuilder.OBJECTREF + ObjectGraphBuilder.OBJECTSIZE + initalMemoryUsage);
		else
			newOg.setMemUsage(ObjectGraphBuilder.OBJECTREF + ObjectGraphBuilder.OBJECTSIZE + additionalMemoryUsage);
		newOg.setObjectName(objectName);
		newOg.setClassName(clazz.getName());
		og.addMemUsage(newOg.getMemUsage());
		og.getObjectMap().put(objectName, newOg);
	}

}
