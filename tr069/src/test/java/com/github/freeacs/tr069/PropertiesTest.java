package com.github.freeacs.tr069;

import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.tr069.xml.ParameterValueStruct;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PropertiesTest {

    @Test
    public void isParameterQuirkWithVersion() {
        // Given:
        SessionData sessionData = mock(SessionData.class);
        Unittype unittype = new Unittype("SpeedTouch 780", "na", "na", Unittype.ProvisioningProtocol.TR069);
        when(sessionData.getUnittype()).thenReturn(unittype);
        CPEParameters cpeParameters = getCpeParameters();
        when(sessionData.getCpeParameters()).thenReturn(cpeParameters);

        // When:
        boolean isParameterKeyQuirk = Properties.isParameterkeyQuirk(sessionData);

        // Then:
        assertTrue(isParameterKeyQuirk);
    }

    @Test
    public void isParameterQuirkwithoutVersion() {
        // Given:
        SessionData sessionData = mock(SessionData.class);
        Unittype unittype = new Unittype("P-2602HW-F3", "na", "na", Unittype.ProvisioningProtocol.TR069);
        when(sessionData.getUnittype()).thenReturn(unittype);
        CPEParameters cpeParameters = getCpeParameters();
        when(sessionData.getCpeParameters()).thenReturn(cpeParameters);

        // When:
        boolean isParameterKeyQuirk = Properties.isParameterkeyQuirk(sessionData);

        // Then:
        assertTrue(isParameterKeyQuirk);
    }

    @Test
    public void isNotParameterQuirk() {
        // Given:
        SessionData sessionData = mock(SessionData.class);
        Unittype unittype = new Unittype("freecwmp", "na", "na", Unittype.ProvisioningProtocol.TR069);
        when(sessionData.getUnittype()).thenReturn(unittype);
        CPEParameters cpeParameters = getCpeParameters();
        when(sessionData.getCpeParameters()).thenReturn(cpeParameters);

        // When:
        boolean isParameterKeyQuirk = Properties.isParameterkeyQuirk(sessionData);

        // Then:
        assertFalse(isParameterKeyQuirk);
    }

    private CPEParameters getCpeParameters() {
        CPEParameters cpeParameters = new CPEParameters("KeyRoot.");
        ParameterValueStruct struct = new ParameterValueStruct();
        struct.setName("KeyRoot.DeviceInfo.SoftwareVersion");
        struct.setValue("6.2.29.2");
        cpeParameters.getCpeParams().put("KeyRoot.DeviceInfo.SoftwareVersion", struct);
        return cpeParameters;
    }
}
