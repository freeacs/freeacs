package com.github.freeacs.common.hikari;

import com.typesafe.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

public class HikariDataSourceHelper {
  public static DataSource dataSource(Config config) {
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setDriverClassName(config.getString("datasource.driverClassName"));
    hikariConfig.setJdbcUrl(config.getString("datasource.jdbcUrl"));
    hikariConfig.setUsername(config.getString("datasource.username"));
    hikariConfig.setPassword(config.getString("datasource.password"));

    hikariConfig.setMinimumIdle(config.getInt("datasource.minimum-idle"));
    hikariConfig.setMaximumPoolSize(config.getInt("datasource.maximum-pool-size"));
    hikariConfig.setConnectionTestQuery("SELECT 1");
    hikariConfig.setPoolName(config.getString("datasource.poolName"));

    hikariConfig.addDataSourceProperty("dataSource.cachePrepStmts", "true");
    hikariConfig.addDataSourceProperty("dataSource.prepStmtCacheSize", "250");
    hikariConfig.addDataSourceProperty("dataSource.prepStmtCacheSqlLimit", "2048");
    hikariConfig.addDataSourceProperty("dataSource.useServerPrepStmts", "true");

    hikariConfig.setAutoCommit(true);

    return new HikariDataSource(hikariConfig);
  }
}
