package com.github.freeacs.stun;

import com.github.freeacs.common.util.Sleep;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.Inbox;
import com.github.freeacs.dbi.Message;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.Unittype.ProvisioningProtocol;
import com.github.freeacs.dbi.util.ProvisioningMode;
import com.github.freeacs.dbi.util.SystemConstants;
import com.github.freeacs.dbi.util.SystemParameters;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SingleKickThread is responsible for initiating a kick (connect to ConnectionRequestURL or the
 * UDPConnectinRequestAddress) for one or more devices.
 *
 * <p>This class is implemented as a Thread which always runs and listen for requests issued by
 * North-Bound Interfaces (Web, Shell or WS). If such a request (for a kick) is detected it will
 * check the state of the unit, to see if there's some parameters which can be used to either
 * connect using ConnectionRequestURL (TCP/HTTP connect) or UDPConnectionRequestURL (UDP connect).
 * In the latter case, it is possible to traverse NATs to reach the device, and it's the primary
 * reason for why this logic is placed in the STUN-server. The support for this logic requires the
 * STUN-server to issue the UDP-packet. By doing so, Fusion supports the requirement to TR-111 part
 * 2.
 */
public class SingleKickThread implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(SingleKickThread.class);

  private final DBI dbi;
  private final DataSource xapsCp;
  private final Properties properties;

  private ACSUnit acsUnit;

  private Map<String, InspectionState> unitWatch = new HashMap<>();
  private Inbox inbox = new Inbox();
  private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

  public SingleKickThread(DataSource xapsCp, DBI dbi, Properties properties) {
    this.xapsCp = xapsCp;
    this.dbi = dbi;
    this.properties = properties;
  }

  /* METHODS FOR DETECTING UNITS WHICH SHOULD BE KICKED */
  /**
   * The standard way of detecting change, a message is sent from Shell, Web or WS directly to this
   * module.
   *
   * @throws SQLException
   */
  private void updateUnitWatchBasedOnMessages() throws SQLException {
    for (Message m : inbox.getUnreadMessages()) {
      String unitId = m.getObjectId();
      if (unitWatch.get(unitId) == null) {
        Unit unit = acsUnit.getUnitById(unitId);
        if (unit == null) {
          LOG.debug(unitId + " was not found in the database, no kick performed");
          continue;
        }
        if (unit.getUnittype().getProtocol() != ProvisioningProtocol.TR069) {
          LOG.debug(
              unitId
                  + " is not a TR069 unit, leaving it alone for another protocol server to handle");
          continue;
        }
        InspectionState is = new InspectionState();
        is.setTmsOfLastChange(System.currentTimeMillis());
        LOG.debug(unitId + " is added to the watch list (from messages), will now be processed.");
        unitWatch.put(unitId, is);
      } else {
        InspectionState is = unitWatch.get(unitId);
        is.setTmsOfLastChange(System.currentTimeMillis());
        is.setKicked(false);
      }
      inbox.markMessageAsRead(m);
    }
    inbox.deleteReadMessage();
  }

  /**
   * This methods is run once every 5 minute. Will check if there are units which have stored data
   * in the unit_param_session table. If so, and they're not already on the unitWatch-list, they're
   * units which are "stranded" and it must be cleaned up (after a 15m wait).
   *
   * @throws SQLException
   */
  private void updateUnitWatchBasedOnSessionParams() throws SQLException {
    List<String> unitIds = acsUnit.getUnitIdsFromSessionUnitParameters();
    for (String unitId : unitIds) {
      if (unitWatch.get(unitId) == null) {
        InspectionState is = new InspectionState();
        is.setTmsOfLastChange(System.currentTimeMillis());
        is.setKicked(true); // do not kick it again
        LOG.debug(
            unitId + " is added to the watch list (from session params), will now be processed.");
        unitWatch.put(unitId, is);
      }
    }
  }

  /**
   * This methods is run once every 5 minute. Will check if there are units which have provisioning
   * mode set to INSPECTION or EXTRACTION. If so, and they're not already on the unitWatch-list,
   * they're units which are "stranded" and it must be cleaned up (after a 15m wait).
   *
   * @throws SQLException
   */
  private void updateUnitWatchBasedOnProvisioningMode() throws SQLException {
    Map<String, Unit> units = acsUnit.getUnits(ProvisioningMode.READALL.toString(), null, null);
    for (String unitId : units.keySet()) {
      if (unitWatch.get(unitId) == null) {
        Unit unit = acsUnit.getUnitById(unitId);
        if (unit.getProvisioningMode() != ProvisioningMode.REGULAR) {
          InspectionState is = new InspectionState();
          is.setTmsOfLastChange(System.currentTimeMillis());
          is.setKicked(true); // do not kick it again
          LOG.debug(
              unitId + " is added to the watch list (from prov-mode), will now be processed.");
          unitWatch.put(unitId, is);
        }
      }
    }
  }

  /* KICK RELATED METHODS */

  /** CLEAN UP. */
  private void reset(
      Iterator<String> watchListIterator,
      Unit unit /* , UnitParameter stateUp, UnitParameter modeUp */)
      throws SQLException {
    watchListIterator.remove();
    unit.toWriteQueue(SystemParameters.PROVISIONING_MODE, ProvisioningMode.REGULAR.toString());
    if (SystemConstants.DEFAULT_INSPECTION_MESSAGE.equals(
        unit.getParameterValue(SystemParameters.INSPECTION_MESSAGE))) {
      unit.toWriteQueue(
          SystemParameters.INSPECTION_MESSAGE, SystemConstants.DEFAULT_INSPECTION_MESSAGE);
    }
    acsUnit.addOrChangeQueuedUnitParameters(unit);
    acsUnit.deleteAllSessionParameters(unit);
  }

  /** MAIN LOOP. */
  private void processUnitWatchList() throws SQLException {
    Iterator<String> iterator = unitWatch.keySet().iterator();
    while (iterator.hasNext()) {
      String unitId = null;
      Unit unit = null;
      try {
        unitId = iterator.next();
        InspectionState lastIS = unitWatch.get(unitId);
        acsUnit = new ACSUnit(xapsCp, dbi.getAcs(), dbi.getSyslog()); // make sure xAPS object is updated
        unit = acsUnit.getUnitById(unitId);
        long timeSinceLastChange = System.currentTimeMillis() - lastIS.getTmsOfLastChange();
        if (timeSinceLastChange > 15 * 60 * 1000) {
          LOG.debug(
              unitId
                  + " is reset (inspection-params deleted, mode/state reset, removed from watchList) because it's been 15 min. since provisioning mode change.");
          reset(iterator, unit);
        } else if (unit.getProvisioningMode() == ProvisioningMode.REGULAR
            && timeSinceLastChange > 60 * 1000) {
          LOG.debug(
              unitId
                  + " is reset (inspection-params deleted, mode/state reset, removed from watchList) because the mode has been "
                  + ProvisioningMode.REGULAR
                  + " for 60 sec.");
          reset(iterator, unit);
        } else if (!lastIS.isKicked()) {
          Kick.KickResponse kr = Kick.kick(unit, properties);
          if (kr.isKicked()) {
            acsUnit.addOrChangeUnitParameter(
                unit,
                SystemParameters.INSPECTION_MESSAGE,
                "Kick success at "
                    + sdf.format(new Date())
                    + " - *MAY* expect provisioning response :: "
                    + kr.getMessage());
          } else {
            acsUnit.addOrChangeUnitParameter(
                unit,
                SystemParameters.INSPECTION_MESSAGE,
                "Kick failed at "
                    + sdf.format(new Date())
                    + " - require reboot to initiate provisioning :: "
                    + kr.getMessage());
          }
          LOG.debug(
              unit.getId()
                  + " was kicked (response = "
                  + kr.isKicked()
                  + ", message =  "
                  + kr.getMessage()
                  + ").");
          lastIS.setKicked(true);
        }
      } catch (Throwable t) {
        if (unit != null) {
          reset(iterator, unit);
        }
        LOG.error(
            "An error occurred in processUnitWatchList() with unit "
                + unitId
                + ", continues with next unit (if any)",
            t);
      }
    }
  }

  /** MAIN METHOD. */
  public void run() {
    try {
      inbox.addFilter(new Message(null, Message.MTYPE_PUB_IM, null, Message.OTYPE_UNIT));
      dbi.registerInbox("KickRunnable", inbox);
      this.acsUnit = new ACSUnit(xapsCp, dbi.getAcs(), dbi.getSyslog());
      Sleep sleep = new Sleep(1000, 1000, true);
      long lastUpdateCheck = 0;
      do {
        try {
          sleep.sleep();
          if (Sleep.isTerminated()) {
            break;
          }
          updateUnitWatchBasedOnMessages();
          if (System.currentTimeMillis() - lastUpdateCheck > 5 * 60000) {
            updateUnitWatchBasedOnProvisioningMode();
            updateUnitWatchBasedOnSessionParams();
            lastUpdateCheck = System.currentTimeMillis();
          }
          processUnitWatchList();
        } catch (Throwable tInner) {
          if (LOG != null) {
            LOG.error("An error ocurred, SingleKickThread.run() continues", tInner);
          }
        }
      } while (true);
    } catch (Throwable tOuter) {
      LOG.error(
          "An error ocurred, SingleKickThread.run() exits - server is not able to process Extraction/Inspection/Kick Mode anymore!!!",
          tOuter);
    }
  }

  public class InspectionState {
    private long tmsOfLastChange;
    private boolean kicked;

    public long getTmsOfLastChange() {
      return tmsOfLastChange;
    }

    public void setTmsOfLastChange(long tmsOfLastChange) {
      this.tmsOfLastChange = tmsOfLastChange;
    }

    public boolean isKicked() {
      return kicked;
    }

    public void setKicked(boolean kicked) {
      this.kicked = kicked;
    }
  }
}
