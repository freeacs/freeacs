package com.github.freeacs.common.quartz;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

public class QuartzJob implements Job {

  public interface Job {
    void apply();
  }

  @Override
  public void execute(JobExecutionContext jobContext) {
    JobDetail jobDetail = jobContext.getJobDetail();
    Job job = (Job) jobDetail.getJobDataMap().get("job");
    job.apply();
  }
}
