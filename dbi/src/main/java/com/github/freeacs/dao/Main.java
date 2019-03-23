package com.github.freeacs.dao;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.util.Date;


public class Main {
    private static IMap<String, UnitType> cachedUnitTypes;

    public static void main(String[] args) {
        HazelcastInstance instance = Hazelcast.newHazelcastInstance();
        cachedUnitTypes = instance.getMap("UnitTypes");
        cachedUnitTypes.put("123" + new Date().toString(), UnitType.builder().name("ssadd").vendor("sads").description("dsdadd").protocol(Protocol.TR069).build());
        cachedUnitTypes.values().forEach(System.out::println);
    }
}
