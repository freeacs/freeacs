package com.github.freeacs.common.cache;

import java.util.List;

public class NoOpACSCacheManager implements ACSCacheManager {
    @Override
    public <T> T get(String key, Class<T> type) {
        return null;
    }

    @Override
    public <T> List<T> getList(String key, Class<T> type) {
        return null;
    }

    @Override
    public void put(String key, Object value) {
        // Do nothing
    }

    @Override
    public void evict(String key) {
        // Do nothing
    }
}