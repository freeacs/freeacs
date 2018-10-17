package com.github.freeacs.web.app.table;

import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.GroupParameter;
import com.github.freeacs.dbi.Job;
import com.github.freeacs.dbi.JobParameter;
import com.github.freeacs.dbi.ProfileParameter;
import com.github.freeacs.dbi.Trigger;
import com.github.freeacs.dbi.UnitParameter;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.web.app.page.job.JobFilter;
import com.github.freeacs.web.app.util.WebConstants;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Table element list factory. Named wrongly, should be named TableElementListFactory. Because of
 * stability related to refactoring, renaming has not been prioritized.
 *
 * <p>Provides static factory methods for generating parameter lists that require the tree view.
 *
 * @author Jarl Andre Hubenthal
 */
public class TableElementMaker {
  public List<TableElement> getGroups(Unittype unittype) throws Exception {
    List<TableElement> list = new ArrayList<>();
    List<String> topLevelGroupnames =
        convertGroupsToNames(unittype.getGroups().getTopLevelGroups());
    Collections.sort(topLevelGroupnames, String.CASE_INSENSITIVE_ORDER);
    for (String groupname : topLevelGroupnames) {
      Group group = unittype.getGroups().getByName(groupname);
      getGroup(unittype, list, group, WebConstants.PARAMETERS_START_INDENTATION);
    }
    return list;
  }

  public List<TableElement> getTriggers(Unittype unittype) throws Exception {
    List<TableElement> list = new ArrayList<>();
    List<String> topLevelTriggerNames =
        convertTriggersToNames(unittype.getTriggers().getTopLevelTriggers());
    Collections.sort(topLevelTriggerNames, String.CASE_INSENSITIVE_ORDER);
    for (String triggername : topLevelTriggerNames) {
      Trigger trigger = unittype.getTriggers().getByName(triggername);
      getTrigger(unittype, list, trigger, WebConstants.PARAMETERS_START_INDENTATION);
    }
    return list;
  }

  private void getTrigger(
      Unittype unittype, List<TableElement> list, Trigger trigger, Integer nbsp) {
    String tableTriggerId = getTableTriggerId(trigger);
    if (trigger != null && !trigger.getChildren().isEmpty()) {
      list.add(new TableElement(tableTriggerId, nbsp, trigger, true));
      List<String> childrenTriggernames = convertTriggersToNames(trigger.getChildren());
      childrenTriggernames.sort(String.CASE_INSENSITIVE_ORDER);
      for (String childrenTriggername : childrenTriggernames) {
        Trigger g = unittype.getTriggers().getByName(childrenTriggername);
        getTrigger(unittype, list, g, nbsp + WebConstants.PARAMETERS_NEXT_INDENTATION);
      }
    } else {
      list.add(new TableElement(tableTriggerId, nbsp, trigger, false));
    }
  }

  private void getGroup(Unittype unittype, List<TableElement> list, Group group, Integer nbsp) {
    String tableGroupId = getTableGroupId(group);
    if (group != null && !group.getChildren().isEmpty()) {
      list.add(new TableElement(tableGroupId, nbsp, group, true));
      List<String> childrenGroupnames = convertGroupsToNames(group.getChildren());
      childrenGroupnames.sort(String.CASE_INSENSITIVE_ORDER);
      for (String childrenGroupname : childrenGroupnames) {
        Group g = unittype.getGroups().getByName(childrenGroupname);
        getGroup(unittype, list, g, nbsp + WebConstants.PARAMETERS_NEXT_INDENTATION);
      }
    } else {
      list.add(new TableElement(tableGroupId, nbsp, group, false));
    }
  }

  private List<String> convertGroupsToNames(List<Group> groups) {
    List<String> names = new ArrayList<>();
    for (Group g : groups) {
      names.add(g.getName());
    }
    return names;
  }

  private List<String> convertTriggersToNames(List<Trigger> triggers) {
    List<String> names = new ArrayList<>();
    for (Trigger t : triggers) {
      names.add(t.getName());
    }
    return names;
  }

  private String getTableGroupId(Group g) {
    if (g == null) {
      return null;
    }
    String id = g.getName().replace(".", "-");
    Group group = g;
    while ((group = group.getParent()) != null) {
      id = group.getName().replace(".", "-") + "." + id;
    }
    return id;
  }

  private String getTableTriggerId(Trigger g) {
    if (g == null) {
      return null;
    }
    String id = g.getName().replace(".", "-");
    Trigger group = g;
    while ((group = group.getParent()) != null) {
      id = group.getName().replace(".", "-") + "." + id;
    }
    return id;
  }

  public List<TableElement> getJobs(Unittype unittype) throws Exception {
    List<TableElement> list = new ArrayList<>();
    List<Job> topLevelJobs = findTopLevelJobs(unittype);
    if (!topLevelJobs.isEmpty()) {
      List<String> topLevelJobnames = convertJobsToNames(topLevelJobs);
      topLevelJobnames.sort(String.CASE_INSENSITIVE_ORDER);
      for (String jobname : topLevelJobnames) {
        Job topJob = unittype.getJobs().getByName(jobname);
        getJob(unittype, list, topJob, WebConstants.PARAMETERS_START_INDENTATION);
      }
    }
    return list;
  }

  /**
   * Gets the job.
   *
   * @param unittype the unittype
   * @param list the list
   * @param job the job
   * @param nbsp the nbsp
   * @return the job
   * @throws IllegalArgumentException the illegal argument exception
   * @throws SecurityException the security exception
   * @throws IllegalAccessException the illegal access exception
   * @throws InvocationTargetException the invocation target exception
   * @throws NoSuchMethodException the no such method exception
   */
  private void getJob(Unittype unittype, List<TableElement> list, Job job, Integer nbsp) {
    String id = getTableJobId(job);
    if (!getChildren(job).isEmpty()) {
      list.add(new TableElement(id, nbsp, job, true));
      List<String> childrenJobnames = convertJobsToNames(getChildren(job));
      childrenJobnames.sort(String.CASE_INSENSITIVE_ORDER);
      for (String childrenJobname : childrenJobnames) {
        Job childrenJob = unittype.getJobs().getByName(childrenJobname);
        getJob(unittype, list, childrenJob, nbsp + WebConstants.PARAMETERS_NEXT_INDENTATION);
      }
    } else {
      list.add(new TableElement(id, nbsp, job, false));
    }
  }

  /**
   * Find top level jobs.
   *
   * @param unittype the unittype
   * @return the list
   */
  private List<Job> findTopLevelJobs(Unittype unittype) {
    Job[] jobs = unittype.getJobs().getJobs();
    List<Job> topLevel = new ArrayList<>();
    for (Job j : jobs) {
      if (j.getDependency() == null) {
        topLevel.add(j);
      }
    }
    return topLevel;
  }

  /**
   * Gets the children.
   *
   * @param j the j
   * @return the children
   */
  private List<Job> getChildren(Job j) {
    return j.getChildren();
  }

  /**
   * Convert jobs to names.
   *
   * @param jobs the jobs
   * @return the list
   */
  private List<String> convertJobsToNames(List<Job> jobs) {
    List<String> names = new ArrayList<>();
    for (Job j : jobs) {
      names.add(j.getName());
    }
    return names;
  }

  private String getTableJobId(Job j) {
    String id = j.getName().replace(".", "-");
    Job job = j;
    while ((job = job.getDependency()) != null) {
      id = job.getName().replace(".", "-") + "." + id;
    }
    return id;
  }

  public List<TableElement> getParameters(Object[]... inputs) {
    return getParameters(null, inputs);
  }

  public List<TableElement> getParameters(JobFilter jobFilter, Object[]... inputs) {
    List<TableElement> tableElements = new ArrayList<>();
    UnittypeParameter[] params = getUnittypeParams(inputs);
    UnitParameter[] uParams = getUnitParams(inputs);
    UnitParameter[] uSessionParams = getUnitSessionParams(inputs);
    ProfileParameter[] pParams = getProfileParams(inputs);
    GroupParameter[] gParams = getGroupParams(inputs);
    JobParameter[] jParams = getJobParams(inputs);
    for (int i = 0; i < params.length; i++) {
      if (parameterShouldNotBeListed(jobFilter, params, i)) {
        continue;
      }
      String param = params[i].getName();
      String[] names = param.split("\\.");
      String id = "";
      Integer tab = WebConstants.PARAMETERS_START_INDENTATION;
      for (int j = 0; j < names.length; j++) {
        UnittypeParameter utp = j == (names.length - 1) ? params[i] : null;
        String name = names[j];
        if (!id.isEmpty()) {
          id += ".";
        }
        id += name;
        if (!tableElements.isEmpty()) {
          List<TableElement> copy = new ArrayList<>(tableElements);
          boolean isThere = false;
          for (TableElement element : copy) {
            if (element.getName().equals(id)) {
              isThere = true;
              break;
            }
          }
          if (!isThere) {
            tableElements.add(new TableElement(id, tab, utp));
          }
        } else {
          tableElements.add(new TableElement(id, tab, utp));
        }
        tab += WebConstants.PARAMETERS_NEXT_INDENTATION;
      }
    }
    Collections.sort(tableElements, new TableElementComparator());
    if (pParams != null) {
      for (ProfileParameter pParam : pParams) {
        for (TableElement te : tableElements) {
          if (te.getUnittypeParameter() == pParam.getUnittypeParameter()) {
            te.setProfileParameter(pParam);
            break;
          }
        }
      }
    }
    if (uParams != null) {
      for (UnitParameter uParam : uParams) {
        for (TableElement te : tableElements) {
          if (te.getUnittypeParameter() == uParam.getParameter().getUnittypeParameter()) {
            te.setUnitParameter(uParam);
            break;
          }
        }
      }
    }
    if (uSessionParams != null) {
      for (UnitParameter uSessionParam : uSessionParams) {
        for (TableElement te : tableElements) {
          if (te.getUnittypeParameter() == uSessionParam.getParameter().getUnittypeParameter()) {
            te.setUnitSessionParameter(uSessionParam);
            break;
          }
        }
      }
    }
    if (gParams != null) {
      for (GroupParameter gParam : gParams) {
        for (TableElement te : tableElements) {
          if (te.getUnittypeParameter() != null
              && te.getUnittypeParameter().equals(gParam.getParameter().getUnittypeParameter())
              && gParam.getId() != null) {
            te.addGroupParameter(gParam);
          }
        }
      }
    }
    if (jParams != null) {
      for (JobParameter jParam : jParams) {
        for (TableElement te : tableElements) {
          if (te.getUnittypeParameter() != null
              && te.getUnittypeParameter().equals(jParam.getParameter().getUnittypeParameter())) {
            te.setJobParameter(jParam);
            break;
          }
        }
      }
    }
    return tableElements;
  }

  private boolean parameterShouldNotBeListed(
      JobFilter jobFilter, UnittypeParameter[] params, int i) {
    return jobFilter != null && !jobFilter.listParameter(params[i]);
  }

  /**
   * Gets the group params.
   *
   * <p>We need to specifically make sure that each group parameter has an Id because of an
   * underlying bug somewhere else.
   *
   * @param inputs the inputs
   * @return the group params
   */
  private GroupParameter[] getGroupParams(Object[][] inputs) {
    for (Object[] list : inputs) {
      if (list instanceof GroupParameter[]) {
        List<GroupParameter> arr = new ArrayList<>();
        for (Object aList : list) {
          GroupParameter groupParam = (GroupParameter) aList;
          if (groupParam.getId() != null) {
            arr.add(groupParam);
          }
        }
        return arr.toArray(new GroupParameter[] {});
      }
    }
    return null;
  }

  /**
   * Gets the unit params.
   *
   * @param inputs the inputs
   * @return the unit params
   */
  private UnitParameter[] getUnitParams(Object[][] inputs) {
    for (Object[] list : inputs) {
      if (list instanceof UnitParameter[]) {
        return (UnitParameter[]) list;
      }
    }
    return null;
  }

  private UnitParameter[] getUnitSessionParams(Object[][] inputs) {
    // UGLY HACK, we expect the Unit-parameters to come first in the input to the
    // getParameters()-method
    // The next occurence of unit-parameters must then be the unit-session-parameters
    boolean foundUnitParameters = false;
    for (Object[] list : inputs) {
      if (foundUnitParameters && list instanceof UnitParameter[]) {
        return (UnitParameter[]) list;
      }
      if (list instanceof UnitParameter[]) {
        foundUnitParameters = true;
      }
    }
    return null;
  }

  /**
   * Gets the profile params.
   *
   * @param inputs the inputs
   * @return the profile params
   */
  private ProfileParameter[] getProfileParams(Object[][] inputs) {
    for (Object[] list : inputs) {
      if (list instanceof ProfileParameter[]) {
        return (ProfileParameter[]) list;
      }
    }
    return null;
  }

  /**
   * Gets the job params.
   *
   * @param inputs the inputs
   * @return the job params
   */
  private JobParameter[] getJobParams(Object[][] inputs) {
    for (Object[] list : inputs) {
      if (list instanceof JobParameter[]) {
        return (JobParameter[]) list;
      }
    }
    return null;
  }

  /**
   * Gets the unittype params.
   *
   * @param inputs the inputs
   * @return the unittype params
   */
  private UnittypeParameter[] getUnittypeParams(Object[][] inputs) {
    for (Object[] list : inputs) {
      if (list instanceof UnittypeParameter[]) {
        return (UnittypeParameter[]) list;
      }
    }
    return null;
  }
}
