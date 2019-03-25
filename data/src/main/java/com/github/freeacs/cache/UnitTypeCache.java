package com.github.freeacs.cache;

import com.github.freeacs.dao.UnitType;
import com.github.freeacs.dao.UnitTypeDao;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import io.vavr.control.Option;
import org.springframework.stereotype.Component;

@Component
public class UnitTypeCache {
    private static final String KEY_BY_ID = "unitTypesById";
    private static final String KEY_BY_NAME = "unitTypesByName";

    private final IMap<String, UnitType> nameCache;
    private final IMap<Long, UnitType> idCache;

    private final UnitTypeDao unitTypeDao;

    public UnitTypeCache(UnitTypeDao unitTypeDao, HazelcastInstance cache) {
        this.unitTypeDao = unitTypeDao;
        this.idCache = cache.getMap(KEY_BY_ID);
        this.nameCache = cache.getMap(KEY_BY_NAME);
    }

    public Option<UnitType> getUnitTypeById(Long id) {
        UnitType unitTypeFromCache = idCache.get(id);
        if (unitTypeFromCache != null) {
            return Option.of(unitTypeFromCache);
        }
        return unitTypeDao.getUnitTypeById(id)
            .map(unitType -> idCache.put(id, nameCache.put(unitType.getName(), unitType)));
    }

    public Option<UnitType> getUnitTypeByName(String name) {
        UnitType unitTypeFromCache = nameCache.get(name);
        if (unitTypeFromCache != null) {
            return Option.of(unitTypeFromCache);
        }
        return unitTypeDao.getUnitTypeByName(name)
            .map(unitType -> idCache.put(unitType.getId(), nameCache.put(unitType.getName(), unitType)));
    }

    public Long createUnitType(UnitType unitType) {
        if (nameCache.containsKey(unitType.getName())) {
            throw new IllegalArgumentException("UnitType already exists with this name: " + unitType.getName());
        }
        Long newId = unitTypeDao.createUnitType(unitType);
        UnitType withId = unitType.withId(newId);
        idCache.put(newId, withId);
        nameCache.put(unitType.getName(), withId);
        return newId;
    }
}
