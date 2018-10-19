package com.github.freeacs.common.ssl;

/*
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * EasyX509TrustManager unlike default {@link X509TrustManager} accepts self-signed certificates.
 *
 * <p>This trust manager SHOULD NOT be used for productive systems due to security reasons, unless
 * it is a concious decision and you are perfectly aware of security implications of accepting
 * self-signed certificates
 *
 * @author <a href="mailto:adrian.sutton@ephox.com">Adrian Sutton</a>
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 *     <p>DISCLAIMER: HttpClient developers DO NOT actively support this component. The component is
 *     provided as a reference material, which may be inappropriate for use without additional
 *     customization.
 */
public class EasyX509TrustManager implements X509TrustManager {
  /** The standard trust manager. */
  private X509TrustManager standardTrustManager;

  /**
   * Constructor for EasyX509TrustManager.
   *
   * @param keystore the keystore
   * @throws NoSuchAlgorithmException the no such algorithm exception
   * @throws KeyStoreException the key store exception
   */
  public EasyX509TrustManager(KeyStore keystore)
      throws NoSuchAlgorithmException, KeyStoreException {
    TrustManagerFactory factory =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    factory.init(keystore);
    TrustManager[] trustmanagers = factory.getTrustManagers();
    if (trustmanagers.length == 0) {
      throw new NoSuchAlgorithmException("no trust manager found");
    }
    this.standardTrustManager = (X509TrustManager) trustmanagers[0];
  }

  /**
   * Check client trusted.
   *
   * @param certificates the certificates
   * @param authType the auth type
   * @throws CertificateException the certificate exception
   * @see javax.net.ssl.X509TrustManager#checkClientTrusted(X509Certificate[],String authType)
   */
  public void checkClientTrusted(X509Certificate[] certificates, String authType)
      throws CertificateException {
    standardTrustManager.checkClientTrusted(certificates, authType);
  }

  /**
   * Check server trusted.
   *
   * @param certificates the certificates
   * @param authType the auth type
   * @throws CertificateException the certificate exception
   * @see javax.net.ssl.X509TrustManager#checkServerTrusted(X509Certificate[],String authType)
   */
  public void checkServerTrusted(X509Certificate[] certificates, String authType)
      throws CertificateException {
    if (certificates == null || certificates.length != 1) {
      standardTrustManager.checkServerTrusted(certificates, authType);
    }
  }

  /**
   * Gets the accepted issuers.
   *
   * @return the accepted issuers
   * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
   */
  public X509Certificate[] getAcceptedIssuers() {
    return this.standardTrustManager.getAcceptedIssuers();
  }
}
