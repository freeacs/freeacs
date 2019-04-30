package com.github.freeacs.getrpcmethods;

import com.github.freeacs.dbi.DBI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static com.github.freeacs.common.util.FileSlurper.getFileAsString;
import static com.github.freeacs.utils.Matchers.hasNoSpace;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AbstractGetRPCMethodsTest {

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
