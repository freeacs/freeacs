package com.github.freeacs.dbi;

import com.github.freeacs.dbi.util.MapWrapper;
import com.github.freeacs.dbi.util.ProvisioningMode;
import com.github.freeacs.dbi.util.SystemParameters;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Unit {
  private String id;

  private Unittype unittype;

  private Profile profile;

  private Map<String, UnitParameter> unitParameters;
  private Map<String, UnitParameter> sessionParameters;

  private boolean paramsAvailable;

  private List<UnitParameter> writeQueue = new ArrayList<>();
  private List<UnitParameter> deleteQueue = new ArrayList<>();

  public Unit(String id, Unittype unittype, Profile profile) {
    this.id = id;
    this.unittype = unittype;
    this.profile = profile;
  }

  public Unit(String id) {
    if (id == null) {
      throw new IllegalArgumentException("Cannot make a unit if unitId is not defined");
    }
    this.id = id;
    this.unittype = null;
    this.profile = null;
  }

  public String getId() {
    return id;
  }

  public Profile getProfile() {
    return profile;
  }

  /**
   * This get-method will return the set of both profile and unit-parameters. The unit-parameters
   * will override the profile parameters.
   */
  public Map<String, String> getParameters() {
    MapWrapper<String> mw = new MapWrapper<String>(ACS.isStrictOrder());
    Map<String, String> map = mw.getMap();
    if (profile != null && profile.getProfileParameters() != null) {
      ProfileParameter[] pparams = profile.getProfileParameters().getProfileParameters();
      for (int i = 0; pparams != null && i < pparams.length; i++) {
        if (unitParameters != null
            && unitParameters.get(pparams[i].getUnittypeParameter().getName()) != null) {
          continue;
        }
        map.put(pparams[i].getUnittypeParameter().getName(), pparams[i].getValue());
      }
    }
    if (unitParameters != null) {
      for (Entry<String, UnitParameter> entry : unitParameters.entrySet()) {
        map.put(entry.getKey(), entry.getValue().getParameter().getValue());
      }
    }
    return map;
  }

  public Map<String, UnitParameter> getUnitParameters() {
    if (this.unitParameters == null) {
      MapWrapper<UnitParameter> mw = new MapWrapper<>(ACS.isStrictOrder());
      unitParameters = mw.getMap();
    }
    return unitParameters;
  }

  public void setUnitParameters(Map<String, UnitParameter> parameters) {
    this.unitParameters = parameters;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof Unit && this.id.equals(((Unit) o).getId());
  }

  @Override
  public String toString() {
    return id;
  }

  public Unittype getUnittype() {
    return unittype;
  }

  public boolean isParamsAvailable() {
    return paramsAvailable;
  }

  public void setParamsAvailable(boolean paramsAvailable) {
    this.paramsAvailable = paramsAvailable;
  }

  public String getParameterValue(String unittypeParameterName) {
    return getParameterValue(unittypeParameterName, true);
  }

  public String getParameterValue(String unittypeParameterName, boolean useProfileIfNecessary) {
    if (!paramsAvailable) {
      throw new IllegalArgumentException(
          "Cannot ask for parameter values from unit before loading parameters");
    }
    if (unitParameters != null) {
      UnitParameter up = unitParameters.get(unittypeParameterName);
      if (up != null && !up.getParameter().valueWasNull()) {
        return up.getValue();
      }
      // else
      // return null;
    }
    if (useProfileIfNecessary && profile != null) {
      ProfileParameter pp = profile.getProfileParameters().getByName(unittypeParameterName);
      if (pp != null) {
        return pp.getValue();
      }
    }
    return null;
  }

  public ProvisioningMode getProvisioningMode() {
    try {
      return ProvisioningMode.valueOf(getParameterValue(SystemParameters.PROVISIONING_MODE, false));
    } catch (Throwable t) {
      return ProvisioningMode.REGULAR;
    }
  }

  public boolean isSessionMode() {
    ProvisioningMode mode = getProvisioningMode();
    return mode == ProvisioningMode.READALL;
  }

  /**
   * Queue unit parameters here for future add/Change operation on the database - reduces number of
   * commits.
   */
  public synchronized void toWriteQueue(String unittypeParameterName, String value) {
    if (unittype == null || profile == null) {
      throw new IllegalArgumentException(
          "Cannot add to write queue in Unit because Unittype or Profile object is null");
    }
    UnittypeParameter utp = unittype.getUnittypeParameters().getByName(unittypeParameterName);
    toWriteQueue(new UnitParameter(new Parameter(utp, value), id, profile));
  }

  public synchronized void toWriteQueue(UnitParameter up) {
    Iterator<UnitParameter> queueIterator = writeQueue.iterator();
    if (queueIterator.hasNext()) {
      UnitParameter tmp = queueIterator.next();
      if (tmp.getParameter()
          .getUnittypeParameter()
          .getId()
          .equals(up.getParameter().getUnittypeParameter().getId())) {
        queueIterator.remove();
      }
    }
    writeQueue.add(up);
  }

  /** Upon read, the queue will be deleted and a new one created. */
  public synchronized List<UnitParameter> flushWriteQueue() {
    List<UnitParameter> tmp = writeQueue;
    writeQueue = new ArrayList<>();
    return tmp;
  }

  /**
   * Queue unit parameters here for future delete operation on the database - reduces number of
   * commits.
   */
  public synchronized void toDeleteQueue(String unittypeParameterName) {
    if (unittype == null || profile == null) {
      throw new IllegalArgumentException(
          "Cannot add to delete queue in Unit because Unittype or Profile object is null");
    }
    UnittypeParameter utp = unittype.getUnittypeParameters().getByName(unittypeParameterName);
    toDeleteQueue(new UnitParameter(new Parameter(utp, null), id, profile));
  }

  public synchronized void toDeleteQueue(UnitParameter up) {
    Iterator<UnitParameter> queueIterator = deleteQueue.iterator();
    if (queueIterator.hasNext()) {
      UnitParameter tmp = queueIterator.next();
      if (tmp.getParameter()
          .getUnittypeParameter()
          .getId()
          .equals(up.getParameter().getUnittypeParameter().getId())) {
        queueIterator.remove();
      }
    }
    deleteQueue.add(up);
  }

  /** Upon read, the *queue* will be deleted and a new one created. */
  public synchronized List<UnitParameter> flushDeleteQueue() {
    List<UnitParameter> tmp = deleteQueue;
    deleteQueue = new ArrayList<>();
    return tmp;
  }

  public Map<String, UnitParameter> getSessionParameters() {
    if (this.sessionParameters == null) {
      MapWrapper<UnitParameter> mw = new MapWrapper<UnitParameter>(ACS.isStrictOrder());
      sessionParameters = mw.getMap();
    }
    return sessionParameters;
  }
}
