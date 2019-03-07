package com.github.freeacs.stun;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.UnitParameter;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.dbi.UnittypeParameterFlag;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class KickTest {

  @Test
  public void testPublicIpCheckEnabled() throws MalformedURLException {
    // When:
    Properties properties =
        new Properties(
            ConfigFactory.empty()
                .withValue("kick.check-public-ip", ConfigValueFactory.fromAnyRef(true)));
    String ip = "http://192.168.0.1";
    // When:
    boolean isPublic = new Kick().checkIfPublicIP(ip, properties);
    // Then:
    assertFalse(isPublic);
  }

  @Test
  public void testPublicIpCheckDisabled() throws MalformedURLException {
    // When:
    Properties properties =
        new Properties(
            ConfigFactory.empty()
                .withValue("kick.check-public-ip", ConfigValueFactory.fromAnyRef(false)));
    String ip = "http://192.168.0.1";
    // When:
    boolean isPublic = new Kick().checkIfPublicIP(ip, properties);
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
  public void checkThatKickTriesToKickifPublicIpCheckIsDisabled() throws MalformedURLException {
    // Given:
    Properties properties =
        new Properties(
            ConfigFactory.empty()
                .withValue("kick.check-public-ip", ConfigValueFactory.fromAnyRef(false)));
    Unit unit = new Unit("unitId");
    Map<String, UnitParameter> parameters = new HashMap<>();
    parameters.put(
        "InternetGatewayDevice.ManagementServer.ConnectionRequestURL",
        createUnitParameter(
            "unitId",
            "InternetGatewayDevice.ManagementServer.ConnectionRequestURL",
            "http://localhost:8080/connect"));
    unit.setUnitParameters(parameters);
    unit.setParamsAvailable(true);
    Kick kick = mock(Kick.class);
    when(kick.kickInternal(any(Unit.class), any(Properties.class))).thenCallRealMethod();
    when(kick.checkIfPublicIP(anyString(), any(Properties.class))).thenCallRealMethod();
    when(kick.kickUsingTCP(any(Unit.class), anyString(), any(), any()))
        .thenReturn(
            new Kick.KickResponse(
                true,
                "TCP/HTTP-kick to http://localhost:8080/connect "
                    + "got HTTP response code 200, indicating success"));
    // When:
    Kick.KickResponse kr = kick.kickInternal(unit, properties);
    // Then:
    assertNotNull(kr);
    assertTrue(kr.isKicked());
  }
}
