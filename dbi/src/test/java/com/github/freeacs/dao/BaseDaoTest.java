package com.github.freeacs.dao;

import org.jdbi.v3.core.Jdbi;
import org.junit.After;
import org.junit.Before;

public class BaseDaoTest {
    private String jdbcUrl = "jdbc:h2:mem:testdb;MODE=MYSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
    private String user = "sa";
    private String password = "";
    protected Jdbi jdbi;

    @Before
    public void init() {
        String schema = convertStreamToString(this.getClass().getClassLoader().getResourceAsStream("schema.sql"));
        jdbi = Jdbi.create(jdbcUrl, user, password).installPlugins();
        jdbi.withHandle(handle -> handle.createScript(schema).execute());
    }

    @After
    public void destroy() {
        jdbi.withHandle(handle -> handle.createUpdate("DROP ALL OBJECTS").execute());
    }

    private static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
