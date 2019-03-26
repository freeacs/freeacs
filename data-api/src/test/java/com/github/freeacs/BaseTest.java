package com.github.freeacs;

import com.hazelcast.core.HazelcastInstance;
import org.jdbi.v3.core.Jdbi;
import org.junit.After;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class BaseTest {

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Autowired
    private Jdbi jdbi;

    @After
    public void init() throws IOException {
        hazelcastInstance.getMap("unitTypesById").removeAll(o -> true);
        hazelcastInstance.getMap("unitTypesByName").removeAll(o -> true);
        hazelcastInstance.getMap("profilesById").removeAll(o -> true);
        jdbi.withHandle(handle -> {
            handle.createUpdate("DROP ALL OBJECTS").execute();
            handle.createScript(FileUtil.readFileFromClasspath("/h2-schema.sql")).execute();
            return null;
        });
    }
}
