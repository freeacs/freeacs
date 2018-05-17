package com.github.freeacs.ws.impl;

/*
 * Copyright 2006 Sun Microsystems, Inc.  All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class InstallCert {

	private static final Logger logger = LoggerFactory.getLogger(InstallCert.class);

	public static void installCertificate(String url) throws Exception {
		int port = 443;
		char[] passphrase = "changeit".toCharArray();
		int doubleSlashPos = url.indexOf("//");
		int slashPos = url.indexOf("/", doubleSlashPos + 2);
		String host = url.substring(doubleSlashPos + 2, slashPos);
		int colonPos = host.indexOf(":");
		if (colonPos > -1) {
			port = Integer.parseInt(host.substring(colonPos + 1));
			host = host.substring(0, colonPos);
		}
		char SEP = File.separatorChar;
		File dir = new File(System.getProperty("java.home") + SEP + "lib" + SEP + "security");
		File file = new File(dir, "jssecacerts");
		if (file.isFile() == false) {
			file = new File(dir, "cacerts");
		}
		InputStream in = new FileInputStream(file);
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(in, passphrase);
		in.close();

		SSLContext context = SSLContext.getInstance("TLS");
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(ks);
		X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
		SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);
		context.init(null, new TrustManager[] { tm }, null);
		SSLSocketFactory factory = context.getSocketFactory();
		logger.debug("KeyStore " + file + " is loaded");

		logger.debug("Opening connection to " + host + ":" + port + "...");
		SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
		socket.setSoTimeout(10000);
		try {
			socket.startHandshake();
			socket.close();
			logger.debug("SSL Certificate is already trusted");
			return;
		} catch (SSLException e) {
			logger.debug("SSL Certificate was not trusted, must be added to the keystore");
		}

		X509Certificate[] chain = tm.chain;
		if (chain == null) {
			logger.error("Could not obtain server certificate chain");
			return;
		}

		logger.debug("Server sent " + chain.length + " certificate(s):");
		MessageDigest sha1 = MessageDigest.getInstance("SHA1");
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		for (int i = 0; i < chain.length; i++) {
			X509Certificate cert = chain[i];
			String msg = "Certificate" + i + ": ";
			msg += "Subject:" + cert.getSubjectDN();
			msg += ", Issuer:" + cert.getIssuerDN();
			sha1.update(cert.getEncoded());
			msg += ", SHA1:" + toHexString(sha1.digest());
			md5.update(cert.getEncoded());
			msg += ", MD5:" + toHexString(md5.digest());
			logger.debug(msg);
		}
		X509Certificate cert = chain[chain.length - 1];
		String alias = host + "-" + chain.length;
		ks.setCertificateEntry(alias, cert);
		OutputStream out = new FileOutputStream(file);
		ks.store(out, passphrase);
		out.close();
		logger.debug("Added Certificate-" + (chain.length - 1) + " to keystore 'jssecacerts' using alias '" + alias + "'");
	}

	private static final char[] HEXDIGITS = "0123456789abcdef".toCharArray();

	private static String toHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length * 3);
		for (int b : bytes) {
			b &= 0xff;
			sb.append(HEXDIGITS[b >> 4]);
			sb.append(HEXDIGITS[b & 15]);
			sb.append(' ');
		}
		return sb.toString();
	}

	private static class SavingTrustManager implements X509TrustManager {

		private final X509TrustManager tm;
		private X509Certificate[] chain;

		SavingTrustManager(X509TrustManager tm) {
			this.tm = tm;
		}

		public X509Certificate[] getAcceptedIssuers() {
			throw new UnsupportedOperationException();
		}

		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			throw new UnsupportedOperationException();
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			this.chain = chain;
			tm.checkServerTrusted(chain, authType);
		}
	}

}