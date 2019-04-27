package com.github.freeacs;

import com.github.freeacs.dbi.*;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.tr069.base.BaseCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.sql.SQLException;
import java.util.Collections;

import static com.github.freeacs.Matchers.hasNoSpace;
import static com.github.freeacs.common.util.FileSlurper.getFileAsString;
import static com.github.freeacs.dbi.Unittype.ProvisioningProtocol.TR069;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class AbstractBaseTest {
    protected static final String UNIT_ID = "test123";
    protected static final String UNIT_TYPE_NAME = "Test";
    protected static final String UNIT_PASSWORD = "password";
    protected static final String PROFILE_NAME = "Default";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected DBI dbi;

    protected void addNonProvisionedUnit() throws SQLException {
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

    protected void discoverUnit(@Nullable RequestPostProcessor authPostProcessor) throws Exception {
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
