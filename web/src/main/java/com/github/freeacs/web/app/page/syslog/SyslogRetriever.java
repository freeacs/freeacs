package com.github.freeacs.web.app.page.syslog;

import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Syslog;
import com.github.freeacs.dbi.SyslogEntry;
import com.github.freeacs.dbi.SyslogFilter;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.util.ACSLoader;
import com.github.freeacs.web.app.util.SessionCache;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.sql.DataSource;

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

  public List<SyslogEntry> getSyslogEntries(
      Unit unit,
      Date fromDate,
      Date endDate,
      Integer max,
      String sessionId,
      DataSource xapsDataSource,
      DataSource syslogDataSource)
      throws SQLException {
    inputData.getTimestampStart().setValue(fromDate);
    inputData.getTimestampEnd().setValue(endDate);
    inputData.getUnit().setValue("^" + unit.getId() + "$");
    return getSyslogEntries(
        max, unit.getUnittype(), unit.getProfile(), sessionId, xapsDataSource, syslogDataSource);
  }

  public List<SyslogEntry> getSyslogEntries(
      Integer maxrows,
      Unittype unittype,
      Profile profile,
      String sessionId,
      DataSource xapsDataSource,
      DataSource syslogDataSource)
      throws SQLException {
    Syslog syslog = new Syslog(syslogDataSource, ACSLoader.getIdentity(sessionId, xapsDataSource));
    SyslogFilter filter = new SyslogFilter();
    if (maxrows != null) {
      filter.setMaxRows(maxrows + 1);
    }
    filter.setMessage(inputData.getMessage().getString());
    filter.setFacilityVersion(inputData.getFacilityVersion().getString());
    if (unittype != null) {
      filter.setUnittypes(Collections.singletonList(unittype));
    } else if (AbstractWebPage.isUnittypesLimited(sessionId, xapsDataSource, syslogDataSource)) {
      List<Unittype> uts =
          AbstractWebPage.getAllowedUnittypes(sessionId, xapsDataSource, syslogDataSource);
      if (uts.isEmpty()) {
        return new ArrayList<>();
      }
      filter.setUnittypes(uts);
    }
    if (profile != null) {
      filter.setProfiles(Collections.singletonList(profile));
    } else {
      List<Profile> profiles =
          AbstractWebPage.getAllowedProfiles(sessionId, unittype, xapsDataSource, syslogDataSource);
      if (profiles != null && !profiles.isEmpty()) {
        filter.setProfiles(profiles);
      }
    }
    if (inputData.getSeverity().getStringArray() != null) {
      filter.setSeverity(
          (Integer[]) SyslogUtil.translateSeverityLevel(inputData.getSeverity().getStringArray()));
    }
    if (inputData.getEvent().getInteger() != null) {
      filter.setEventId(inputData.getEvent().getInteger());
    }
    if (inputData.getIpaddress().getString() != null
        && !inputData.getIpaddress().getString().isEmpty()) {
      filter.setIpAddress(inputData.getIpaddress().getString());
    }
    if (inputData.getUserId().getString() != null && !inputData.getUserId().getString().isEmpty()) {
      filter.setUserId(inputData.getUserId().getString());
    }
    if (inputData.getFacility().getString() != null
        && !"All".equals(inputData.getFacility().getString())) {
      filter.setFacility(Integer.parseInt(inputData.getFacility().getString()));
    }
    filter.setCollectorTmsStart(
        inputData.getTimestampStart().getDateOrDefault(SyslogUtil.getDate()));
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
    return syslog.read(filter, ACSLoader.getXAPS(sessionId, xapsDataSource, syslogDataSource));
  }
}
