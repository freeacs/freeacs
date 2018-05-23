package com.github.freeacs.web.app.page.job;

import com.github.freeacs.dbi.*;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.web.Page;
import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.*;
import com.github.freeacs.web.app.menu.MenuItem;
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.util.ACSLoader;
import com.github.freeacs.web.app.util.SessionCache;
import com.github.freeacs.web.app.util.SessionData;
import com.github.freeacs.web.app.util.WebConstants;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class UnitJobPage extends AbstractWebPage {
	// Do NOT static this variable, contains singletons that should NOT be shared by different views
	//	private final JobStatusMethods jobStatusMethods = new JobStatusMethods();

	// FIXME Why are we using class variables? What are the problem we are solving with this?
	private JobData inputData;

	@Qualifier("main") DataSource mainDataSource;
	@Qualifier("syslog") DataSource syslogDataSource;

	private ACS acs;
	//	private Unittype unittype;
	//	private Group group;
	//	private Job job;

	private String sessionId;

	public void process(ParameterParser req, Output outputHandler, DataSource xapsDataSource, DataSource syslogDataSource) throws Exception {
		inputData = (JobData) InputDataRetriever.parseInto(new JobData(), req);

		sessionId = req.getSession().getId();

		acs = ACSLoader.getXAPS(sessionId, xapsDataSource, syslogDataSource);
		if (acs == null) {
			outputHandler.setRedirectTarget(WebConstants.DB_LOGIN_URL);
			return;
		}

		InputDataIntegrity.loadAndStoreSession(req, outputHandler, inputData, inputData.getUnittype(), inputData.getJob());

		Map<String, Object> fmMap = outputHandler.getTemplateMap();

		DropDownSingleSelect<Unittype> unittypes = InputSelectionFactory.getUnittypeSelection(inputData.getUnittype(), acs);
		fmMap.put("unittypes", unittypes);
		Unittype unittype = unittypes.getSelected();
		if (unittype != null) {
			String jobName = SessionCache.getSessionData(sessionId).getJobname();
			Job job = unittype.getJobs().getByName(jobName);

			if (inputData.getCmd().hasValue("exportfailedjobs")) {
				exportFailedUnitJobs(outputHandler, jobName);
				return;
			} else if (inputData.getCmd().hasValue("exportcompletedjobs")) {
				exportCompletedUnitJobs(outputHandler, jobName);
				return;
			} else if (inputData.getCmd().hasValue("getfailedunitjobs")) {
				getFailedUnitJobs(job, outputHandler);
				return;
			} else if (inputData.getCmd().hasValue("getcompletedunitjobs")) {
				getCompletedUnitJobs(job, outputHandler, unittype);
				return;
			}
		}
		outputHandler.setDirectToPage(Page.JOBSOVERVIEW);
	}

	public List<MenuItem> getShortcutItems(SessionData sessionData) {
		List<MenuItem> list = new ArrayList<MenuItem>();
		list.addAll(super.getShortcutItems(sessionData));
		if (sessionData.getUnittypeName() != null) {
			list.add(new MenuItem("Create new Job", Page.JOB).addCommand("create"));
			list.add(new MenuItem("Job overwiew", Page.JOBSOVERVIEW));
			if (sessionData.getJobname() != null) {
				Unittype unittype = acs.getUnittype(sessionData.getUnittypeName());
				Job job = unittype.getJobs().getByName(sessionData.getJobname());
				if (job != null) {
					list.add(new MenuItem("List failed unit jobs", Page.JOB).addCommand("getfailedunitjobs").addParameter("limit", "100").addParameter("unittype", job.getUnittype().getName())
							.addParameter("group", job.getGroup().getName()).addParameter("job", job.getName()));
					list.add(new MenuItem("List completed unit jobs", Page.JOB).addCommand("getcompletedunitjobs").addParameter("limit", "100").addParameter("unittype", job.getUnittype().getName())
							.addParameter("group", job.getGroup().getName()).addParameter("job", job.getName()));
				}
			}
		}
		return list;
	}

	/* Unit Job pages */

	private void exportFailedUnitJobs(Output res, String jobName) throws IOException {
		List<UnitJob> failedjobs = SessionCache.getSessionData(sessionId).getFailedUnitJobsList();
		StringBuilder string = new StringBuilder();
		if (failedjobs != null && failedjobs.size() > 0) {
			string.append("UnitId\t");
			string.append("Status\t");
			string.append("Started\t");
			string.append("Ended\t");
			string.append("Unconfirmed\t");
			string.append("Confirmed\t");
			string.append("\n");
			for (UnitJob unitJob : failedjobs) {
				string.append(unitJob.getUnitId()).append("\t");
				string.append(unitJob.getStatus()).append("\t");
				string.append(unitJob.getStartTimestamp().toString()).append("\t");
				string.append(unitJob.getEndTimestamp().toString()).append("\t");
				string.append(unitJob.getUnconfirmedFailed()).append("\t");
				string.append(unitJob.getConfirmedFailed()).append("\t");
				string.append("\n");
			}
		} else {
			string.append("There are no unit jobs to export");
		}
		res.setDownloadAttachment(jobName + "-unitjobs-failed.txt");
		res.setDirectResponse(string.toString());
	}

	private void exportCompletedUnitJobs(Output res, String jobName) throws IOException {
		Collection<Unit> completedjobs = SessionCache.getSessionData(sessionId).getCompletedUnitJobsList();
		StringBuilder string = new StringBuilder();
		if (completedjobs != null && completedjobs.size() > 0) {
			string.append("UnitId");
			string.append("\n");
			for (Unit unitJob : completedjobs) {
				string.append(unitJob.getId());
				string.append("\n");
			}
		} else {
			string.append("There are no unit jobs to export");
		}
		res.setDownloadAttachment(jobName + "-unitjobs-ok.txt");
		res.setContentType("text/plain");
		res.setDirectResponse(string.toString());
	}

	private void getFailedUnitJobs(Job job, Output res) throws SQLException, IOException, TemplateException {
		res.setTemplatePath("unit-job/failed");
		Map<String, Object> rootMap = new HashMap<String, Object>();
		UnitJobs unitJobs = new UnitJobs(mainDataSource);
		List<UnitJob> unitJobsList = unitJobs.readAllProcessed(job);
		//		List<UnitJob> list = new ArrayList<UnitJob>();
		//		if (limit != null) {
		//			for (int i = 0; i < limit && i <= unitJobsList.size() - 1; i++) {
		//				list.add(unitJobsList.get(i));
		//			}
		//			unitJobsList = list;
		//		}
		SessionCache.getSessionData(sessionId).setFailedUnitJobsList(null);
		if (unitJobsList.size() > 0) {
			rootMap.put("unitJobs", unitJobsList);
			SessionCache.getSessionData(sessionId).setFailedUnitJobsList(unitJobsList);
		}
		rootMap.put("job", job);
		rootMap.put("async", inputData.getAsync().getString());
		//		rootMap.put("limit", limit);
		res.getTemplateMap().putAll(rootMap);
	}

	private void getCompletedUnitJobs(Job job, Output res, Unittype unittype) throws SQLException, IOException, TemplateException {
		res.setTemplatePath("unit-job/completed");
		ACSUnit acsUnit = ACSLoader.getACSUnit(sessionId, mainDataSource, syslogDataSource);
		Profile profile = job.getGroup().getProfile();
		UnittypeParameter historyParameterUtp = job.getGroup().getUnittype().getUnittypeParameters().getByName(SystemParameters.JOB_HISTORY);
		Parameter historyParameter = new Parameter(historyParameterUtp, "%," + job.getId() + ":%");
		Collection<Unit> units = acsUnit.getUnits(unittype, profile, Arrays.asList(historyParameter), null).values();
		Map<String, Object> rootMap = new HashMap<String, Object>();
		SessionCache.getSessionData(sessionId).setFailedUnitJobsList(null);
		if (units.size() > 0) {
			rootMap.put("completedUnitJobs", units);
			rootMap.put("lastindexof", new LastIndexOfMethod());
			rootMap.put("getparamvalue", new GetParameterValue(acsUnit));
			rootMap.put("jobparameters", job.getDefaultParameters().values());
			SessionCache.getSessionData(sessionId).setCompletedUnitJobsList(units);
		}
		rootMap.put("job", job);
		rootMap.put("async", inputData.getAsync().getString());
		//		rootMap.put("limit", limit);
		res.getTemplateMap().putAll(rootMap);
	}

}
