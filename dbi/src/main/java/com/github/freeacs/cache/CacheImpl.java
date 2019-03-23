package com.github.freeacs.cache;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;

public class CacheImpl implements Cache {

    private static Cache CACHE;

    public static Cache getInstance() {
        if (CACHE == null) {
            CACHE = new CacheImpl();
        }
        return CACHE;
    }

    private HazelcastInstance hazelcastInstance;

    private CacheImpl() {
        hazelcastInstance = Hazelcast.newHazelcastInstance();
    }

    public CacheImpl(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public <T> IList<T> getList(String key) {
        return hazelcastInstance.getList(key);
    }

    @Override
    public <K, V> IMap<K, V> getMap(String key) {
        return hazelcastInstance.getMap(key);
    }
}
