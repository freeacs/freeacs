package com.github.freeacs.cache;

import com.github.freeacs.dao.Unit;
import com.github.freeacs.dao.UnitDao;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.springframework.stereotype.Component;

@Component
public class UnitCache {
    private static final String KEY = "units";

    private final IMap<String, Unit> cache;

    private final UnitDao unitDao;

    public UnitCache(UnitDao unitDao, HazelcastInstance cache) {
        this.unitDao = unitDao;
        this.cache = cache.getMap(KEY);
    }

    public Option<Unit> getUnit(String unitId) {
        if (cache.containsKey(unitId)) {
            return Option.of(cache.get(unitId));
        }
        Option<Unit> maybeUnit = unitDao.getUnit(unitId);
        maybeUnit.forEach(unit -> cache.put(unitId, unit));
        return maybeUnit;
    }

    public void createUnit(Unit unit) {
        if (cache.containsKey(unit.getUnitId())) {
            throw new IllegalArgumentException("Unit already exists");
        }
        unitDao.createUnit(unit);
        cache.put(unit.getUnitId(), unit);
    }

    public List<Unit> searchForUnits(String searchStr, List<Long> profiles, Integer limit) {
        if (searchStr == null || searchStr.trim().length() == 0) {
            throw new IllegalArgumentException("Search string is required (is null or empty)");
        }
        if (profiles == null || profiles.isEmpty()) {
            throw new IllegalArgumentException("Profiles is required (is null or empty)");
        }
        return unitDao.searchForUnits(searchStr, profiles, limit);

    }

}
