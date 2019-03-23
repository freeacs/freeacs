package com.github.freeacs.cache;

import com.github.freeacs.dao.Unit;
import com.github.freeacs.dao.UnitDao;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.SqlPredicate;
import io.vavr.collection.List;

import java.util.Collection;
import java.util.stream.Collectors;

public class UnitCache {
    public static final String KEY = "units";

    private final IMap<String, Unit> cache;

    private final UnitDao unitDao;

    public UnitCache(UnitDao unitDao, Cache cache) {
        this.unitDao = unitDao;
        this.cache = cache.getMap(KEY);
    }

    public Unit getUnit(String unitId) {
        if (cache.containsKey(unitId)) {
            return cache.get(unitId);
        }
        Unit unit = unitDao.getUnit(unitId);
        cache.put(unitId, unit);
        return unit;
    }

    public void createUnit(Unit unit) {
        if (cache.containsKey(unit.getUnitId())) {
            throw new IllegalArgumentException("Unit already exists");
        }
        unitDao.createUnit(unit);
        cache.put(unit.getUnitId(), unit);
    }

    /**
     * Just a dummy example. Its not practically possible to use a cache for units.
     *
     * It can be 2 - 10 million units in the database. Which is too much to preload.
     */
    public Collection<Unit> searchForUnits(String searchStr, List<Long> profiles) {
        if (searchStr == null || searchStr.trim().length() == 0) {
            throw new IllegalArgumentException("Search string is required (is null or empty)");
        }
        if (profiles == null || profiles.isEmpty()) {
            throw new IllegalArgumentException("Profiles is required (is null or empty)");
        }
        String profileIds = profiles.map(Object::toString).collect(Collectors.joining(","));
        Predicate predicate = new SqlPredicate("unitId LIKE '%" + searchStr + "%' AND profileId IN (" + profileIds + ")");
        return cache.values(predicate);
    }

}
