package com.github.freeacs.web.app.table;

import com.github.freeacs.dbi.*;
import com.github.freeacs.web.app.page.job.JobFilter;
import com.github.freeacs.web.app.util.WebConstants;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Table element list factory. Named wrongly, should be named TableElementListFactory.
 * Because of stability related to refactoring, renaming has not been prioritized.
 * 
 * Provides static factory methods for generating parameter lists that require the tree view.
 * 
 * @author Jarl Andre Hubenthal
 *
 */
public class TableElementMaker {
	public TableElementMaker() {
	}

	public List<TableElement> getGroups(Unittype unittype) throws Exception {
		List<TableElement> list = new ArrayList<TableElement>();
		List<String> topLevelGroupnames = convertGroupsToNames(unittype.getGroups().getTopLevelGroups());
		Collections.sort(topLevelGroupnames, String.CASE_INSENSITIVE_ORDER);
		for (String groupname : topLevelGroupnames) {
			Group group = unittype.getGroups().getByName(groupname);
			getGroup(unittype, list, group, WebConstants.PARAMETERS_START_INDENTATION);
		}
		return list;
	}

	public List<TableElement> getTriggers(Unittype unittype) throws Exception {
		List<TableElement> list = new ArrayList<TableElement>();
		List<String> topLevelTriggerNames = convertTriggersToNames(unittype.getTriggers().getTopLevelTriggers());
		Collections.sort(topLevelTriggerNames, String.CASE_INSENSITIVE_ORDER);
		for (String triggername : topLevelTriggerNames) {
			Trigger trigger = unittype.getTriggers().getByName(triggername);
			getTrigger(unittype, list, trigger, WebConstants.PARAMETERS_START_INDENTATION);
		}
		return list;
	}

	private void getTrigger(Unittype unittype, List<TableElement> list, Trigger trigger, Integer nbsp) throws IllegalArgumentException, SecurityException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		String tableTriggerId = getTableTriggerId(trigger);
		Integer str = nbsp;
		if (trigger != null && trigger.getChildren().size() > 0) {
			list.add(new TableElement(tableTriggerId, str, trigger, true));
			List<String> childrenTriggernames = convertTriggersToNames(trigger.getChildren());
			Collections.sort(childrenTriggernames, String.CASE_INSENSITIVE_ORDER);
			for (String childrenTriggername : childrenTriggernames) {
				Trigger g = unittype.getTriggers().getByName(childrenTriggername);
				getTrigger(unittype, list, g, nbsp + WebConstants.PARAMETERS_NEXT_INDENTATION);
			}
		} else
			list.add(new TableElement(tableTriggerId, str, trigger, false));
	}

	private void getGroup(Unittype unittype, List<TableElement> list, Group group, Integer nbsp) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		String tableGroupId = getTableGroupId(group);
		Integer str = nbsp;
		if (group != null && group.getChildren().size() > 0) {
			list.add(new TableElement(tableGroupId, str, group, true));
			List<String> childrenGroupnames = convertGroupsToNames(group.getChildren());
			Collections.sort(childrenGroupnames, String.CASE_INSENSITIVE_ORDER);
			for (String childrenGroupname : childrenGroupnames) {
				Group g = unittype.getGroups().getByName(childrenGroupname);
				getGroup(unittype, list, g, nbsp + WebConstants.PARAMETERS_NEXT_INDENTATION);
			}
		} else
			list.add(new TableElement(tableGroupId, str, group, false));
	}

	private List<String> convertGroupsToNames(List<Group> groups) {
		List<String> names = new ArrayList<String>();
		for (Group g : groups) {
			names.add(g.getName());
		}
		return names;
	}

	private List<String> convertTriggersToNames(List<Trigger> triggers) {
		List<String> names = new ArrayList<String>();
		for (Trigger t : triggers) {
			names.add(t.getName());
		}
		return names;
	}

	private String getTableGroupId(Group g) {
		if (g == null)
			return null;
		String id = g.getName().replace(".", "-");
		Group group = g;
		while ((group = group.getParent()) != null) {
			id = group.getName().replace(".", "-") + "." + id;
		}
		return id;
	}

	private String getTableTriggerId(Trigger g) {
		if (g == null)
			return null;
		String id = g.getName().replace(".", "-");
		Trigger group = g;
		while ((group = group.getParent()) != null) {
			id = group.getName().replace(".", "-") + "." + id;
		}
		return id;
	}

	public List<TableElement> getJobs(Unittype unittype) throws Exception {
		List<TableElement> list = new ArrayList<TableElement>();
		List<Job> topLevelJobs = findTopLevelJobs(unittype);
		if (topLevelJobs.size() > 0) {
			List<String> topLevelJobnames = convertJobsToNames(topLevelJobs);
			Collections.sort(topLevelJobnames, String.CASE_INSENSITIVE_ORDER);
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
	private void getJob(Unittype unittype, List<TableElement> list, Job job, Integer nbsp) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		String id = getTableJobId(job);
		Integer str = nbsp;
		if (getChildren(job).size() > 0) {
			list.add(new TableElement(id, str, job, true));
			List<String> childrenJobnames = convertJobsToNames(getChildren(job));
			Collections.sort(childrenJobnames, String.CASE_INSENSITIVE_ORDER);
			for (String childrenJobname : childrenJobnames) {
				Job childrenJob = unittype.getJobs().getByName(childrenJobname);
				getJob(unittype, list, childrenJob, nbsp + WebConstants.PARAMETERS_NEXT_INDENTATION);
			}
		} else
			list.add(new TableElement(id, str, job, false));
	}

	/**
	 * Find top level jobs.
	 *
	 * @param unittype the unittype
	 * @return the list
	 */
	private List<Job> findTopLevelJobs(Unittype unittype) {
		Job[] jobs = unittype.getJobs().getJobs();
		List<Job> topLevel = new ArrayList<Job>();
		for (Job j : jobs) {
			if (j.getDependency() == null)
				topLevel.add(j);
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
		List<Job> jobs = j.getChildren();
		return jobs;
	}

	/**
	 * Convert jobs to names.
	 *
	 * @param jobs the jobs
	 * @return the list
	 */
	private List<String> convertJobsToNames(List<Job> jobs) {
		List<String> names = new ArrayList<String>();
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
		List<TableElement> tableElements = new ArrayList<TableElement>();
		UnittypeParameter[] params = getUnittypeParams(inputs);
		UnitParameter[] uParams = getUnitParams(inputs);
		UnitParameter[] uSessionParams = getUnitSessionParams(inputs);
		ProfileParameter[] pParams = getProfileParams(inputs);
		GroupParameter[] gParams = getGroupParams(inputs);
		JobParameter[] jParams = getJobParams(inputs);
		for (int i = 0; i < params.length; i++) {
			if (parameterShouldNotBeListed(jobFilter, params, i))
				continue;
			String param = params[i].getName();
			String[] names = param.split("\\.");
			String id = "";
			Integer tab = WebConstants.PARAMETERS_START_INDENTATION;
			for (int j = 0; j < names.length; j++) {
				UnittypeParameter utp = (j == (names.length - 1) ? params[i] : null);
				String name = names[j];
				if (id.length() > 0)
					id += ".";
				id += name;
				if (tableElements.size() > 0) {
					List<TableElement> copy = new ArrayList<TableElement>();
					copy.addAll(tableElements);
					boolean isThere = false;
					for (TableElement element : copy) {
						if (element.getName().equals(id)) {
							isThere = true;
							break;
						}
					}
					if (!isThere)
						tableElements.add(new TableElement(id, tab, utp));
				} else
					tableElements.add(new TableElement(id, tab, utp));
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
					if (te.getUnittypeParameter() != null && te.getUnittypeParameter().equals(gParam.getParameter().getUnittypeParameter()) && gParam.getId() != null)
						te.addGroupParameter(gParam);
				}
			}
		}
		if (jParams != null) {
			for (JobParameter jParam : jParams) {
				for (TableElement te : tableElements) {
					if (te.getUnittypeParameter() != null && te.getUnittypeParameter().equals(jParam.getParameter().getUnittypeParameter())) {
						te.setJobParameter(jParam);
						break;
					}
				}
			}
		}
		return tableElements;
	}

	private boolean parameterShouldNotBeListed(JobFilter jobFilter, UnittypeParameter[] params, int i) {
		return (jobFilter != null && !jobFilter.listParameter(params[i]));
	}

	//	private boolean parameterFlagIsInspection(UnittypeParameter[] params, int i) {
	//		return false;
	//		//		return params[i].getFlag().isInspection();
	//	}

	//	private boolean eitherGroupParamsOrJobParamsIsNotNull(GroupParameter[] gParams, JobParameter[] jParams) {
	//		return (gParams != null || jParams != null);
	//	}

	/**
	 * Gets the group params.
	 * 
	 * We need to specifically make sure that each group parameter has an Id
	 * because of an underlying bug somewhere else.
	 *
	 * @param inputs the inputs
	 * @return the group params
	 */
	private GroupParameter[] getGroupParams(Object[][] inputs) {
		for (Object[] list : inputs) {
			if (list instanceof GroupParameter[]) {
				List<GroupParameter> arr = new ArrayList<GroupParameter>();
				for (int i = 0; i < list.length; i++) {
					GroupParameter groupParam = (GroupParameter) list[i];
					if (groupParam.getId() != null)
						arr.add(groupParam);
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
		// UGLY HACK, we expect the Unit-parameters to come first in the input to the getParameters()-method
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