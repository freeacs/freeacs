package com.github.freeacs.common.quartz;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

import java.util.function.Supplier;

public class QuartzJob implements Job {
    @Override
    public void execute(JobExecutionContext jobContext) {
        JobDetail jobDetail = jobContext.getJobDetail();
        Supplier<Void> job = (Supplier<Void>) jobDetail.getJobDataMap().get("job");
        job.get();
    }
}
