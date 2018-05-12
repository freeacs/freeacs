package com.github.freeacs.common.scheduler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class ScheduleList {

	// This list orders the Schedule objects so that first in the list 
	// are first to run.
	private LinkedList<Schedule> list = new LinkedList<Schedule>();

	public synchronized void add(Schedule newSchedule) {
		newSchedule.setAddedToQueueTms(System.currentTimeMillis());
		for (int i = 0; i < list.size(); i++) {
			Schedule oldSchedule = list.get(i);
			if (newSchedule.getNextLaunch() < oldSchedule.getNextLaunch()) {
				// Place newSchedule in front of oldSchedule
				list.add(i, newSchedule);
				return;
			}
		}
		list.add(newSchedule);
	}

	public synchronized Schedule peek() {
		return list.peek();
	}

	public synchronized Schedule pop() {
		try {
			return list.pop();
		} catch (NoSuchElementException nse) {
			return null;
		}
	}

	public synchronized void remove(Schedule schedule) {
		list.remove(schedule);
	}

	public synchronized List<Schedule> getSchedules() {
		List<Schedule> tmp = new ArrayList<Schedule>();
		for (int i = 0; i < list.size(); i++)
			tmp.add(list.get(i));
		return tmp;
	}
}
