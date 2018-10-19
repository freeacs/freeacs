package com.github.freeacs.common.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The Class HTTPSManager. */
public class HTTPSManager {
  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(HTTPSManager.class);

  /**
   * Install certificate.
   *
   * @param url the url
   * @param password the password
   * @throws Exception the exception
   */
  public static void installCertificate(String url, String password) throws Exception {
    int port = 443;
    char[] passphrase = password.toCharArray();
    int doubleSlashPos = url.indexOf("//");
    int slashPos = url.indexOf('/', doubleSlashPos + 2);
    String host = url.substring(doubleSlashPos + 2, slashPos);
    int colonPos = host.indexOf(':');
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
    TrustManagerFactory tmf =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(ks);
    X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
    SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);
    context.init(null, new TrustManager[] {tm}, null);
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
      logger.error(
          "Aborted - no connection setup - the remote host (" + url + ") does not respond", ste);
      throw ste;
    }

    X509Certificate[] chain = tm.getChain();
    if (chain == null) {
      logger.error("Could not obtain server certificate chain");
      throw new RuntimeException(
          "Could not obtain server certificate chain from remote host (" + url + ")");
    }

    logger.debug("Server sent " + chain.length + " certificate(s):");

    X509Certificate cert = chain[chain.length - 1];
    String alias = host + "-" + chain.length;
    ks.setCertificateEntry(alias, cert);
    OutputStream out = new FileOutputStream(file);
    ks.store(out, passphrase);
    out.close();
    logger.debug(
        "Added Certificate-"
            + (chain.length - 1)
            + " to keystore '"
            + file.getCanonicalPath()
            + "' using alias '"
            + alias
            + "'");
  }
}
