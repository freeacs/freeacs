package com.owera.common.util;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * This class will represent "time windows". Examples of time windows
 * 
 * mo-fr:0800-1600
 * mo-su:1600-2400
 * mo:1600-1601
 * fr-mo:0800-1600
 * fr-mo:2300-0500
 * 
 * The class offers 3 methods to deal with this information:
 * 
 * - getNextTimeWindow (when (in ms) will next time window occur)
 * - getTimeWindowLength (in ms)
 * - isWithinTimeWindow(long ms) returns true or false
 * 
 * This should be enough to determine if something is within or without a time
 * window, as well as schedule something to happen within the next time window.
 * 
 * The class will be used by the TR-069 Server to schedule provisioning.
 * @author Morten
 *
 */
public class TimeWindow {

	private int weekdayStart;

	private int weekdayEnd;

	private int weekdaySpan;

	private int timeStart;

	private int timeEnd;

	private int timeSpan;

	private String orgStr;

	private static Map<String, Integer> weekDayMap = new HashMap<String, Integer>();
	static {
		weekDayMap.put("su", 1);
		weekDayMap.put("mo", 2);
		weekDayMap.put("tu", 3);
		weekDayMap.put("we", 4);
		weekDayMap.put("th", 5);
		weekDayMap.put("fr", 6);
		weekDayMap.put("sa", 7);
	}

	public String toString() {
		return orgStr;
	}

	public TimeWindow(String s) {
		if (s == null)
			s = "mo-su:0000-2400";

		String regexp = "(mo|tu|we|th|fr|sa|su)(-(mo|tu|we|th|fr|sa|su))?:\\d{4}-\\d{4}";
		boolean match = s.matches(regexp);
		if (!match)
			throw new IllegalArgumentException("The TimeWindow argument does not match this regexp:" + regexp);

		weekdayStart = weekDayMap.get(s.substring(0, 2));
		int offset = 0;
		if (s.indexOf("-") == 2) {
			weekdayEnd = weekDayMap.get(s.substring(3, 5));
			offset = 3;
		} else
			weekdayEnd = weekdayStart;
		timeStart = Integer.parseInt(s.substring(3 + offset, 7 + offset));
		timeEnd = Integer.parseInt(s.substring(8 + offset, 12 + offset));
		if (timeStart == timeEnd) {
			timeEnd += 2400;
			//			throw new IllegalArgumentException("It is not allowed with equal hour-minute arguments (causes a window to be 0 sec).");
		}

		weekdaySpan = calculateWeekdaySpan(weekdayStart, weekdayEnd);
		timeSpan = calculateTimeSpan(timeStart, timeEnd);

		orgStr = s;

	}

	public static void main(String[] args) {
		printInfo(new TimeWindow(null));
		printInfo(new TimeWindow("tu-tu:2300-2300"));
		printInfo(new TimeWindow("mo-mo:0700-0100"));
	}

	private static void printInfo(TimeWindow tw) {
		long dailyLength = tw.getDailyLength();
		long nextStartTms = tw.getNextStartTms(System.currentTimeMillis());
		long prevStartTms = tw.getPreviousStartTms(System.currentTimeMillis());
		System.out.println("[" + tw + "] Is inside tw:         " + tw.isWithinTimeWindow(System.currentTimeMillis()));
		System.out.println("[" + tw + "] Next startTms:        " + String.format("%1$tF %1$tR", nextStartTms));
		System.out.println("[" + tw + "] Next endTms  :        " + String.format("%1$tF %1$tR", nextStartTms+dailyLength));		
		System.out.println("[" + tw + "] Previous startTms:    " + String.format("%1$tF %1$tR", prevStartTms));
		System.out.println("[" + tw + "] Previous endTms  :    " + String.format("%1$tF %1$tR", prevStartTms+dailyLength));
		System.out.println("[" + tw + "] Length of tw (daily): " + dailyLength / 1000);
		System.out.println("[" + tw + "] Length of tw (weekly):" + tw.getWeeklyLength() / 1000 + "\n");
	}

	public long getDailyLength() {
		int hour = timeSpan / 100;
		int minute = timeSpan % 100;
		return hour * 3600 * 1000 + minute * 60 * 1000;
	}

	public long getWeeklyLength() {
		return weekdaySpan * getDailyLength();
	}

	public long getPreviousStartTms(long tms) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(tms);
		int minuteStart = timeStart % 100;
		int hourStart = (timeStart - minuteStart) / 100;
		c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH));
		c.set(Calendar.HOUR_OF_DAY, hourStart);
		c.set(Calendar.MINUTE, minuteStart);
		c.set(Calendar.SECOND, 0);

		while (true) {
			long checkTms = c.getTimeInMillis();
			//			System.out.println(String.format("-- Checking %1$tF %1$tR", checkTms));
			if (isWithinTimeWindow(checkTms) && c.getTimeInMillis() <= tms)
				return checkTms;
			c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) - 1);
		}
	}

	public long getNextStartTms(long tms) {
		Calendar c = Calendar.getInstance();
		int minuteStart = timeStart % 100;
		int hourStart = (timeStart - minuteStart) / 100;
		c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH));
		c.set(Calendar.HOUR_OF_DAY, hourStart);
		c.set(Calendar.MINUTE, minuteStart);
		c.set(Calendar.SECOND, 0);

		while (true) {
			long checkTms = c.getTimeInMillis();
			//			System.out.println(String.format("-- Checking %1$tF %1$tR", checkTms));
			if (isWithinTimeWindow(checkTms) && c.getTimeInMillis() > tms)
				return checkTms;
			c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 1);
		}
	}

	private int calculateWeekdaySpan(int day1, int day2) {
		int suggestedDiff = day2 - day1;
		if (suggestedDiff >= 0)
			return suggestedDiff + 1;
		else
			return day2 + 7 - day1 + 1;
	}

	private int calculateTimeSpan(int time1, int time2) {
		int suggestedDiff = time2 - time1;
		if (suggestedDiff >= 0)
			return suggestedDiff;
		else
			return time2 + 2400 - time1;
	}

	public boolean isWithinTimeWindow(long tms) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(tms);
		int currentDay = c.get(Calendar.DAY_OF_WEEK);
		int currentHour = c.get(Calendar.HOUR_OF_DAY);
		int currentMinute = c.get(Calendar.MINUTE);
		int currentTime = 100 * currentHour + currentMinute;

		int currentDayToWeekdayEndSpan = calculateWeekdaySpan(currentDay, weekdayEnd);
		boolean withinWeekday = false;
		if (currentDayToWeekdayEndSpan <= weekdaySpan)
			withinWeekday = true;

		if (withinWeekday) {
			if (calculateTimeSpan(currentTime, timeEnd) <= timeSpan)
				return true;
		}
		return false;
	}

}
