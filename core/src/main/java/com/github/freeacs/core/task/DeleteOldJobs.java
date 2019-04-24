package com.github.freeacs.core.task;

import com.github.freeacs.core.Properties;
import com.github.freeacs.dbi.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteOldJobs extends DBIOwner {
  private final Properties properties;

  public DeleteOldJobs(String taskName, DBI dbi, Properties properties) {
    super(taskName, dbi);
    this.properties = properties;
  }

  private static Logger logger = LoggerFactory.getLogger(DeleteOldJobs.class);

  private ACS acs;

  @Override
  public void runImpl() throws Exception {
    acs = getLatestACS();
    removeOldJobs();
  }

  @Override
  public Logger getLogger() {
    return logger;
  }

  private void removeOldJobs() throws Exception {
    UnitJobs unitJobs = new UnitJobs(getDataSource());
    Map<Integer, Job> jobMap = new HashMap<>();
    Unittype[] unittypeArr = acs.getUnittypes().getUnittypes();
    for (Unittype unittype : unittypeArr) {
      Job[] jobList = unittype.getJobs().getJobs();
      for (Job j : jobList) {
        if (JobStatus.COMPLETED.equals(j.getStatus())) {
          jobMap.put(j.getId(), j);
        }
      }
    }
    List<Job> removeFromJCMap = new ArrayList<>();
    for (Job job : jobMap.values()) {
      if (System.currentTimeMillis() - job.getEndTimestamp().getTime()
          > properties.getCompletedJobLimit() * 3600 * 1000) {
        if (logger.isInfoEnabled()) {
          logger.info(
              "DeleteOldJobs: Found an old job ("
                  + job.getName()
                  + ")(ended at "
                  + job.getEndTimestamp()
                  + "), will try to delete it");
        }
        if (job.getChildren().isEmpty()) {
          unitJobs.delete(job);
          if (logger.isInfoEnabled()) {
            logger.info(
                "DeleteOldJobs: \tDeleted all rows in unit_job with jobId = " + job.getId());
          }
          job.getGroup().getUnittype().getJobs().deleteJobParameters(job, acs);
          if (logger.isInfoEnabled()) {
            logger.info(
                "DeleteOldJobs: \tDeleted all rows in job_param with jobId = " + job.getId());
          }
          job.getGroup().getUnittype().getJobs().delete(job, acs);
          if (logger.isInfoEnabled()) {
            logger.info("DeleteOldJobs: \tDeleted row in job with jobId = " + job.getId());
          }
          removeFromJCMap.add(job);
        } else if (logger.isInfoEnabled()) {
          logger.info("DeleteOldJobs: \tCould not delete job, since some children job still exist");
        }
      }
    }
    for (Job job : removeFromJCMap) {
      jobMap.remove(job.getId());
      if (logger.isInfoEnabled()) {
        logger.info("DeleteOldJobs: \tDeleted job from jobControlMap (memory-structure)");
      }
    }
  }
}
