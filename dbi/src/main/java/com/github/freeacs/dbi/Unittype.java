package com.github.freeacs.dbi;

import com.github.freeacs.common.util.NumberComparator;
import com.github.freeacs.dbi.util.MapWrapper;
import com.github.freeacs.dbi.util.SystemParameters;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Unittype implements Comparable<Unittype> {
  public enum ProvisioningProtocol {
    TR069,
    HTTP,
    OPP,
    NA,
    TFTP;

    public static ProvisioningProtocol toEnum(String s) {
      if (s == null || "TR-069".equals(s)) {
        return TR069;
      }
      if ("N/A".equals(s)) {
        return NA;
      }
      return valueOf(s);
    }
  }

  private Integer id;

  private String name;

  private String oldName;

  private String vendor;

  private String description;

  private ProvisioningProtocol protocol;

  private UnittypeParameters unittypeParameters;

  private Profiles profiles;

  private Files files;

  private Groups groups;

  private Triggers triggers;

  private Jobs jobs;

  private SyslogEvents syslogEvents;

  private Heartbeats heartbeats;

  private ACS acs;

  public Unittype(String name, String vendor, String desc, ProvisioningProtocol protocol) {
    if (name == null || "".equals(name.trim())) {
      throw new IllegalArgumentException("Unittype name cannot be null or an empty string");
    }
    this.name = name;
    this.vendor = vendor;
    this.description = desc;
    setProtocol(protocol);
  }

  /**
   * Only to be used internally (to shape ACS object according to permissions).
   *
   * @param p
   * @return
   */
  protected void removeObjects(Profile p) {
    files = null;
    // Remove all groups which do not use the profile p

    for (Group group : getGroups().getGroups()) {
      if (group.getTopParent().getProfile() == null
          || !group.getTopParent().getProfile().getId().equals(p.getId())) {
        groups.removeGroupFromDataModel(group);
      }
    }

    for (Job job : getJobs().getJobs()) {
      if (groups.getById(job.getGroup().getId()) == null) {
        jobs.removeJobFromDataModel(job);
      }
    }

    syslogEvents = null;
    triggers = null;
    heartbeats = null;
  }

  public void setAcs(ACS acs) {
    this.acs = acs;
  }

  public ACS getAcs() {
    return acs;
  }

  public String getDescription() {
    return description;
  }

  public String getVendor() {
    return vendor;
  }

  public Profiles getProfiles() {
    if (profiles == null) {
      Map<Integer, Profile> idMap = new HashMap<>();
      MapWrapper<Profile> mw = new MapWrapper<>(ACS.isStrictOrder());
      Map<String, Profile> nameMap = mw.getMap();
      profiles = new Profiles(idMap, nameMap);
    }
    return profiles;
  }

  protected void setProfiles(Profiles profiles) {
    this.profiles = profiles;
  }

  @Override
  public String toString() {
    String retStr = "[" + name + "] [" + vendor + "] [" + description + "] ";
    retStr += getProfiles().getProfiles().length + " profiles, ";
    retStr += getUnittypeParameters().getUnittypeParameters().length + " unittype parameters";
    retStr += " (" + super.toString() + ")";
    return retStr;
  }

  public UnittypeParameters getUnittypeParameters() {
    if (unittypeParameters == null) {
      Map<Integer, UnittypeParameter> idMap = new HashMap<>();
      MapWrapper<UnittypeParameter> mw = new MapWrapper<UnittypeParameter>(ACS.isStrictOrder());
      Map<String, UnittypeParameter> nameMap = mw.getMap();
      unittypeParameters = new UnittypeParameters(idMap, nameMap, this);
    }
    return unittypeParameters;
  }

  protected void setUnittypeParameters(UnittypeParameters unittypeParameters) {
    this.unittypeParameters = unittypeParameters;
  }

  public Integer getId() {
    return id;
  }

  protected void setId(Integer id) {
    this.id = id;
  }

  protected String getOldName() {
    return oldName;
  }

  public Files getFiles() {
    if (files == null) {
      MapWrapper<File> mw1 = new MapWrapper<File>(ACS.isStrictOrder());
      Map<String, File> m1 = mw1.getMap();
      MapWrapper<File> mw2 = new MapWrapper<File>(ACS.isStrictOrder());
      Map<String, File> m2 = mw2.getMap();
      files = new Files(new HashMap<Integer, File>(), m1, m2, this);
    }
    return files;
  }

  protected void setFiles(Files files) {
    this.files = files;
  }

  public void setDescription(String desc) {
    this.description = desc;
  }

  public void setVendor(String vendor) {
    this.vendor = vendor.trim();
  }

  public Heartbeats getHeartbeats() {
    if (heartbeats == null) {
      Map<Integer, Heartbeat> idMap = new HashMap<>();
      MapWrapper<Heartbeat> mw = new MapWrapper<Heartbeat>(ACS.isStrictOrder());
      Map<String, Heartbeat> nameMap = mw.getMap();
      heartbeats = new Heartbeats(idMap, nameMap, this);
    }
    return heartbeats;
  }

  protected void setHeartbeats(Heartbeats heartbeats) {
    this.heartbeats = heartbeats;
  }

  public Triggers getTriggers() {
    if (triggers == null) {
      Map<Integer, Trigger> idMap = new HashMap<>();
      MapWrapper<Trigger> mw = new MapWrapper<Trigger>(ACS.isStrictOrder());
      Map<String, Trigger> nameMap = mw.getMap();
      triggers = new Triggers(idMap, nameMap, this);
    }
    return triggers;
  }

  protected void setTriggers(Triggers triggers) {
    this.triggers = triggers;
  }

  public Groups getGroups() {
    if (groups == null) {
      Map<Integer, Group> idMap = new HashMap<>();
      MapWrapper<Group> mw = new MapWrapper<Group>(ACS.isStrictOrder());
      Map<String, Group> nameMap = mw.getMap();
      groups = new Groups(idMap, nameMap, this);
    }
    return groups;
  }

  protected void setGroups(Groups groups) {
    this.groups = groups;
  }

  public SyslogEvents getSyslogEvents() {
    if (syslogEvents == null) {
      TreeMap<Integer, SyslogEvent> idMap =
          new TreeMap<Integer, SyslogEvent>(new NumberComparator());
      syslogEvents = new SyslogEvents(idMap, this);
    }
    return syslogEvents;
  }

  protected void setSyslogEvents(SyslogEvents syslogEvents) {
    this.syslogEvents = syslogEvents;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    if (!name.equals(this.name)) {
      this.oldName = this.name;
    }
    this.name = name;
  }

  public ProvisioningProtocol getProtocol() {
    return protocol;
  }

  public void setProtocol(ProvisioningProtocol protocol) {
    if (protocol == null) {
      throw new IllegalArgumentException("Unittype must specify protocol");
    }
    this.protocol = protocol;
  }

  protected void setOldName(String oldName) {
    this.oldName = oldName;
  }

  public Jobs getJobs() {
    if (jobs == null) {
      Map<Integer, Job> idMap = new HashMap<>();
      MapWrapper<Job> mw = new MapWrapper<Job>(ACS.isStrictOrder());
      Map<String, Job> nameMap = mw.getMap();
      jobs = new Jobs(idMap, nameMap, this);
    }
    return jobs;
  }

  protected void setJobs(Jobs jobs) {
    this.jobs = jobs;
  }

  public int compareTo(Unittype o) {
    if (o != null) {
      return getName().compareTo(o.getName());
    }
    return 0;
  }

  protected int ensureValidSystemParameters(ACS acs) throws SQLException {
    int changedParams = 0;
    if (acs.getDbi() != null) { // Will not run on startup (initialization case)
      List<UnittypeParameter> utpList = new ArrayList<>();
      for (Entry<String, UnittypeParameterFlag> entry :
          SystemParameters.commonParameters.entrySet()) {
        UnittypeParameter utp = getUnittypeParameters().getByName(entry.getKey());
        UnittypeParameterFlag newFlag = entry.getValue();
        if (utp == null) {
          utpList.add(new UnittypeParameter(this, entry.getKey(), entry.getValue()));
          changedParams++;
        } else if (!utp.getFlag().isSystem()) {
          utp.setFlag(newFlag);
          utpList.add(utp);
          changedParams++;
        }
      }
      getUnittypeParameters().addOrChangeUnittypeParameters(utpList, acs);
    }
    return changedParams;
  }
}
