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
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a "table element", with unittype parameter, tabbing and other parameters
 * like unit or profile.
 *
 * @author Jarl Andre Hubenthal
 */
@Setter
@Getter
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
   * Gets the list of group parameters.
   *
   * @return the group parameter
   */
  public List<GroupParameter> getGroupParameter() {
    return groupParameterList;
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
   * Gets the unittype parameter.
   *
   * @return the unittype parameter
   */
  public UnittypeParameter getUnittypeParameter() {
    return unitTypeParameter;
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

}
