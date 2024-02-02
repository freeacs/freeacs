package com.github.freeacs.tr069.methods;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ProvisioningMethodTest {

    @Test
    public void nullString() {
        // Given:
        String xml = null;

        // When:
        String methodName = ProvisioningMethod.fromString(xml).name();

        // Then:
        assertNotNull(methodName);
        assertEquals("Empty", methodName);
    }

    @Test
    public void validString() {
        // Given:
        String xml = "GetRPCMethods";

        // When:
        String methodName = ProvisioningMethod.fromString(xml).name();

        // Then:
        assertNotNull(methodName);
        assertEquals("GetRPCMethods", methodName);
    }

    @Test
    public void unknownString() {
        // Given:
        String xml = "FooBar";

        // When:
        String methodName = ProvisioningMethod.fromString(xml).name();

        // Then:
        assertNotNull(methodName);
        assertEquals("Empty", methodName);
    }

}
