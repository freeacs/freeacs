package com.github.freeacs.shell.command;

public interface Substitute {

	//	public boolean isProcessed();
	//
	//	public void setProcessed(boolean processed);
	//
	//	public boolean isChanged();
	//
	//	public void setChanged(boolean changed);

	public void resetToOriginalState();
	
	public String getStringToSubstitute();

	public void setSubstitutedString(String s);
}
