package com.github.freeacs.download;

import com.github.freeacs.dbi.*;
import com.github.freeacs.provisioning.AbstractProvisioningTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

import static com.github.freeacs.provisioning.AbstractProvisioningTest.UNIT_TYPE_NAME;

@SuppressWarnings("WeakerAccess")
public abstract class AbstractDownloadTest {
    static final byte[] FILE_BYTES = new byte[]{3,6,1};
    static final String FILE_VERSION = "1.23.1";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected DBI dbi;

    protected void addTestfile() throws SQLException {
        AbstractProvisioningTest.addUnitsToProvision(dbi);
        Unittype unittype = dbi.getAcs().getUnittype(UNIT_TYPE_NAME);
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

