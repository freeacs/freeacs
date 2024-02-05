package com.github.freeacs.cache;

import com.github.freeacs.cache.serializers.FileSerializer;
import com.hazelcast.config.CompactSerializationConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.hazelcast.core.Hazelcast.newHazelcastInstance;

@Configuration
@EnableCaching
@ConditionalOnProperty(name = "cache.enabled", havingValue = "true")
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        var hazelcastConfig = new Config();
        var serializationConfig = new SerializationConfig();
        var compactSerializationConfig = new CompactSerializationConfig();
        compactSerializationConfig.addSerializer(new FileSerializer());
        serializationConfig.setCompactSerializationConfig(compactSerializationConfig);
        hazelcastConfig.setSerializationConfig(serializationConfig);
        hazelcastConfig.setProperty("hazelcast.logging.type", "slf4j");
        var hazelcastInstance = newHazelcastInstance(hazelcastConfig);
        return new HazelcastCacheManager(hazelcastInstance);
    }
}
