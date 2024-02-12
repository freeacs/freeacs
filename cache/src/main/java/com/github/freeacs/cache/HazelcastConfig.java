package com.github.freeacs.cache;

import com.github.freeacs.cache.serializers.FileSerializer;
import com.hazelcast.config.CompactSerializationConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public interface HazelcastConfig {
    static HazelcastInstance getHazelcastInstance() {
        var hazelcastConfig = Config.load();
        hazelcastConfig.getJetConfig().setEnabled(true);
        var serializationConfig = new SerializationConfig();
        var compactSerializationConfig = new CompactSerializationConfig();
        compactSerializationConfig.addSerializer(new FileSerializer());
        serializationConfig.setCompactSerializationConfig(compactSerializationConfig);
        hazelcastConfig.setSerializationConfig(serializationConfig);
        hazelcastConfig.setProperty("hazelcast.logging.type", "slf4j");
        return Hazelcast.newHazelcastInstance(hazelcastConfig);
    }
}
