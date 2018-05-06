/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.owera.xaps.web.app.page.job;

import com.owera.xaps.dbi.Job;
import com.owera.xaps.dbi.UnittypeParameter;
import com.owera.xaps.dbi.util.SystemParameters;

public class JobFilter {

	private Job job;

	public JobFilter(Job j) {
		this.job = j;
	}

	public boolean listParameter(UnittypeParameter utp) {
		switch (job.getFlags().getType()) {
		case CONFIG:
			return true;
		case SOFTWARE:
			return false; // isSoftwareParameter(utp);
		case RESTART:
			return false; // isRestartParameter(utp);
		case RESET:
			return false; // isResetParameter(utp);
		case TR069_SCRIPT:
			return isScriptParameter(utp);
		default: // SHELL, KICK, TELNET
			return false;
		}
	}

	private boolean isScriptParameter(UnittypeParameter utp) {
		return utp.getName().contains(SystemParameters.DESIRED_TR069_SCRIPT);
	}

	//	private boolean isRestartParameter(UnittypeParameter utp) {
	//		return SystemParameters.RESTART.equals(utp.getName());
	//	}
	//
	//	private boolean isResetParameter(UnittypeParameter utp) {
	//		return SystemParameters.RESET.equals(utp.getName());
	//	}
	//
	//	private boolean isSoftwareParameter(UnittypeParameter utp) {
	//		List<String> params = Arrays.asList(SystemParameters.DESIRED_SOFTWARE_VERSION, SystemParameters.SOFTWARE_URL);
	//		return params.contains(utp.getName());
	//	}
}
