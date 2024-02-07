package com.github.freeacs.common.cache;

import java.util.List;

/**
 * Generic cache manager interface
 */
public interface ACSCacheManager {
    <T> T get(String key, Class<T> type);

    <T> List<T> getList(String key, Class<T> type);

    void put(String key, Object value);
    void evict(String key);
}