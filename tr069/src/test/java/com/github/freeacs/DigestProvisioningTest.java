package com.github.freeacs;

import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.Unittypes;
import com.github.freeacs.dbi.util.SystemParameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.SQLException;
import java.util.Collections;

import static com.github.freeacs.Matchers.hasNoSpace;
import static com.github.freeacs.common.util.FileSlurper.getFileAsString;
import static com.github.freeacs.dbi.Unittype.ProvisioningProtocol.TR069;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.digest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = Main.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = {
        "classpath:application.properties",
        "classpath:application-digest-security.properties",
        "classpath:application-discovery-mode.properties"
})
public class DigestProvisioningTest {
    private static final String UNIT_ID = "test123";
    private static final String UNIT_TYPE_NAME = "Test";
    private static final String UNIT_PASSWORD = "password";
    private static final String PROFILE_NAME = "Default";
    private static final String DIGEST_REALM = "FreeACS";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private DBI dbi;

    @Before
    public void init() throws SQLException {
        ACSUnit acsUnit = new ACSUnit(dbi.getDataSource(), dbi.getAcs(), dbi.getSyslog());
        Unittypes unittypes = dbi.getAcs().getUnittypes();
        Unittype unittype = unittypes.getByName(UNIT_TYPE_NAME);
        if (unittype != null) {
            Unit unit = acsUnit.getUnitById(UNIT_ID);
            if (unit != null) {
                acsUnit.deleteUnit(unit);
            }
            unittypes.deleteUnittype(unittype, dbi.getAcs(), true);
        }
        unittypes.addOrChangeUnittype(new Unittype(UNIT_TYPE_NAME, "","", TR069), dbi.getAcs());
        unittype = unittypes.getByName(UNIT_TYPE_NAME);
        acsUnit.addUnits(Collections.singletonList(UNIT_ID), unittype.getProfiles().getByName(PROFILE_NAME));
        Unit unit = acsUnit.getUnitById(UNIT_ID);
        acsUnit.addOrChangeUnitParameter(unit, SystemParameters.SECRET, UNIT_PASSWORD);
    }

    @Test
    public void getUnauthorizedOnMissingAuthentication() throws Exception {
        mvc.perform(post("/tr069")).andExpect(status().isUnauthorized());
    }

    @Test
    public void discoverUnit() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/tr069")
                .session(session)
                .with(digest(UNIT_ID).password(UNIT_PASSWORD).realm(DIGEST_REALM))
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
                        "/*[local-name() = 'GetParameterNames']" +
                        "/ParameterPath")
                        .string("InternetGatewayDevice."))
                .andExpect(xpath("/*[local-name() = 'Envelope']" +
                        "/*[local-name() = 'Body']" +
                        "/*[local-name() = 'GetParameterNames']" +
                        "/NextLevel")
                        .string("false"));
        mvc.perform(post("/tr069")
                .session(session)
                .content(getFileAsString("/provision/cpe/GetParameterNamesResponse.xml")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/xml"))
                .andExpect(header().string("SOAPAction", ""))
                .andExpect(xpath("/*[local-name() = 'Envelope']" +
                        "/*[local-name() = 'Body']" +
                        "/*[local-name() = 'GetParameterValues']" +
                        "/ParameterNames" +
                        "/string[1]")
                        .string("InternetGatewayDevice.DeviceInfo.SoftwareVersion"))
                .andExpect(xpath("/*[local-name() = 'Envelope']" +
                        "/*[local-name() = 'Body']" +
                        "/*[local-name() = 'GetParameterValues']" +
                        "/ParameterNames" +
                        "/string[2]")
                        .string("InternetGatewayDevice.DeviceInfo.VendorConfigFile."));
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
