package com.github.freeacs.web.app.page.unit;

import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.util.SystemParameters;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UnitPageTest {

    @Test
    public void isNotNatDetectedWhenConnRewUrlContainsPublicUP() {
        // Given:
        String publicIPAddress = "192.168.0.8";
        String connectionRequestUrl = "http://192.168.0.8:7547";
        Unit mockUnit = mock(Unit.class);
        when(mockUnit.getParameterValue(SystemParameters.IP_ADDRESS, false)).thenReturn(publicIPAddress);
        when(mockUnit.getParameterValue("InternetGatewayDevice.ManagementServer.ConnectionRequestURL", false)).thenReturn(connectionRequestUrl);

        // When:
        boolean isNat = UnitPage.isNatDetected(mockUnit);

        // Then:
        assertFalse(isNat);
    }

    @Test
    public void isNotNatDetected() {
        // Given:
        String publicIPAddress = "10.0.2.1";
        String connectionRequestUrl = "http://192.168.0.8:7547";
        Unit mockUnit = mock(Unit.class);
        when(mockUnit.getParameterValue(SystemParameters.IP_ADDRESS, false)).thenReturn(publicIPAddress);
        when(mockUnit.getParameterValue("InternetGatewayDevice.ManagementServer.ConnectionRequestURL", false)).thenReturn(connectionRequestUrl);

        // When:
        boolean isNat = UnitPage.isNatDetected(mockUnit);

        // Then:
        assertTrue(isNat);
    }
}
