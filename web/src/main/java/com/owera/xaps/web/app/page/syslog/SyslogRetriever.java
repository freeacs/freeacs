package com.owera.xaps.web.app.page.syslog;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.owera.common.db.NoAvailableConnectionException;
import com.owera.xaps.dbi.Profile;
import com.owera.xaps.dbi.Syslog;
import com.owera.xaps.dbi.SyslogEntry;
import com.owera.xaps.dbi.SyslogFilter;
import com.owera.xaps.dbi.Unit;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.web.app.page.AbstractWebPage;
import com.owera.xaps.web.app.util.SessionCache;
import com.owera.xaps.web.app.util.XAPSLoader;

public class SyslogRetriever {
	private SyslogData inputData;

	private SyslogRetriever() {
		inputData = new SyslogData();
	}

	private SyslogRetriever(SyslogData inputData) {
		this.inputData = inputData;
	}

	public static SyslogRetriever getInstance() {
		return new SyslogRetriever();
	}

	public static SyslogRetriever getInstance(SyslogData inputData) {
		return new SyslogRetriever(inputData);
	}

	public List<SyslogEntry> getSyslogEntries(Unit unit, Date fromDate, Date endDate, Integer max, String sessionId) throws NoAvailableConnectionException, SQLException, ParseException,
			IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		inputData.getTimestampStart().setValue(fromDate);
		inputData.getTimestampEnd().setValue(endDate);
		inputData.getUnit().setValue("^" + unit.getId() + "$");
		List<SyslogEntry> entries = getSyslogEntries(max, unit.getUnittype(), unit.getProfile(), sessionId);
		return entries;
	}

	public List<SyslogEntry> getSyslogEntries(Unit unit, Date fromDate, Date endDate, Integer max, String messageFilter, String sessionId) throws NoAvailableConnectionException, SQLException,
			ParseException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		inputData.getMessage().setValue(messageFilter);
		return getSyslogEntries(unit, fromDate, endDate, max, sessionId);
	}

	public List<SyslogEntry> getSyslogEntries(Integer maxrows, Unittype unittype, Profile profile, String sessionId) throws NoAvailableConnectionException, SQLException, ParseException,
			IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Syslog syslog = new Syslog(SessionCache.getSyslogConnectionProperties(sessionId), XAPSLoader.getIdentity(sessionId));
		SyslogFilter filter = new SyslogFilter();
		if (maxrows != null)
			filter.setMaxRows(maxrows + 1);
		filter.setMessage(inputData.getMessage().getString());
		filter.setFacilityVersion(inputData.getFacilityVersion().getString());
		if (unittype != null)
			filter.setUnittypes(Arrays.asList(new Unittype[] { unittype }));
		else if (AbstractWebPage.isUnittypesLimited(sessionId)) {
			List<Unittype> uts = AbstractWebPage.getAllowedUnittypes(sessionId);
			if (uts.size() == 0)
				return new ArrayList<SyslogEntry>();
			filter.setUnittypes(uts);
		}
		if (profile != null)
			filter.setProfiles(Arrays.asList(new Profile[] { profile }));
		else {
			List<Profile> profiles = AbstractWebPage.getAllowedProfiles(sessionId, unittype);
			if (profiles != null && profiles.size() > 0) {
				filter.setProfiles(profiles);
			}
		}
		if (inputData.getSeverity().getStringArray() != null)
			filter.setSeverity((Integer[]) SyslogUtil.translateSeverityLevel(inputData.getSeverity().getStringArray()));
		if (inputData.getEvent().getInteger() != null)
			filter.setEventId(inputData.getEvent().getInteger());
		if (inputData.getIpaddress().getString() != null && inputData.getIpaddress().getString().length() > 0)
			filter.setIpAddress(inputData.getIpaddress().getString());
		if (inputData.getUserId().getString() != null && inputData.getUserId().getString().length() > 0)
			filter.setUserId(inputData.getUserId().getString());
		if (inputData.getFacility().getString() != null && !inputData.getFacility().getString().equals("All"))
			filter.setFacility(Integer.parseInt(inputData.getFacility().getString()));
		filter.setCollectorTmsStart(inputData.getTimestampStart().getDateOrDefault(SyslogUtil.getDate()));
		filter.setCollectorTmsEnd(inputData.getTimestampEnd().getDateOrDefault(new Date()));
		if (filter.getCollectorTmsStart().getTime() == filter.getCollectorTmsEnd().getTime()) {
			// Set to the same minute, increase end-tms to be the 0 millisecond of next minute:
			Calendar c = Calendar.getInstance();
			c.setTime(filter.getCollectorTmsEnd());
			c.add(Calendar.MINUTE, 1);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			filter.setCollectorTmsEnd(c.getTime());
		}
		filter.setUnitId(inputData.getUnit().getString());
		List<SyslogEntry> entries = syslog.read(filter, XAPSLoader.getXAPS(sessionId));
		SessionCache.putSyslogEntries(sessionId, entries);
		return entries;
	}
}