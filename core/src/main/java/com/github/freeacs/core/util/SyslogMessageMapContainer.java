package com.github.freeacs.core.util;

import com.github.freeacs.common.util.TimestampMap;
import com.github.freeacs.dbi.Heartbeat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SyslogMessageMapContainer {
  public class SyslogMessageMap {
    private Heartbeat heartbeat;
    private TimestampMap unitIdTmsMap;

    public SyslogMessageMap(Heartbeat heartbeat) {
      this.heartbeat = heartbeat;
      unitIdTmsMap = new TimestampMap();
    }

    public TimestampMap getUnitIdTmsMap() {
      return unitIdTmsMap;
    }

    public void append(String unitId, Long tms) {
      unitIdTmsMap.put(unitId, tms);
    }

    public String toString() {
      String oldest = "N/A";
      String newest = "N/A";
      if (unitIdTmsMap.oldest() != null) {
        oldest = sdf.format(new Date(unitIdTmsMap.get(unitIdTmsMap.oldest())));
      }
      if (unitIdTmsMap.newest() != null) {
        newest = sdf.format(new Date(unitIdTmsMap.get(unitIdTmsMap.newest())));
      }
      return "Heartbeat:"
          + heartbeat
          + ", Period:"
          + oldest
          + "-"
          + newest
          + ", Size:"
          + unitIdTmsMap.size();
    }

    public Heartbeat getHeartbeat() {
      return heartbeat;
    }

    public void setHeartbeat(Heartbeat heartbeat) {
      this.heartbeat = heartbeat;
    }
  }

  private static SimpleDateFormat sdf = new SimpleDateFormat("MMM dd HH:mm:ss");

  private Map<Integer, SyslogMessageMap> container = new HashMap<>();

  public SyslogMessageMap getSyslogMessageMap(Integer heartbeatId) {
    return container.get(heartbeatId);
  }

  public SyslogMessageMap createSyslogMessageMap(Heartbeat heartbeat) {
    SyslogMessageMap smm = new SyslogMessageMap(heartbeat);
    container.put(heartbeat.getId(), smm);
    return smm;
  }

  public Collection<SyslogMessageMap> getContainerValues() {
    return container.values();
  }

  public Iterator<Integer> getIterator() {
    return container.keySet().iterator();
  }
}
