package com.github.freeacs.getrpcmethods;

import com.github.freeacs.common.util.DBScriptUtility;
import com.github.freeacs.dbi.DBI;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.mariadb.jdbc.MariaDbDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;

import static com.github.freeacs.common.util.FileSlurper.getFileAsString;
import static com.github.freeacs.utils.Matchers.hasNoSpace;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(initializers = BasicGetRPCMethodsTest.DataSourceInitializer.class)
@Testcontainers
public abstract class AbstractGetRPCMethodsTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:5.7.34");

    static MariaDbDataSource dataSource;

    @BeforeAll
    static void beforeAll() throws Exception {
        mysql.start();
        dataSource = new MariaDbDataSource();
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

    public static void getRPCMethods(@Nullable RequestPostProcessor authPostProcessor, MockMvc mvc) throws Exception {
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
                .session(session)
                .content(getFileAsString("/provision/cpe/GetRPCMethods.xml")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/xml"))
                .andExpect(header().string("SOAPAction", ""))
                .andExpect(xpath("/*[local-name() = 'Envelope']" +
                        "/*[local-name() = 'Body']" +
                        "/*[local-name() = 'GetRPCMethodsResponse']" +
                        "/MethodList" +
                        "/string[1]")
                        .string("Inform"))
                .andExpect(xpath("/*[local-name() = 'Envelope']" +
                        "/*[local-name() = 'Body']" +
                        "/*[local-name() = 'GetRPCMethodsResponse']" +
                        "/MethodList" +
                        "/string[2]")
                        .string("GetRPCMethods"))
                .andExpect(xpath("/*[local-name() = 'Envelope']" +
                        "/*[local-name() = 'Body']" +
                        "/*[local-name() = 'GetRPCMethodsResponse']" +
                        "/MethodList" +
                        "/string[3]")
                        .string("TransferComplete"))
                .andExpect(xpath("/*[local-name() = 'Envelope']" +
                        "/*[local-name() = 'Body']" +
                        "/*[local-name() = 'GetRPCMethodsResponse']" +
                        "/MethodList" +
                        "/string[4]")
                        .string("AutonomousTransferComplete"));
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
    }
}
