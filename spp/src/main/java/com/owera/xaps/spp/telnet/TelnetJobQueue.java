package com.owera.xaps.spp.telnet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TelnetJobQueue {

	// Contains a map for each job-id, each map contains Key:Unitid, Value: TelnetJob-object
	private Map<Integer, Map<String, TelnetJob>> metMap = new HashMap<Integer, Map<String, TelnetJob>>();
	// Contains iterators for each job-id. Each iterate over a  (UnitId,TelnetJob)-map found in the metaMap object
	// Needed to keep track of which unit is to be run the next time
	private Map<Integer, Iterator<String>> iteratorMap = new HashMap<Integer, Iterator<String>>();
	// List of telnet-jobs specified 
	private List<Integer> jobIds = new ArrayList<Integer>();
	private int jobIdsIndex = 0;
	private int recursiveDepth = 0;

	public Map<String, TelnetJob> getTelnetJobMap(Integer jobId) {
		return metMap.get(jobId);
	}

	public void put(Integer jobId, Map<String, TelnetJob> map) {
		metMap.put(jobId, map);
		if (!jobIds.contains(jobId))
			jobIds.add(jobId);
	}

	public void remove(Integer jobId) {
		metMap.remove(jobId);
		iteratorMap.remove(jobId);
		if (jobIds.contains(jobId))
			jobIds.remove(jobId);
	}

	public TelnetJob getNextTelnetJob() {
		if (jobIds.size() == 0)
			return null;
		Integer jobId = jobIds.get(jobIdsIndex);
		Map<String, TelnetJob> map = metMap.get(jobId);
		Iterator<String> iterator = iteratorMap.get(jobId);
		if (iterator == null) {
			iterator = map.keySet().iterator();
			iteratorMap.put(jobId, iterator);
		}
		jobIdsIndex++;
		if (jobIdsIndex >= jobIds.size())
			jobIdsIndex = 0;

		if (iterator.hasNext()) {
			recursiveDepth = 0;
			String unitId = iterator.next();
			return map.get(unitId);
		} else {
			recursiveDepth++;
			if (recursiveDepth > jobIds.size())
				return null;
			else
				return getNextTelnetJob();
		}
	}
}
