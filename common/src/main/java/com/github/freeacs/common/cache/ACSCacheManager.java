package com.github.freeacs.common.cache;

/**
 * Generic cache manager interface
 */
public interface ACSCacheManager {
    <T> T get(String key, Class<T> type);
    void put(String key, Object value);
    void evict(String key);
}