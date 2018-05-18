package com.github.freeacs.tr069.xml;

import java.util.ArrayList;

public class MethodList {

	private ArrayList<String> methods;
    
    public MethodList() {
        this.methods = new ArrayList<String>();
    }
    
    public ArrayList<String> getMethods() {
        return methods;
    }
    
    public String getMethod(int index) {
        return methods.get(index);
    }
    
    public void setMethods(ArrayList<String> methods) {
        this.methods = methods;
    }
    
    public void addMethod(String method) {
        this.methods.add(method);
    }

    public boolean contains(String method) {
        return methods.contains(method);
    }
}
