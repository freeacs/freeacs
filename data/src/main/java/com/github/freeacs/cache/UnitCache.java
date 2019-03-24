package com.github.freeacs.cache;

import com.github.freeacs.dao.Unit;
import com.github.freeacs.dao.UnitDao;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.SqlPredicate;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

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
