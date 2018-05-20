package com.github.freeacs.web.app.page.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.*;
import java.net.SocketTimeoutException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


/**
 * The Class HTTPSManager.
 */
public class HTTPSManager {

	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(HTTPSManager.class);

	/**
	 * Install certificate.
	 *
	 * @param url the url
	 * @param password
	 * @throws Exception the exception
	 */
	public static void installCertificate(String url, String password) throws Exception {
		int port = 443;
		char[] passphrase = password.toCharArray();
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
		if (!file.isFile()) {
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
			logger.warn("SSL Certificate was not trusted, must be added to the keystore", e);
		} catch (SocketTimeoutException ste) {
			logger.error("Aborted - no connection setup - the remote host (" + url + ") does not respond", ste);
			throw ste;
		}

		X509Certificate[] chain = tm.chain;
		if (chain == null) {
			logger.error("Could not obtain server certificate chain");
			throw new RuntimeException("Could not obtain server certificate chain from remote host (" + url + ")");
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
		logger.debug("Added Certificate-" + (chain.length - 1) + " to keystore '"+file.getCanonicalPath()+"' using alias '" + alias + "'");
	}

	/** The Constant HEXDIGITS. */
	private static final char[] HEXDIGITS = "0123456789abcdef".toCharArray();

	/**
	 * To hex string.
	 *
	 * @param bytes the bytes
	 * @return the string
	 */
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

	/**
	 * The Class SavingTrustManager.
	 */
	private static class SavingTrustManager implements X509TrustManager {

		/** The tm. */
		private final X509TrustManager tm;
		
		/** The chain. */
		private X509Certificate[] chain;

		/**
		 * Instantiates a new saving trust manager.
		 *
		 * @param tm the tm
		 */
		SavingTrustManager(X509TrustManager tm) {
			this.tm = tm;
		}

		/* (non-Javadoc)
		 * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
		 */
		public X509Certificate[] getAcceptedIssuers() {
			return tm.getAcceptedIssuers();
//			throw new UnsupportedOperationException();
		}

		/* (non-Javadoc)
		 * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[], java.lang.String)
		 */
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			tm.checkClientTrusted(chain, authType);
//			throw new UnsupportedOperationException();
		}

		/* (non-Javadoc)
		 * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[], java.lang.String)
		 */
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			this.chain = chain;
			tm.checkServerTrusted(chain, authType);
		}
	}

}
