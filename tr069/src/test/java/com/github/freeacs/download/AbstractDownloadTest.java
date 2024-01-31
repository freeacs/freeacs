package com.github.freeacs.download;

import com.github.freeacs.common.util.AbstractMySqlIntegrationTest;
import com.github.freeacs.dbi.*;
import com.github.freeacs.provisioning.AbstractProvisioningTest;
import com.github.freeacs.utils.MysqlDataSourceInitializer;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

@ContextConfiguration(initializers = AbstractDownloadTest.DataSourceInitializer.class)
@SuppressWarnings("WeakerAccess")
public abstract class AbstractDownloadTest implements AbstractMySqlIntegrationTest {
    static final byte[] FILE_BYTES = new byte[]{3,6,1};
    static final String FILE_VERSION = "1.23.1";

    public static class DataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
            MysqlDataSourceInitializer.initialize(mysql, applicationContext);
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

