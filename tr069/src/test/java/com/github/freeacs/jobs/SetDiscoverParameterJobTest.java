package com.github.freeacs.jobs;

import com.github.freeacs.Main;
import com.github.freeacs.common.util.FileSlurper;
import com.github.freeacs.core.Properties;
import com.github.freeacs.core.task.ScriptExecutor;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.File;
import com.github.freeacs.dbi.FileType;
import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.Job;
import com.github.freeacs.dbi.JobFlag;
import com.github.freeacs.dbi.JobStatus;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.provisioning.AbstractProvisioningTest;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

import static com.github.freeacs.provisioning.AbstractProvisioningTest.*;
import static org.junit.Assert.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = Main.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = {
        "classpath:application.properties",
        "classpath:application-h2-datasource.properties",
        "classpath:application-basic-security.properties",
        "classpath:application-discovery-off.properties"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EnableScheduling
public class SetDiscoverParameterJobTest {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    private DBI dbi;

    // This comes from the core module, and is imported with scope test in maven dependencies
    private ScriptExecutor scriptExecutorTask;

    @Scheduled(cron = "* * * ? * *")
    public void scheduleScriptExecution() {
        scriptExecutorTask.setThisLaunchTms(System.currentTimeMillis());
        scriptExecutorTask.run();
    }

    @Before
    public void init() throws SQLException, IOException {
        // We initialize the ScriptExecutor task from the core module
        Config config = ConfigFactory.empty()
                .withValue("shellscript.poolsize", ConfigValueFactory.fromAnyRef(1))
                .withValue("syslog.severity.0.limit", ConfigValueFactory.fromAnyRef(90));
        scriptExecutorTask = new ScriptExecutor("ScriptExecutor", dbi, new Properties(config));
        // Create necessary state
        AbstractProvisioningTest.addUnitsToProvision(dbi);
        Unittype unittype = dbi.getAcs().getUnittype(UNIT_TYPE_NAME);
        Profile profile = unittype.getProfiles().getByName("Default");
        Group group = new Group("Test", "Test", null, unittype, profile);
        unittype.getGroups().addOrChangeGroup(group, dbi.getAcs());
        File file = new File();
        file.setBytes(FileSlurper.getFileAsString("/shellscripts/setdiscoverparameter.fss").getBytes());
        file.setDescription("test");
        file.setType(FileType.SHELL_SCRIPT);
        file.setVersion("1.0");
        file.setUnittype(unittype);
        file.setOwner(dbi.getAcs().getUser());
        file.setTimestamp(Date.valueOf(LocalDate.now()));
        file.setName("Shell script");
        unittype.getFiles().addOrChangeFile(file, dbi.getAcs());
        Job job = new Job();
        job.setDescription("Test");
        job.setStopRules("n1");
        job.setUnittype(unittype);
        job.setGroup(group);
        job.setFlags(new JobFlag(JobFlag.JobType.SHELL, JobFlag.JobServiceWindow.DISRUPTIVE));
        job.setFile(file);
        job.setUnconfirmedTimeout(60);
        job.setRepeatCount(1);
        unittype.getJobs().add(job, dbi.getAcs());
        job.setStatus(JobStatus.STARTED);
        unittype.getJobs().changeStatus(job, dbi.getAcs());
        ACSUnit acsUnit = dbi.getACSUnit();
        acsUnit.addOrChangeUnitParameter(acsUnit.getUnitById(UNIT_ID), SystemParameters.JOB_CURRENT, job.getId().toString());
    }

    @Test
    public void setDiscoverParameterViaJob() throws Exception {
        AbstractProvisioningTest.provisionUnit(httpBasic(UNIT_ID, UNIT_PASSWORD), mvc);
        ACSUnit acsUnit = dbi.getACSUnit();
        String discoverValue = acsUnit.getUnitById(UNIT_ID).getUnitParameters().get(SystemParameters.DISCOVER).getValue();
        assertEquals("valueToExpectInTest", discoverValue);
    }
}
