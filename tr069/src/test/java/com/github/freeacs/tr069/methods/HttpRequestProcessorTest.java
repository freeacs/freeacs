package com.owera.xaps.tr069.methods;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HttpRequestProcessorTest {

    @Test
    public void testNormalResponse() {
        // Given:
        String rebootResponse = "<soap:Body><cwmp:RebootResponse><Irreleevant /></cwmp:RebootResponse></soap:Body>";

        // When:
        String methodName = HTTPRequestProcessor.extractMethodName(rebootResponse);

        // Then:
        assertEquals("Reboot", methodName);
    }

    @Test
    public void testClosedElement() {
        // Given:
        String rebootResponse = "<soap:Body><cwmp:RebootResponse/></soap:Body>";

        // When:
        String methodName = HTTPRequestProcessor.extractMethodName(rebootResponse);

        // Then:
        assertEquals("Reboot", methodName);
    }
}