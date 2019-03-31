package com.github.freeacs;

import com.hazelcast.core.HazelcastInstance;
import org.jdbi.v3.core.Jdbi;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class BaseTest {

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Autowired
    private Jdbi jdbi;

    @Before
    public void init() {
        jdbi.withHandle(handle ->
                handle.createUpdate("insert into user_(username, secret, fullname, accesslist, is_admin) " +
                        "values('admin', 'A33E0694639DA19CF58FA1130B2D767F6F4531019FDD45D73D178CED', 'Admin', 'Admin', 1);").execute());
    }

    @After
    public void teardown() throws IOException {
        hazelcastInstance.getMap("unitTypesById").clear();
        hazelcastInstance.getMap("unitTypesByName").clear();
        hazelcastInstance.getMap("profilesById").clear();
        jdbi.withHandle(handle -> {
            handle.createUpdate("DROP ALL OBJECTS").execute();
            return handle.createScript(FileUtil.readFileFromClasspath("/h2-schema.sql")).execute();
        });
    }
}
