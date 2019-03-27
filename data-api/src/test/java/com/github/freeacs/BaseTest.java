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
        hazelcastInstance.getMap("unitTypesById").clear();
        hazelcastInstance.getMap("unitTypesByName").clear();
        hazelcastInstance.getMap("profilesById").clear();
        jdbi.withHandle(handle -> {
            handle.createUpdate("DROP ALL OBJECTS").execute();
            return handle.createScript(FileUtil.readFileFromClasspath("/h2-schema.sql")).execute();
        });
        jdbi.withHandle(handle ->
                handle.createUpdate("insert into user_(username, secret, fullname, accesslist, is_admin) " +
                        "values('admin', '4E9BA006A68A8767D65B3761E038CF9040C54A00', 'Admin', 'Admin', 1);").execute());
    }
}
