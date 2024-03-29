package com.github.freeacs.tr069.background;

import com.github.freeacs.common.scheduler.TaskDefaultImpl;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.SyslogConstants;
import com.github.freeacs.dbi.Unit;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ScheduledKickTask extends TaskDefaultImpl {
  @Setter
  @Getter
  public static class UnitKick {
    private Unit unit;
    private final long initiatedTms;
    private int kickCount;
    private long nextTms;

    public UnitKick(Unit u) {
      this.unit = u;
      this.initiatedTms = System.currentTimeMillis();
      this.kickCount = 0;
      this.nextTms = initiatedTms + 5000;
    }

  }

  private static final Logger logger = LoggerFactory.getLogger(ScheduledKickTask.class);
  private final DBI dbi;
  private static final Object syncMonitor = new Object();

  private static final List<UnitKick> kickList = new ArrayList<>();

  public ScheduledKickTask(String taskName, DBI dbi) {
    super(taskName);
    this.dbi = dbi;
  }

  @Override
  public void runImpl() throws Throwable {
    synchronized (syncMonitor) {
      Iterator<UnitKick> listIterator = kickList.iterator();
      long now = System.currentTimeMillis();
      while (listIterator.hasNext()) {
        UnitKick uk = listIterator.next();
        if (uk.getNextTms() > now) {
          continue;
        }
        if (uk.getKickCount() > 10) {
          logger.debug(
              "Removed UnitKick from list, have kicked 10 times (Unitid: "
                  + uk.getUnit().getId()
                  + ")");
          listIterator.remove();
          continue;
        }
        Unit unit = uk.getUnit();
        ACSUnit acsUnit = dbi.getACSUnit();
        acsUnit.addOrChangeQueuedUnitParameters(unit);
        dbi.publishKick(unit, SyslogConstants.FACILITY_STUN);
        uk.setNextTms(now + 30000);
        uk.setKickCount(uk.getKickCount() + 1);
        logger.debug(
            "Initiated a kick to prompt the device to connect again (Unitid: "
                + uk.getUnit().getId()
                + ")");
      }
    }
  }

  @Override
  public Logger getLogger() {
    return logger;
  }

  // FIXME this should have been called from somewhere
  public static void addUnit(Unit u) {
    removeUnit(u.getId());
    synchronized (syncMonitor) {
      kickList.add(new UnitKick(u));
      logger.debug("Add UnitKick to list (Unitid: " + u.getId() + ")");
    }
  }

  public static void removeUnit(String unitId) {
    synchronized (syncMonitor) {
      Iterator<UnitKick> listIterator = kickList.iterator();
      while (listIterator.hasNext()) {
        UnitKick uk = listIterator.next();
        if (uk.getUnit().getId().equals(unitId)) {
          logger.debug("Removed UnitKick from list (Unitid: " + unitId + ")");
          listIterator.remove();
        }
      }
    }
  }
}
