package com.github.freeacs.spp.http;

import com.github.freeacs.base.Log;
import com.github.freeacs.dbi.Certificate;
import com.github.freeacs.dbi.Certificates;
import com.github.freeacs.spp.Properties;
import com.github.freeacs.spp.SessionData;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Authenticator {

	private static Set<String> hostSet = new HashSet<String>();

	public static boolean authenticate(HttpServletRequest req, HttpServletResponse res, SessionData sessionData) throws ServletException, IOException {

		if (sessionData.isAuthenticated()) {
			return true;
		}
		boolean authenticated = true; // default
		String auth_method = Properties.getAuthMethod();
		if (Properties.isDiscoveryMode()) {
			authenticated = BasicAuthenticator.authenticate(req, res, sessionData);
		} else {
			if (auth_method.equalsIgnoreCase("basic"))
				authenticated = BasicAuthenticator.authenticate(req, res, sessionData);
			else if (auth_method.equalsIgnoreCase("digest"))
				authenticated = DigestAuthenticator.authenticate(req, res, sessionData);
			else if (auth_method.equalsIgnoreCase("none")) {
				authenticated = true;
				Log.debug(Authenticator.class, "No authentication method was required");
			} else {
				Log.error(Authenticator.class, "The authentication method is " + auth_method + ", but no impl. exist for this method");
				return false;
			}
		}
		/* Morten jan 2014 - remove certificate check since going open-source
		if (authenticated)
			authenticated = checkCertificate(req, sessionData);
		*/

		return authenticated;
	}

	private static boolean checkCertificate(HttpServletRequest req, SessionData sessionData) {
		Certificates certs = sessionData.getDbAccess().getXaps().getCertificates();
		Certificate cert = certs.getCertificate(Certificate.CERT_TYPE_PROVISIONING);
		if (cert == null || !cert.isDecrypted()) {
			Log.error(Authenticator.class, "The authentication was ok, but no certificate exists for provisioning");
			sessionData.setAuthenticated(false);
		} else if (cert.isTrial()) {
			if (cert.getMaxCount() != null) {
				if (hostSet.size() <= cert.getMaxCount()) {
					hostSet.add(req.getRemoteHost());
					sessionData.setAuthenticated(true);

				} else {
					Log.error(Authenticator.class, "The authentication was ok, but the certificate does not allow more than " + cert.getMaxCount() + " provisioned units");
					sessionData.setAuthenticated(false);
				}
			} else if (cert.getDateLimit() != null) {
				if (System.currentTimeMillis() <= cert.getDateLimit().getTime() + 1440 * 60 * 1000) { // will always add 1 day extra
					sessionData.setAuthenticated(true);
				} else {
					Log.error(Authenticator.class, "The authentication was ok, but the certificate expired at " + cert.getDateLimit());
					sessionData.setAuthenticated(false);
				}
			}

		} else if (cert.isProductionAndValid()) {
			sessionData.setAuthenticated(true);
		} else {
			Log.error(Authenticator.class, "The authentication was ok, but the certificate was not ok (the error is unknown)");
			sessionData.setAuthenticated(false);
		}
		return sessionData.isAuthenticated();
	}
}
