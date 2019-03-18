package com.github.freeacs.common.spark;

import com.github.freeacs.common.hikari.HikariDataSourceHelper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import spark.Spark;

import javax.sql.DataSource;

public abstract class SparkApp {

    protected final Config config;
    protected final DataSource datasource;

    public SparkApp() {
        config = ConfigFactory.load();
        Spark.port(config.getInt("server.port"));
        datasource = HikariDataSourceHelper.dataSource(config.getConfig("main"));
    }
}
