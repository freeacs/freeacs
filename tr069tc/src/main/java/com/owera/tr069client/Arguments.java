package com.owera.tr069client;


public interface Arguments {

	public int getMinutesToRunPrStep();
	public int getNumberOfThreadsPrStep();
	public int getNumberOfSteps();
	public String getProvUrl();
	public int getBitRate();
	public int getDownload();
	public int getFailureEvery();
	public int getHangupEvery();
	public String getRange();
	public boolean isAuthenticate();
	public void setAuthenticate(boolean authenticate);
	public Arguments clone();
	public String getInitialSoftwareVersion();
}
