package com.github.freeacs.jobs;

import com.github.freeacs.Main;
import com.github.freeacs.dbi.*;
import com.github.freeacs.provisioning.AbstractProvisioningTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.SQLException;

import static com.github.freeacs.provisioning.AbstractProvisioningTest.UNIT_TYPE_NAME;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = Main.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = {
        "classpath:application.properties",
        "classpath:application-h2-datasource.properties",
        "classpath:application-basic-security.properties"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BasicJobSetSecretToSerialNumberTest {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    private DBI dbi;

    @Test
    public void init() throws SQLException {
        AbstractProvisioningTest.addNonProvisionedUnit(dbi);
        Unittype unittype = dbi.getAcs().getUnittype(UNIT_TYPE_NAME);
        Profile profile = unittype.getProfiles().getByName("Default");
        Group group = new Group("Test", "Test", null, unittype, profile);
        unittype.getGroups().addOrChangeGroup(group, dbi.getAcs());
        Job job = new Job();
        job.setDescription("Test");
        job.setStopRules("n1");
        unittype.getJobs().add(job, dbi.getAcs());
    }
}
