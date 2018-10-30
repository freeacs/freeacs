package com.github.freeacs.tr069.background;

import com.github.freeacs.common.quartz.QuartzJob;
import org.quartz.DisallowConcurrentExecution;

@DisallowConcurrentExecution
public class ScheduledKickJob extends QuartzJob {}
