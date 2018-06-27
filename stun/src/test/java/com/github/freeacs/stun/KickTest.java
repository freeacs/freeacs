package com.github.freeacs.stun;

import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.UnitParameter;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.dbi.UnittypeParameterFlag;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KickTest {

    private boolean checkPublicIp;

    @Before
    public void init() {
        checkPublicIp = Properties.CHECK_PUBLIC_IP;
    }

    @After
    public void clean() {
        Properties.CHECK_PUBLIC_IP = checkPublicIp;
    }

    @Test
    public void testPublicIpCheckEnabled() throws MalformedURLException {
        // When:
        Properties.CHECK_PUBLIC_IP = true;
        String ip = "http://192.168.0.1";

        // When:
        boolean isPublic = new Kick().checkIfPublicIP(ip);

        // Then:
        assertFalse(isPublic);
    }

    @Test
    public void testPublicIpCheckDisabled() throws MalformedURLException {
        // When:
        Properties.CHECK_PUBLIC_IP = false;
        String ip = "http://192.168.0.1";

        // When:
        boolean isPublic = new Kick().checkIfPublicIP(ip);

        // Then:
        assertTrue(isPublic);
    }

    public UnitParameter createUnitParameter(String unitId, String key, String value) {
        Unittype ut = new Unittype("Some", "Some", "Some", Unittype.ProvisioningProtocol.TR069);
        UnittypeParameter utp = new UnittypeParameter(ut, key, new UnittypeParameterFlag("RW"));
        Profile profile = new Profile("Default", ut);
        return new UnitParameter(utp, unitId, value, profile);
    }

    @Test
    public void checkThatKickTriesToKickifPublicIpCheckIsDisabled() throws MalformedURLException, SQLException {
        // Given:
        Properties.CHECK_PUBLIC_IP = false;
        Unit unit = new Unit("unitId");
        Map<String, UnitParameter> parameters = new HashMap<>();
        parameters.put("InternetGatewayDevice.ManagementServer.ConnectionRequestURL",
                createUnitParameter(
                        "unitId",
                        "InternetGatewayDevice.ManagementServer.ConnectionRequestURL",
                        "http://localhost:8080/connect"));
        unit.setUnitParameters(parameters);
        unit.setParamsAvailable(true);
        Kick kick = mock(Kick.class);
        when(kick.kickInternal(any(Unit.class))).thenCallRealMethod();
        when(kick.checkIfPublicIP(anyString())).thenCallRealMethod();
        when(kick.kickUsingTCP(any(Unit.class), anyString(), any(), any()))
                .thenReturn(new Kick.KickResponse(
                        true,
                        "TCP/HTTP-kick to http://localhost:8080/connect " +
                                "got HTTP response code 200, indicating success"));

        // When:
        Kick.KickResponse kr = kick.kickInternal(unit);

        // Then:
        assertNotNull(kr);
        assertTrue(kr.isKicked());
    }
}
