package com.github.freeacs.common.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryACSCacheManager implements ACSCacheManager {
    private final Map<String, Object> cache = new HashMap<>();

    @Override
    public <T> T get(String key, Class<T> type) {
        //noinspection unchecked
        return cache.get(key) != null ? (T) cache.get(key) : null;
    }

    @Override
    public <T> List<T> getList(String key, Class<T> type) {
        //noinspection unchecked
        return cache.get(key) != null ? (List<T>) cache.get(key) : null;
    }

    @Override
    public void put(String key, Object value) {
        cache.put(key, value);
    }

    @Override
    public void evict(String key) {
        cache.remove(key);
    }
}
