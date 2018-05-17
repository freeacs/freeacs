package com.github.freeacs.core.task;

import com.github.freeacs.core.Properties;
import com.github.freeacs.dbi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeleteOldJobs extends DBIOwner {

	public DeleteOldJobs(String taskName, DataSource xapsCp, DataSource sysCp) throws SQLException {
		super(taskName, xapsCp, sysCp);
	}

	private static Logger logger = LoggerFactory.getLogger(DeleteOldJobs.class);

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
