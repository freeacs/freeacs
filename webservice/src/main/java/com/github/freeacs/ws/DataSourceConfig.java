package com.github.freeacs.ws;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DataSourceConfig {
  @Bean
  @Primary
  @Qualifier("main")
  @ConfigurationProperties("main.datasource")
  public DataSource mainDs() {
    return DataSourceBuilder.create().type(HikariDataSource.class).build();
  }
}
