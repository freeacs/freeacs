package com.github.freeacs.common.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

/** The Class SavingTrustManager. */
public class SavingTrustManager implements X509TrustManager {
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

  public X509Certificate[] getAcceptedIssuers() {
    return tm.getAcceptedIssuers();
  }

  public void checkClientTrusted(X509Certificate[] chain, String authType)
      throws CertificateException {
    tm.checkClientTrusted(chain, authType);
  }

  public void checkServerTrusted(X509Certificate[] chain, String authType)
      throws CertificateException {
    this.chain = chain;
    tm.checkServerTrusted(chain, authType);
  }

  public X509Certificate[] getChain() {
    return chain;
  }
}
