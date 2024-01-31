package com.github.freeacs.download;

import com.github.freeacs.common.util.DBScriptUtility;
import com.github.freeacs.dbi.*;
import com.github.freeacs.provisioning.AbstractProvisioningTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.mariadb.jdbc.MariaDbDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

@Testcontainers
@ContextConfiguration(initializers = AbstractDownloadTest.DataSourceInitializer.class)
@SuppressWarnings("WeakerAccess")
public abstract class AbstractDownloadTest {
    static final byte[] FILE_BYTES = new byte[]{3,6,1};
    static final String FILE_VERSION = "1.23.1";

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:5.7.34");

    @BeforeAll
    static void beforeAll() throws Exception {
        mysql.start();
        MariaDbDataSource dataSource = new MariaDbDataSource();
        dataSource.setUrl(String.format("jdbc:mariadb://%s:%d/%s", mysql.getHost(), mysql.getFirstMappedPort(), mysql.getDatabaseName()));
        dataSource.setUser(mysql.getUsername());
        dataSource.setPassword(mysql.getPassword());
        Connection connection = dataSource.getConnection();
        DBScriptUtility.runScript("install.sql", connection);
        connection.close();
    }

    @AfterAll
    static void afterAll() {
        mysql.stop();
    }

    public static class DataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "spring.test.database.replace=none", // Tells Spring Boot not to start in-memory db for tests.
                    "main.datasource.jdbcUrl=" + mysql.getJdbcUrl(),
                    "main.datasource.username=" + mysql.getUsername(),
                    "main.datasource.password=" + mysql.getPassword()
            );
        }
    }

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected DBI dbi;

    protected void addTestfile(String unitTypeName, String unitId) throws SQLException {
        AbstractProvisioningTest.addUnitsToProvision(dbi, unitTypeName, unitId);
        Unittype unittype = dbi.getAcs().getUnittype(unitTypeName);
        User admin = dbi.getAcs().getUser();
        Files files = unittype.getFiles();
        File file = new File();
        file.setBytes(FILE_BYTES);
        file.setDescription("testfile");
        file.setType(FileType.SOFTWARE);
        file.setVersion(FILE_VERSION);
        file.setTimestamp(Date.valueOf(LocalDate.now()));
        file.setName("Testfile");
        file.setUnittype(unittype);
        file.setOwner(admin);
        file.setTargetName("Testfile");
        files.addOrChangeFile(file, dbi.getAcs());
    }
}

