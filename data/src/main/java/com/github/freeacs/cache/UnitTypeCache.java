package com.github.freeacs.cache;

import com.github.freeacs.dao.UnitType;
import com.github.freeacs.dao.UnitTypeDao;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import io.vavr.control.Option;

public class UnitTypeCache {
    private static final String KEY = "unitTypes";

    private final IMap<Long, UnitType> cache;

    private final UnitTypeDao unitTypeDao;

    public UnitTypeCache(UnitTypeDao unitTypeDao, HazelcastInstance cache) {
        this.unitTypeDao = unitTypeDao;
        this.cache = cache.getMap(KEY);
    }

    public Option<UnitType> getUnitType(Long id) {
        if (cache.containsKey(id)) {
            return Option.of(cache.get(id));
        }
        Option<UnitType> maybeUnitType = unitTypeDao.getUnitType(id);
        maybeUnitType.forEach(unitType -> cache.put(id, unitType));
        return maybeUnitType;
    }
}
