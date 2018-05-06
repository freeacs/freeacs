package com.owera.xaps.core.task;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.owera.common.db.NoAvailableConnectionException;
import com.owera.common.log.Logger;
import com.owera.xaps.core.Properties;
import com.owera.xaps.dbi.Job;
import com.owera.xaps.dbi.JobStatus;
import com.owera.xaps.dbi.UnitJobs;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.XAPS;

public class DeleteOldJobs extends DBIOwner {

	public DeleteOldJobs(String taskName) throws SQLException, NoAvailableConnectionException {
		super(taskName);
	}

	private Logger logger = new Logger();
	private XAPS xaps;
	private Map<Integer, Job> jobMap;


	@Override
	public void runImpl() throws Exception {
		xaps = getLatestXAPS();
		removeOldJobs();
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

	private void removeOldJobs() throws Exception {
		UnitJobs unitJobs = new UnitJobs(getXapsCp());
		jobMap = new HashMap<Integer, Job>();
		Unittype[] unittypeArr = xaps.getUnittypes().getUnittypes();
		for (Unittype unittype : unittypeArr) {
			Job[] jobList = unittype.getJobs().getJobs();
			for (Job j : jobList) {
				if (j.getStatus().equals(JobStatus.COMPLETED))
					jobMap.put(j.getId(), j);
			}
		}
		List<Job> removeFromJCMap = new ArrayList<Job>();
		for (Job job : jobMap.values()) {
			if (System.currentTimeMillis() - job.getEndTimestamp().getTime() > Properties.getCompletedJobLimit() * 3600 * 1000) {
				logger.info("DeleteOldJobs: Found an old job (" + job.getName() + ")(ended at " + job.getEndTimestamp() + "), will try to delete it");
				if (job.getChildren().size() == 0) {
					unitJobs.delete(job);
					logger.info("DeleteOldJobs: \tDeleted all rows in unit_job with jobId = " + job.getId());
					job.getGroup().getUnittype().getJobs().deleteJobParameters(job, xaps);
					logger.info("DeleteOldJobs: \tDeleted all rows in job_param with jobId = " + job.getId());
					job.getGroup().getUnittype().getJobs().delete(job, xaps);
					logger.info("DeleteOldJobs: \tDeleted row in job with jobId = " + job.getId());
					removeFromJCMap.add(job);
				} else {
					logger.info("DeleteOldJobs: \tCould not delete job, since some children job still exist");
				}
			}
		}
		for (Job job : removeFromJCMap) {
			jobMap.remove(job.getId());
			logger.info("DeleteOldJobs: \tDeleted job from jobControlMap (memory-structure)");
		}

	}

}
