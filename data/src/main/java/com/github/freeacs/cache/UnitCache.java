package com.github.freeacs.cache;

import com.github.freeacs.dao.Unit;
import com.github.freeacs.dao.UnitDao;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class UnitCache {
    private static final String KEY = "units";

    private final IMap<String, Unit> unitIdCache;

    private final UnitDao unitDao;

    public UnitCache(UnitDao unitDao, @Qualifier("hazelcastInstance") HazelcastInstance cache) {
        this.unitDao = unitDao;
        this.unitIdCache = cache.getMap(KEY);
    }

    public Option<Unit> getUnit(String unitId) {
        Unit unitFromCache = unitIdCache.get(unitId);
        if (unitFromCache != null) {
            return Option.of(unitFromCache);
        }
        return unitDao.getUnit(unitId)
                .map(unit -> {
                    unitIdCache.put(unitId, unit);
                    return unit;
                });
    }

    public void createUnit(Unit unit) {
        if (unitIdCache.containsKey(unit.getUnitId())) {
            throw new IllegalArgumentException("Unit already exists");
        }
        unitDao.createUnit(unit);
        unitIdCache.put(unit.getUnitId(), unit);
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
