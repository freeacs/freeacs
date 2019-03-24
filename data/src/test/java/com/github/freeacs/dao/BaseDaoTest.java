package com.github.freeacs.dao;

import org.h2.jdbcx.JdbcDataSource;
import org.jdbi.v3.core.Jdbi;
import org.junit.After;
import org.junit.Before;

import java.sql.SQLException;

public abstract class BaseDaoTest {

    private Jdbi jdbi;
    private JdbcDataSource ds;

    protected UnitTypeDao unitTypeDao;
    protected ProfileDao profileDao;
    protected UnitDao unitDao;
    protected UnitTypeParameterDao unitTypeParameterDao;
    protected ProfileParameterDao profileParameterDao;

    @Before
    public void init() {
        ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test;MODE=MYSQL;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM 'classpath:h2-schema.sql'");
        ds.setUser("sa");
        ds.setPassword("sa");
        jdbi = Jdbi.create(ds).installPlugins();
        unitTypeDao = jdbi.onDemand(UnitTypeDao.class);
        profileDao = jdbi.onDemand(ProfileDao.class);
        unitDao = jdbi.onDemand(UnitDao.class);
        unitTypeParameterDao = jdbi.onDemand(UnitTypeParameterDao.class);
        profileParameterDao = jdbi.onDemand(ProfileParameterDao.class);
    }

    @After
    public void teardown() throws SQLException {
        ds.getConnection().createStatement().executeUpdate("SHUTDOWN");
        ds = null;
        jdbi = null;
    }
}
