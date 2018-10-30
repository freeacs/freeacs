package com.github.freeacs.stun;

import com.github.freeacs.common.quartz.QuartzJob;
import org.quartz.DisallowConcurrentExecution;

@DisallowConcurrentExecution
public class ActiveDeviceDetectionJob extends QuartzJob {}
