package com.owera.xaps.stun;

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

import com.owera.common.db.NoAvailableConnectionException;
import com.owera.common.log.Logger;
import com.owera.common.util.IPAddress;
import com.owera.xaps.dbi.Unit;
import com.owera.xaps.dbi.XAPSUnit;
import com.owera.xaps.dbi.crypto.Crypto;
import com.owera.xaps.dbi.util.SystemParameters;

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

		public void setKicked(boolean kicked) {
			this.kicked = kicked;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

	}

	private static Logger log = new Logger("KickSingle");
	private static Random random = new Random();

	public static KickResponse kick(Unit unit, XAPSUnit xapsUnit) throws MalformedURLException, SQLException, NoAvailableConnectionException {
		CPEParameters cpeParams = new CPEParameters(getKeyroot(unit));
		String udpCrUrl = unit.getParameterValue(cpeParams.UDP_CONNECTION_URL, false);
		String crUrl = unit.getParameterValue(cpeParams.CONNECTION_URL, false);
		String crPass = unit.getParameterValue(cpeParams.CONNECTION_PASSWORD);
		String crUser = unit.getParameterValue(cpeParams.CONNECTION_USERNAME);
		String publicIP = unit.getParameterValue(SystemParameters.IP_ADDRESS);
		// default response
		KickResponse kr = new KickResponse(false, "Neither a public ConnectionRequestURL nor any UDPConnectionRequestAddress was found");
		// TCP-kick (HTTP)
		if (crUrl != null && !crUrl.trim().equals("") && IPAddress.isPublic(new URL(crUrl).getHost())) {
			log.debug(unit.getId() + " will try TCP kick");
			kr = kickUsingTCP(unit, xapsUnit, crUrl, crPass, crUser);
		}
		// UDP-kick
		if (!kr.isKicked() && udpCrUrl != null && !udpCrUrl.trim().equals("")) {
			log.debug(unit.getId() + " will try UDP kick");
			kr = kickUsingUDP(unit, xapsUnit, udpCrUrl, crUrl, crPass, crUser);
		} else if (Properties.expectPortForwarding() && publicIP != null && crUrl != null) {
			log.debug(unit.getId() + " will try TCP kick by expecting port forwarding");
			crUrl = crUrl.replace(new URL(crUrl).getHost(), publicIP);
			kr = kickUsingTCP(unit, xapsUnit, crUrl, crPass, crUser);
		}

		return kr;
	}

	private static KickResponse kickUsingTCP(Unit unit, XAPSUnit xapsUnit, String crUrl, String crPass, String crUser) throws SQLException, NoAvailableConnectionException, MalformedURLException {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(crUrl);
		get.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, new Integer(20000));
		get.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, new Integer(20000));
		client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, true));
		int statusCode = HttpStatus.SC_OK;
		if (crUser != null && crPass != null) {
			get = authenticate(client, get, crUrl, crUser, crPass);
			log.debug(unit.getId() + " had a password and username, hence the kick will be executed with authentication (digest/basic)");
		}
		try {
			HttpResponse response = client.execute(get);
			statusCode = response.getStatusLine().getStatusCode();
		} catch (ConnectTimeoutException ce) {
			log.warn(unit.getId() + " did not respond, indicating a NAT problem or disconnected.");
			return new KickResponse(false, "TCP/HTTP-kick to " + crUrl + " failed, probably due to NAT or other connection problems: " + ce.getMessage());
		} catch (Throwable t) {
			log.warn(unit.getId() + " did not respond, an error has occured: ", t);
			return new KickResponse(false, "TCP/HTTP-kick to " + crUrl + " failed because of an unexpected error: " + t.getMessage());
		}
		if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_NO_CONTENT) {
			log.debug(unit.getId() + " responded with HTTP " + statusCode + ", indicating a successful kick");
			return new KickResponse(true, "TCP/HTTP-kick to " + crUrl + " got HTTP response code " + statusCode + ", indicating success");
		} else {
			log.warn(unit.getId() + " responded with HTTP " + statusCode + ", indicating a unsuccessful kick");
			if (statusCode == HttpStatus.SC_FORBIDDEN || statusCode == HttpStatus.SC_UNAUTHORIZED)
				return new KickResponse(false, "TCP/HTTP-kick to " + crUrl + " (user:" + crUser + ",pass:" + crPass + ") failed, probably due to wrong user/pass since HTTP response code is "
						+ statusCode);
			else
				return new KickResponse(false, "TCP/HTTP-kick to " + crUrl + " failed with HTTP response code " + statusCode);
		}
	}

	private static KickResponse kickUsingUDP(Unit unit, XAPSUnit xapsUnit, String udpCrUrl, String crUrl, String crPass, String crUser) throws NoAvailableConnectionException, SQLException {
		try {
			String id = "" + random.nextInt(100000);
			String cn = "" + random.nextLong();
			String ts = "" + System.currentTimeMillis();
			String text = ts + id + crUser + cn;
			if (crPass == null)
				crPass = "password"; // we must have a password - so we simply set a default password
			String sig = Crypto.computeHmacSHA1AsHexUpperCase(crPass, text);
			// original, according to TR-111 spec example
			//			String req = "GET http://" + udpCrUrl + "?ts=" + ts + "&id=" + id + "&un=" + crUser + "&cn=" + cn + "&sig=" + sig + " HTTP/1.1\r\n";
			// assumed proper HTTP URI
			//			String req = "GET http://" + udpCrUrl + "/?ts=" + ts + "&id=" + id + "&un=" + crUser + "&cn=" + cn + "&sig=" + sig + " HTTP/1.1\r\n";
			// testing without absolute URI
			String req = "GET http://" + udpCrUrl + "/?ts=" + ts + "&id=" + id + "&un=" + crUser + "&cn=" + cn + "&sig=" + sig + " HTTP/1.1\r\n\r\n";
			byte[] buf = req.getBytes();
			if (udpCrUrl.indexOf(":") == -1)
				udpCrUrl += ":80";
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
			return new KickResponse(false, "UDP kick to " + udpCrUrl + " failed: " + t.getMessage() + "");
		}
	}

	/* KICK RELATED METHODS */

	private static String getKeyroot(Unit u) {
		for (String paramName : u.getParameters().keySet()) {
			if (paramName.startsWith("Device."))
				return "Device.";
			if (paramName.startsWith("InternetGatewayDevice."))
				return "InternetGatewayDevice.";
		}
		throw new RuntimeException("No keyroot found for unit " + u.getId() + ", probably because no parameters are defined");
	}

	private static HttpGet authenticate(DefaultHttpClient client, HttpGet get, String urlStr, String username, String password) throws MalformedURLException {
		URL url = new URL(urlStr);
		List<String> authPrefs = new ArrayList<String>(2);
		authPrefs.add(AuthPolicy.DIGEST);
		authPrefs.add(AuthPolicy.BASIC);
		client.getParams().setParameter(AuthPNames.PROXY_AUTH_PREF, authPrefs);
		client.getParams().setParameter(AuthPNames.TARGET_AUTH_PREF, authPrefs);
		Credentials defaultcreds = new UsernamePasswordCredentials(username, password);
		client.getCredentialsProvider().setCredentials(new AuthScope(url.getHost(), url.getPort()), defaultcreds);
		return get;
	}

}
