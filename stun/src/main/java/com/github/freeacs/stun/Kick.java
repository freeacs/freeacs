package com.github.freeacs.stun;

import com.github.freeacs.common.util.IPAddress;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.crypto.Crypto;
import com.github.freeacs.dbi.util.SystemParameters;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.params.CoreConnectionPNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Kick {
  public static class KickResponse {
    private boolean kicked;
    private String message;

    public KickResponse(boolean kicked, String message) {
      this.kicked = kicked;
      this.message = message;
    }

    public boolean isKicked() {
      return kicked;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }
  }

  private static final Kick kickSingleton = new Kick();

  public static KickResponse kick(Unit unit, Properties properties)
      throws MalformedURLException, SQLException {
    return kickSingleton.kickInternal(unit, properties);
  }

  private static Logger log = LoggerFactory.getLogger("KickSingle");
  private static Random random = new Random();

  protected KickResponse kickInternal(Unit unit, Properties properties)
      throws MalformedURLException {
    CPEParameters cpeParams = new CPEParameters(getKeyroot(unit));
    String udpCrUrl = unit.getParameterValue(cpeParams.UDP_CONNECTION_URL, false);
    String crUrl = unit.getParameterValue(cpeParams.CONNECTION_URL, false);
    String crUser = unit.getParameterValue(cpeParams.CONNECTION_USERNAME);
    String crPass = unit.getParameterValue(cpeParams.CONNECTION_PASSWORD);
    if (crPass == null) {
      crPass = crUser;
    }
    String publicIP = unit.getParameterValue(SystemParameters.IP_ADDRESS);
    String publicProtocol = unit.getParameterValue(SystemParameters.PROTOCOL);
    Integer publicPort = getPublicPort(unit);

    // default response
    KickResponse kr =
        new KickResponse(
            false,
            "Neither a public ConnectionRequestURL nor any UDPConnectionRequestAddress was found");

    // TCP-kick (HTTP)
    if (crUrl != null && !"".equals(crUrl.trim()) && checkIfPublicIP(crUrl, properties)) {
      log.debug(unit.getId() + ": will try TCP kick on " + crUrl);
      return kickUsingTCP(unit, crUrl, crPass, crUser);
    }

    // UDP-kick
    if (!kr.isKicked() && udpCrUrl != null && !"".equals(udpCrUrl.trim())) {
      log.debug(unit.getId() + ": will try UDP kick on " + udpCrUrl);
      return kickUsingUDP(unit, udpCrUrl, crPass, crUser);
    }

    // TCP-kick with port forwarding
    if (properties.isExpectPortForwarding() && publicIP != null && crUrl != null) {
      String newCrUrl = crUrl.replace(new URL(crUrl).getHost(), publicIP);
      log.debug(
          unit.getId()
              + ": will try TCP kick by expecting port forwarding on "
              + crUrl
              + " -> "
              + newCrUrl);
      return kickUsingTCP(unit, newCrUrl, crPass, crUser);
    }

    // Dynamic TCP-kick
    if (crUrl == null && publicIP != null && publicProtocol != null && publicPort != null) {
      crUrl = String.format("%s://%s:%d", publicProtocol, publicIP, publicPort);
      log.debug(unit.getId() + ": will try dynamic TCP kick on " + crUrl);
      return kickUsingTCP(unit, crUrl, crPass, crUser);
    }

    return kr;
  }

  private Integer getPublicPort(Unit unit) {
    try {
      return Integer.parseInt(unit.getParameterValue(SystemParameters.PORT));
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  /**
   * Check if IP is public only if its configured. If its not configured, this method returns true
   * always.
   *
   * @param crUrl The ip to check
   * @param properties props
   * @return a boolean if configured to check if ip is public, otherwise always true
   * @throws MalformedURLException if the ip is malformed
   */
  boolean checkIfPublicIP(String crUrl, Properties properties) throws MalformedURLException {
    return !properties.isCheckPublicIp() || IPAddress.isPublic(new URL(crUrl).getHost());
  }

  protected KickResponse kickUsingTCP(Unit unit, String crUrl, String crPass, String crUser)
      throws MalformedURLException {
    DefaultHttpClient client = new DefaultHttpClient();
    HttpGet get = new HttpGet(crUrl);
    get.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);
    get.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
    client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, true));
    int statusCode;
    if (crUser != null && crPass != null) {
      get = authenticate(client, get, crUrl, crUser, crPass);
      log.debug(
          unit.getId()
              + " had a password and username, hence the kick will be executed with authentication (digest/basic)");
    }
    try {
      HttpResponse response = client.execute(get);
      statusCode = response.getStatusLine().getStatusCode();
    } catch (ConnectTimeoutException ce) {
      log.warn(unit.getId() + " did not respond, indicating a NAT problem or disconnected.");
      return new KickResponse(
          false,
          "TCP/HTTP-kick to "
              + crUrl
              + " failed, probably due to NAT or other connection problems: "
              + ce.getMessage());
    } catch (Throwable t) {
      log.warn(unit.getId() + " did not respond, an error has occured: ", t);
      return new KickResponse(
          false,
          "TCP/HTTP-kick to "
              + crUrl
              + " failed because of an unexpected error: "
              + t.getMessage());
    }
    if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_NO_CONTENT) {
      log.debug(
          unit.getId() + " responded with HTTP " + statusCode + ", indicating a successful kick");
      return new KickResponse(
          true,
          "TCP/HTTP-kick to "
              + crUrl
              + " got HTTP response code "
              + statusCode
              + ", indicating success");
    } else {
      log.warn(
          unit.getId() + " responded with HTTP " + statusCode + ", indicating a unsuccessful kick");
      if (statusCode == HttpStatus.SC_FORBIDDEN || statusCode == HttpStatus.SC_UNAUTHORIZED) {
        return new KickResponse(
            false,
            "TCP/HTTP-kick to "
                + crUrl
                + " (user:"
                + crUser
                + ",pass:"
                + crPass
                + ") failed, probably due to wrong user/pass since HTTP response code is "
                + statusCode);
      } else {
        return new KickResponse(
            false, "TCP/HTTP-kick to " + crUrl + " failed with HTTP response code " + statusCode);
      }
    }
  }

  private KickResponse kickUsingUDP(Unit unit, String udpCrUrl, String crPass, String crUser) {
    try {
      String id = String.valueOf(random.nextInt(100000));
      String cn = String.valueOf(random.nextLong());
      String ts = String.valueOf(System.currentTimeMillis());
      String text = ts + id + crUser + cn;
      String passFix = crPass == null ? "password" : crPass;
      String sig = Crypto.computeHmacSHA1AsHexUpperCase(passFix, text);
      String req =
          "GET http://"
              + udpCrUrl
              + "/?ts="
              + ts
              + "&id="
              + id
              + "&un="
              + crUser
              + "&cn="
              + cn
              + "&sig="
              + sig
              + " HTTP/1.1\r\n\r\n";
      byte[] buf = req.getBytes();
      if (!udpCrUrl.contains(":")) {
        udpCrUrl += ":80";
      }
      InetAddress address = InetAddress.getByName(udpCrUrl.split(":")[0]);
      int port = Integer.parseInt(udpCrUrl.split(":")[1]);
      DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
      for (int i = 0; i < 3; i++) {
        MessageStack.push(packet);
      }
      log.debug(unit.getId() + " has been kicked using UDP (TR-111) method to " + udpCrUrl);
      return new KickResponse(true, "UDP kick to " + udpCrUrl + " was initiated");
    } catch (Throwable t) {
      log.error(unit.getId() + " UDP kick to " + udpCrUrl + " failed", t);
      return new KickResponse(false, "UDP kick to " + udpCrUrl + " failed: " + t.getMessage());
    }
  }

  /** KICK RELATED METHODS. */
  private String getKeyroot(Unit u) {
    for (String paramName : u.getParameters().keySet()) {
      if (paramName.startsWith("Device.")) {
        return "Device.";
      }
      if (paramName.startsWith("InternetGatewayDevice.")) {
        return "InternetGatewayDevice.";
      }
    }
    throw new RuntimeException(
        "No keyroot found for unit " + u.getId() + ", probably because no parameters are defined");
  }

  private HttpGet authenticate(
      DefaultHttpClient client, HttpGet get, String urlStr, String username, String password)
      throws MalformedURLException {
    URL url = new URL(urlStr);
    List<String> authPrefs = new ArrayList<String>(2);
    authPrefs.add(AuthPolicy.DIGEST);
    authPrefs.add(AuthPolicy.BASIC);
    client.getParams().setParameter(AuthPNames.PROXY_AUTH_PREF, authPrefs);
    client.getParams().setParameter(AuthPNames.TARGET_AUTH_PREF, authPrefs);
    Credentials defaultcreds = new UsernamePasswordCredentials(username, password);
    client
        .getCredentialsProvider()
        .setCredentials(new AuthScope(url.getHost(), url.getPort()), defaultcreds);
    return get;
  }
}
