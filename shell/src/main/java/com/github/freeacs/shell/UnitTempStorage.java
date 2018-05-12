package com.github.freeacs.shell;

import com.github.freeacs.dbi.Profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnitTempStorage {

	private Map<Profile, List<String>> map = new HashMap<Profile, List<String>>();

	private int counter = 0;

	public Map<Profile, List<String>> getUnits() {
		if (map == null)
			map = new HashMap<Profile, List<String>>();
		return map;
	}

	public void addUnit(Profile profile, String unitId) {
		Map<Profile, List<String>> map = getUnits();
		List<String> units = map.get(profile);
		if (units == null) {
			units = new ArrayList<String>();
			map.put(profile, units);
		}
		units.add(unitId);
		counter++;
	}

	public int size() {
		return counter;
	}

	public void reset() {
		map = new HashMap<Profile, List<String>>();
		counter = 0;
	}

}
