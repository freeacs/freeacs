package com.github.freeacs.tr069.methods;

import org.junit.Test;

import static org.junit.Assert.*;

public class ProvisioningMethodTest {

    @Test
    public void nullXmlReturnsEmpty() {
        // Given:
        String xml = null;

        // When:
        String methodName = ProvisioningMethod.extractMethodFrom(xml).name();

        // Then:
        assertNotNull(methodName);
        assertEquals("Empty", methodName);
    }

    @Test
    public void validSelfClosedMethod() {
        // Given:
        String xml = "<cwmp:GetRPCMethods/>";

        // When:
        String methodName = ProvisioningMethod.extractMethodFrom(xml).name();

        // Then:
        assertNotNull(methodName);
        assertEquals("GetRPCMethods", methodName);
    }

    @Test
    public void validClosedMethod() {
        // Given:
        String xml = "<cwmp:GetRPCMethods></cwmp:GetRPCMethods>";

        // When:
        String methodName = ProvisioningMethod.extractMethodFrom(xml).name();

        // Then:
        assertNotNull(methodName);
        assertEquals("GetRPCMethods", methodName);
    }

    @Test
    public void validUnclosedMethod() {
        // Given:
        String xml = "<cwmp:GetRPCMethods>";

        // When:
        String methodName = ProvisioningMethod.extractMethodFrom(xml).name();

        // Then:
        assertNotNull(methodName);
        assertEquals("GetRPCMethods", methodName);
    }

    @Test
    public void emptyWhenNotSupported() {
        // Given:
        String xml = "<cwmp:YoMama/>";

        // When:
        String methodName = ProvisioningMethod.extractMethodFrom(xml).name();

        // Then:
        assertNotNull(methodName);
        assertEquals("Empty", methodName);
    }

    @Test
    public void validResponseMethod() {
        // Given:
        String xml = "<cwmp:GetParameterValuesResponse>";

        // When:
        String methodName = ProvisioningMethod.extractMethodFrom(xml).name();

        // Then:
        assertNotNull(methodName);
        assertEquals("GetParameterValues", methodName);
    }

    @Test
    public void otherValidResponseMethod() {
        // Given:
        String xml = "<cwmp:SetParameterValuesResponse>";

        // When:
        String methodName = ProvisioningMethod.extractMethodFrom(xml).name();

        // Then:
        assertNotNull(methodName);
        assertEquals("SetParameterValues", methodName);
    }

    @Test
    public void validRequestMethod() {
        // Given:
        String xml = "<cwmp:TransferComplete>";

        // When:
        String methodName = ProvisioningMethod.extractMethodFrom(xml).name();

        // Then:
        assertNotNull(methodName);
        assertEquals("TransferComplete", methodName);
    }

    @Test
    public void validInformMethod() {
        // Given:
        String xml = "<cwmp:Inform>";

        // When:
        String methodName = ProvisioningMethod.extractMethodFrom(xml).name();

        // Then:
        assertNotNull(methodName);
        assertEquals("Inform", methodName);
    }

}
