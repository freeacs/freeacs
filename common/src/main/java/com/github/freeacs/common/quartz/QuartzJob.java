package com.github.freeacs.common.quartz;

import java.util.function.Supplier;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

public class QuartzJob implements Job {
  @Override
  public void execute(JobExecutionContext jobContext) {
    JobDetail jobDetail = jobContext.getJobDetail();
    Supplier<Void> job = (Supplier<Void>) jobDetail.getJobDataMap().get("job");
    job.get();
  }
}
