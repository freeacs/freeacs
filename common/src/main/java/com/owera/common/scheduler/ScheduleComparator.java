package com.owera.common.scheduler;

import java.util.Comparator;

public class ScheduleComparator implements Comparator<Schedule> {

	@Override
	public int compare(Schedule arg0, Schedule arg1) {
		if (arg0 == null)
			return -1;
		if (arg1 == null)
			return 1;
		if (arg0.getNextLaunch() < arg1.getNextLaunch())
			return -1;
		else
			return 1;
	}

}
