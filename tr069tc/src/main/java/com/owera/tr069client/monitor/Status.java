package com.owera.tr069client.monitor;

public class Status {

	public static int INIT = 0;
	public static int AU = 1;
	public static int IN = 2;
	public static int GPN = 3;
	public static int GPV = 4;
	public static int SPV = 5;
	public static int DO = 6;
	public static int TC = 7;
	public static int EM = 8;
	public static int WAIT = 9;
	public static int FIN = 10;
	public static int RE = 11;
	public static int GRM = 12;

	public static String[] names = new String[] { "INIT", "AUTH", "INFO", "GPNA", "GPVA", "SPVA", "DOWN", "TRCO", "EMPT", "WAIT", "FINI", "REBO", "GRME"};


	// uses the constants above
	private int currentOperation;

	// denotes which operation the error occured within
	private int errorOcurred;

	// denotes which operation the retry occured within
	private int[] retryOccuredArr = new int[13];

	private int servedOK;
	private int servedFailed;
	private int retrySleep;

	public Status() {
		this.currentOperation = INIT;
	}

	public int getCurrentOperation() {
		return currentOperation;
	}

	public void setCurrentOperation(int currentOperation) {
		this.currentOperation = currentOperation;
	}

	public int getErrorOcurred() {
		return errorOcurred;
	}

	public void setErrorOcurred(int errorOcurred) {
		this.errorOcurred = errorOcurred;
	}

	public void incRetryOccured(int retryOccured) {
		retryOccuredArr[retryOccured]++;
	}

	public int getRetrySleep() {
		return retrySleep;
	}

	public void setRetrySleep(int retrySleep) {
		this.retrySleep = retrySleep;
	}

	public int getServedFailed() {
		return servedFailed;
	}

	public void setServedFailed(int servedFailed) {
		this.servedFailed = servedFailed;
	}

	public int getServedOK() {
		return servedOK;
	}

	public void setServedOK(int servedOK) {
		this.servedOK = servedOK;
	}

	public int[] getRetryOccuredArr() {
		return retryOccuredArr;
	}

	public void setRetryOccuredArr(int[] retryOccuredArr) {
		this.retryOccuredArr = retryOccuredArr;
	}

}
