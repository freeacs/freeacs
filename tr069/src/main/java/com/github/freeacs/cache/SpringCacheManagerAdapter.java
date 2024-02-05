package com.github.freeacs.cache;

import com.github.freeacs.common.cache.ACSCacheManager;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringCacheManagerAdapter implements ACSCacheManager {

    private final CacheManager cacheManager;

    public SpringCacheManagerAdapter(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        Cache cache = cacheManager.getCache("defaultCache"); // Adjust the cache name as needed
        if (cache == null) return null;
        Cache.ValueWrapper wrapper = cache.get(key);
        if (wrapper == null) return null;
        Object value = wrapper.get();
        if (value != null && !type.isAssignableFrom(value.getClass())) {
            throw new ClassCastException("The value in the cache is not of the expected type");
        }
        //noinspection unchecked
        return (T) value;
    }

    @Override
    public void put(String key, Object value) {
        Cache cache = cacheManager.getCache("defaultCache"); // Adjust the cache name as needed
        if (cache != null) {
            cache.put(key, value);
        }
    }

    @Override
    public void evict(String key) {
        Cache cache = cacheManager.getCache("defaultCache"); // Adjust the cache name as needed
        if (cache != null) {
            cache.evict(key);
        }
    }
}
