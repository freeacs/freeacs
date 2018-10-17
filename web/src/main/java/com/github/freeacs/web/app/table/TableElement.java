package com.github.freeacs.web.app.table;

import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.GroupParameter;
import com.github.freeacs.dbi.Job;
import com.github.freeacs.dbi.JobParameter;
import com.github.freeacs.dbi.ProfileParameter;
import com.github.freeacs.dbi.Trigger;
import com.github.freeacs.dbi.UnitParameter;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.web.app.page.trigger.ReleaseTrigger;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a "table element", with unittype parameter, tabbing and other parameters
 * like unit or profile.
 *
 * @author Jarl Andre Hubenthal
 */
public class TableElement {
  /** The name. */
  private String name;

  /** The amount of left margin (or tab as it can be called). */
  private Integer tab;

  /** The trigger. */
  private Trigger trigger;

  /** The Release trigger information object. */
  private ReleaseTrigger releaseTrigger;

  /** The group. */
  private Group group;

  /** The gp. */
  private List<GroupParameter> groupParameterList;

  /** The group parent. */
  private boolean groupParent;

  /** The trigger parent. */
  private boolean triggerParent;

  /** The job. */
  private Job job;

  /** The jp. */
  private JobParameter jobParameter;

  /** The job parent. */
  private boolean jobParent;

  /** The pp. */
  private ProfileParameter profileParameter;

  /** The up. */
  private UnitParameter unitParameter;

  private UnitParameter unitSessionParameter;

  /** The utp. */
  private UnittypeParameter unitTypeParameter;

  /**
   * It should be impossible to "do a new" on this class outside the package. Especially without any
   * constructor parameters.
   */
  @SuppressWarnings("unused")
  private TableElement() {}

  /**
   * Instantiates a new table element.
   *
   * @param name the name
   * @param tab the tab
   * @param group the group
   * @param parent the parent
   */
  TableElement(String name, Integer tab, Group group, boolean parent) {
    this.tab = tab;
    this.name = name;
    this.group = group;
    this.groupParent = parent;
  }

  TableElement(String name, Integer tab, Trigger trigger, boolean parent) {
    this.tab = tab;
    this.name = name;
    this.trigger = trigger;
    this.triggerParent = parent;
  }

  /**
   * Instantiates a new table element.
   *
   * @param name the name
   * @param tab the tab
   * @param job the job
   * @param parent the parent
   */
  TableElement(String name, Integer tab, Job job, boolean parent) {
    this.tab = tab;
    this.name = name;
    this.job = job;
    this.jobParent = parent;
  }

  /**
   * Instantiates a new table element.
   *
   * @param name the name
   * @param tab the tab
   * @param utp the utp
   */
  TableElement(String name, Integer tab, UnittypeParameter utp) {
    this.tab = tab;
    this.name = name;
    this.unitTypeParameter = utp;
  }

  public void addGroupParameter(GroupParameter groupParameter) {
    if (groupParameterList == null) {
      groupParameterList = new ArrayList<>();
    }
    groupParameterList.add(new TableGroupParameter(groupParameter));
  }

  /**
   * Just to avoid making hundreds of set methods for each of them.
   *
   * @param param the param
   */
  //	public void addParameter(Object param) {
  //		if (param instanceof UnitParameter)
  //			unitParameter = (UnitParameter) param;
  //		else if (param instanceof ProfileParameter)
  //			profileParameter = (ProfileParameter) param;
  //		else if (param instanceof JobParameter)
  //			jobParameter = (JobParameter) param;
  //		else
  //			throw new IllegalArgumentException("Parameter must be one of the following types:
  // Unit,Profile,Group or Job");
  //	}

  /**
   * Gets the group.
   *
   * @return the group
   */
  public Group getGroup() {
    return group;
  }

  /**
   * Gets the list of group parameters.
   *
   * @return the group parameter
   */
  public List<GroupParameter> getGroupParameter() {
    return groupParameterList;
  }

  /**
   * Gets the job.
   *
   * @return the job
   */
  public Job getJob() {
    return job;
  }

  /**
   * Gets the job parameter.
   *
   * @return the job parameter
   */
  public JobParameter getJobParameter() {
    return jobParameter;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Gets the profile parameter.
   *
   * @return the profile parameter
   */
  public ProfileParameter getProfileParameter() {
    return profileParameter;
  }

  /**
   * Gets the short name.
   *
   * @return the short name
   */
  public String getShortName() {
    return name.substring(name.lastIndexOf('.') + 1);
  }

  /**
   * Gets the tab.
   *
   * @return the tab
   */
  public Integer getTab() {
    return this.tab;
  }

  /**
   * Gets the unit parameter.
   *
   * @return the unit parameter
   */
  public UnitParameter getUnitParameter() {
    return unitParameter;
  }

  /**
   * Gets the unittype parameter.
   *
   * @return the unittype parameter
   */
  public UnittypeParameter getUnittypeParameter() {
    return unitTypeParameter;
  }

  /**
   * Checks if is group parent.
   *
   * @return true, if is group parent
   */
  public boolean isGroupParent() {
    return groupParent;
  }

  /**
   * Checks if is job parent.
   *
   * @return true, if is job parent
   */
  public boolean isJobParent() {
    return jobParent;
  }

  /**
   * Sets the group.
   *
   * @param group the new group
   */
  public void setGroup(Group group) {
    this.group = group;
  }

  /**
   * Sets the group parent.
   *
   * @param groupParent the new group parent
   */
  public void setGroupParent(boolean groupParent) {
    this.groupParent = groupParent;
  }

  /**
   * Sets the job.
   *
   * @param job the new job
   */
  public void setJob(Job job) {
    this.job = job;
  }

  /**
   * Sets the job parent.
   *
   * @param jobParent the new job parent
   */
  public void setJobParent(boolean jobParent) {
    this.jobParent = jobParent;
  }

  /**
   * DEBUG PURPOSES ONLY. HAS NOTHING DO WITH THE TABLE RENDERING. USED FOR EASILY DEBUGGING TABLE
   * ELEMENTS WHILE STEPPING IN DEBUG MDOE.
   */
  @Override
  public String toString() {
    return "["
        + tab
        + getName()
        + " - "
        + (unitTypeParameter != null ? unitTypeParameter.getName() : "null")
        + "]";
  }

  public Trigger getTrigger() {
    return trigger;
  }

  public void setTrigger(Trigger trigger) {
    this.trigger = trigger;
  }

  public boolean isTriggerParent() {
    return triggerParent;
  }

  public void setTriggerParent(boolean triggerParent) {
    this.triggerParent = triggerParent;
  }

  public ReleaseTrigger getReleaseTrigger() {
    return releaseTrigger;
  }

  public void setReleaseTrigger(ReleaseTrigger releaseTrigger) {
    this.releaseTrigger = releaseTrigger;
  }

  public UnitParameter getUnitSessionParameter() {
    return unitSessionParameter;
  }

  public void setJobParameter(JobParameter jobParameter) {
    this.jobParameter = jobParameter;
  }

  public void setProfileParameter(ProfileParameter profileParameter) {
    this.profileParameter = profileParameter;
  }

  public void setUnitParameter(UnitParameter unitParameter) {
    this.unitParameter = unitParameter;
  }

  public void setUnitSessionParameter(UnitParameter unitSessionParameter) {
    this.unitSessionParameter = unitSessionParameter;
  }
}
