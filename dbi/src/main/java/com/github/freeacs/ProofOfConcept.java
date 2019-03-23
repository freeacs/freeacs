package com.github.freeacs;

import com.github.freeacs.cache.CacheImpl;
import com.github.freeacs.cache.UnitCache;
import com.github.freeacs.dao.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.jdbi.v3.core.Jdbi;

import java.util.Collection;

public class ProofOfConcept {
    public static void main(String[] args) {
        Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test;MODE=MYSQL;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM 'classpath:h2-schema.sql'").installPlugins();
        UnitDao unitDao = jdbi.onDemand(UnitDao.class);
        UnitTypeDao unitTypeDao = jdbi.onDemand(UnitTypeDao.class);
        Long unitTypeId = unitTypeDao.createUnitType(new UnitType(null, "Test", "Test", "Test", Protocol.TR069));
        ProfileDao profileDao = jdbi.onDemand(ProfileDao.class);
        Long profileId = profileDao.createProfile(new Profile(null, "Default", unitTypeId));
        HazelcastInstance instance = Hazelcast.newHazelcastInstance();
        UnitCache unitCache = new UnitCache(unitDao, new CacheImpl(instance));
        unitCache.createUnit(new Unit("123", profileId, unitTypeId));
        // just a dummy example, not possible to have 2 - 10 million units in memory
        Collection<Unit> units = unitCache.searchForUnits("2", io.vavr.collection.List.of(1L));
        units.forEach(System.out::println);
    }
}
