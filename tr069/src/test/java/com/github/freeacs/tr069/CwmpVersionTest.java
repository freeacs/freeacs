package com.github.freeacs.tr069;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CwmpVersionTest {

    @Test
    public void extractValidVersion3() {
        // Given:
        String xml = "<SOAP-ENV:Envelope xmlns:cwmp=\"urn:dslforum-org:cwmp-1-3\"></SOAP-ENV:Envelope>";

        // When:
        String version = CwmpVersion.extractVersionFrom(xml);

        // Then:
        assertNotNull(version);
        assertEquals("1-3", version);
    }

    @Test
    public void extractValidVersion1() {
        // Given:
        String xml = "<SOAP-ENV:Envelope xmlns:cwmp=\"urn:dslforum-org:cwmp-1-1\"></SOAP-ENV:Envelope>";

        // When:
        String version = CwmpVersion.extractVersionFrom(xml);

        // Then:
        assertNotNull(version);
        assertEquals("1-1", version);
    }

    @Test
    public void fallBackTo2IfNotSupported() {
        // Given:
        String xml = "<SOAP-ENV:Envelope xmlns:cwmp=\"urn:dslforum-org:cwmp-1-9\"></SOAP-ENV:Envelope>";

        // When:
        String version = CwmpVersion.extractVersionFrom(xml);

        // Then:
        assertNotNull(version);
        assertEquals("1-2", version);
    }
}
