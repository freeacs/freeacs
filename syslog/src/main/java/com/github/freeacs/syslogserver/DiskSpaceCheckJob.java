package com.github.freeacs.syslogserver;

import com.github.freeacs.common.quartz.QuartzJob;
import org.quartz.DisallowConcurrentExecution;

@DisallowConcurrentExecution
public class DiskSpaceCheckJob extends QuartzJob {}
