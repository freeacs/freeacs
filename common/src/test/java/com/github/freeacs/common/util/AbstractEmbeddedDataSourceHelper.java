package com.github.freeacs.common.util;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.UUID;

public abstract class AbstractEmbeddedDataSourceHelper {
    private static String randomFolder;

    protected static HikariDataSource dataSource;

    private static DB db;

    public static void setUpBeforeClass() throws ManagedProcessException {
        randomFolder = "./db/" + UUID.randomUUID();
        DBConfigurationBuilder configBuilder = DBConfigurationBuilder.newBuilder();
        configBuilder.setPort(3307);
        configBuilder.setDataDir(randomFolder);
        db = DB.newEmbeddedDB(configBuilder.build());
        db.start();
        db.createDB("acs", "acs", "acs");
        db.source("install.sql", "acs", "acs", "acs");
        String url = configBuilder.getURL("acs");
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDataSourceClassName("org.mariadb.jdbc.MariaDbDataSource");
        hikariConfig.addDataSourceProperty("url", url);
        hikariConfig.addDataSourceProperty("user", "acs");
        hikariConfig.addDataSourceProperty("password", "acs");
        dataSource = new HikariDataSource(hikariConfig);
    }

    public static void tearDownAfterClass() throws Exception {
        dataSource.unwrap(HikariDataSource.class).close();
        db.stop();
        FileUtils.deleteDirectory(new File(randomFolder));
    }
}
