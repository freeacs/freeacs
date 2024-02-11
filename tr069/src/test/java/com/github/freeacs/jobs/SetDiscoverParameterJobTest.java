package com.github.freeacs.jobs;

import com.github.freeacs.Main;
import com.github.freeacs.common.util.AbstractMySqlIntegrationTest;
import com.github.freeacs.common.util.FileSlurper;
import com.github.freeacs.core.Properties;
import com.github.freeacs.core.task.ScriptExecutor;
import com.github.freeacs.dbi.*;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.provisioning.AbstractProvisioningTest;
import com.github.freeacs.utils.MysqlDataSourceInitializer;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

import static com.github.freeacs.common.util.FileSlurper.getFileAsString;
import static com.github.freeacs.provisioning.AbstractProvisioningTest.*;
import static com.github.freeacs.utils.Matchers.hasNoSpace;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = Main.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = {
        "classpath:application.properties",
        "classpath:application-basic-security.properties",
        "classpath:application-discovery-off.properties"
})
@EnableScheduling
@ContextConfiguration(initializers = SetDiscoverParameterJobTest.DataSourceInitializer.class)
public class SetDiscoverParameterJobTest implements AbstractMySqlIntegrationTest {

    public static class DataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
            MysqlDataSourceInitializer.initialize(mysql, applicationContext);
        }
    }

    @Autowired
    protected MockMvc mvc;

    @Autowired
    private DBI dbi;

    private void init() throws SQLException, IOException {
        // Create necessary state
        AbstractProvisioningTest.addUnitsToProvision(dbi, UNIT_TYPE_NAME, UNIT_ID);
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
        job.setName("Test");
        job.setDescription("Test");
        job.setStopRules("n1");
        job.setUnittype(unittype);
        job.setGroup(group);
        job.setFlags(new JobFlag(JobType.SHELL, JobFlag.JobServiceWindow.DISRUPTIVE));
        job.setFile(file);
        job.setUnconfirmedTimeout(60);
        job.setRepeatCount(1);
        unittype.getJobs().add(job, dbi.getAcs());
        job.setStatus(JobStatus.STARTED);
        unittype.getJobs().changeStatus(job, dbi.getAcs());
        ACSUnit acsUnit = dbi.getACSUnit();
        acsUnit.addOrChangeUnitParameter(acsUnit.getUnitById(UNIT_ID), SystemParameters.JOB_CURRENT, job.getId().toString());
    }

    @Scheduled(cron = "* * * ? * *")
    public void setDiscoverParameterViaScript() throws Exception {
        // We initialize the ScriptExecutor task from the core module
        Config config = ConfigFactory.empty()
                .withValue("shellscript.poolsize", ConfigValueFactory.fromAnyRef(1))
                .withValue("syslog.severity.0.limit", ConfigValueFactory.fromAnyRef(90));
        // This comes from the core module, and is imported with scope test in maven dependencies
        ScriptExecutor scriptExecutorTask = new ScriptExecutor("ScriptExecutor", dbi, new Properties(config));
        scriptExecutorTask.setThisLaunchTms(System.currentTimeMillis());
        scriptExecutorTask.runImpl();
    }

    @Test
    public void setDiscoverParameterViaJob() throws Exception {
        init();
        provisionUnit(httpBasic(UNIT_ID, UNIT_PASSWORD));
        ACSUnit acsUnit = dbi.getACSUnit();
        String discoverValue = acsUnit.getUnitById(UNIT_ID).getUnitParameters().get(SystemParameters.DISCOVER).getValue();
        assertEquals("valueToExpectInTest", discoverValue);
    }

    private void provisionUnit(RequestPostProcessor authPostProcessor) throws Exception {
        MockHttpSession session = new MockHttpSession();
        MockHttpServletRequestBuilder postRequestBuilder = post("/tr069").session(session);
        if (authPostProcessor != null) {
            postRequestBuilder = postRequestBuilder.with(authPostProcessor);
        }
        mvc.perform(postRequestBuilder
                        .content(getFileAsString("/provision/cpe/Inform.xml")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/xml"))
                .andExpect(header().string("SOAPAction", ""))
                .andExpect(xpath("/*[local-name() = 'Envelope']" +
                        "/*[local-name() = 'Body']" +
                        "/*[local-name() = 'InformResponse']" +
                        "/MaxEnvelopes")
                        .string("1"));
        mvc.perform(post("/tr069")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/xml"))
                .andExpect(header().string("SOAPAction", ""))
                .andExpect(xpath("/*[local-name() = 'Envelope']" +
                        "/*[local-name() = 'Body']" +
                        "/*[local-name() = 'GetParameterValues']" +
                        "/ParameterNames" +
                        "/string[1]")
                        .string("InternetGatewayDevice.DeviceInfo.VendorConfigFile."))
                .andExpect(xpath("/*[local-name() = 'Envelope']" +
                        "/*[local-name() = 'Body']" +
                        "/*[local-name() = 'GetParameterValues']" +
                        "/ParameterNames" +
                        "/string[2]")
                        .string("InternetGatewayDevice.ManagementServer.PeriodicInformInterval"));
        mvc.perform(post("/tr069")
                        .session(session)
                        .content(getFileAsString("/provision/cpe/GetParameterValuesResponse.xml")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/xml"))
                .andExpect(header().string("SOAPAction", ""))
                .andExpect(xpath("/*[local-name() = 'Envelope']" +
                        "/*[local-name() = 'Body']" +
                        "/*[local-name() = 'SetParameterValues']" +
                        "/ParameterList" +
                        "/ParameterValueStruct" +
                        "/Name")
                        .string("InternetGatewayDevice.ManagementServer.PeriodicInformInterval"))
                .andExpect(xpath("/*[local-name() = 'Envelope']" +
                        "/*[local-name() = 'Body']" +
                        "/*[local-name() = 'SetParameterValues']" +
                        "/ParameterList" +
                        "/ParameterValueStruct" +
                        "/Value")
                        .string(hasNoSpace()));
        mvc.perform(post("/tr069")
                        .session(session)
                        .content(getFileAsString("/provision/cpe/SetParameterValuesResponse.xml")))
                .andExpect(status().isNoContent())
                .andExpect(content().contentType("text/html"))
                .andExpect(header().doesNotExist("SOAPAction"));
        if (authPostProcessor != null) {
            mvc.perform(post("/tr069")
                            .session(session))
                    .andExpect(status().isUnauthorized());
        }
    }
}
