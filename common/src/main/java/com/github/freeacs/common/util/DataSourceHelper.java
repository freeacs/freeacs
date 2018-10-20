package com.github.freeacs.common.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DataSourceHelper {

  public static javax.sql.DataSource inMemoryDataSource() {
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
    hikariConfig.setConnectionTestQuery("VALUES 1");
    hikariConfig.addDataSourceProperty(
        "URL", "jdbc:h2:mem:testdb;MODE=MYSQL;INIT=RUNSCRIPT FROM 'classpath:h2-schema.sql';");
    hikariConfig.addDataSourceProperty("user", "sa");
    hikariConfig.addDataSourceProperty("password", "sa");
    return new HikariDataSource(hikariConfig);
  }
}
