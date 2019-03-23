package com.github.freeacs.cache;

import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;

public interface Cache {
    <T> IList<T> getList(String key);

    <K, V> IMap<K, V> getMap(String key);
}
