package com.github.freeacs.dbi.crypto;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * This class should handle all crypto and crypto-like operations for ACS. There are currently X
 * operations needed:
 *
 * <p>1. compute a SHA1 digest - used to store passwords in the ACS database 2. compute a HMAC-SHA1
 * digest - used to transmit ConnectionPassword securly in TR-111
 */
public class Crypto {
  private static String convertByte2HexUpperCase(byte[] bytes) {
    StringBuilder s = new StringBuilder();
    for (byte b : bytes) {
      s.append(String.format("%02x", b));
    }
    return s.toString().toUpperCase();
  }

  public static String computeSHA1DigestAsHexUpperCase(String text) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA1");
      byte[] sha1hash;
      md.update(text.getBytes(), 0, text.length());
      sha1hash = md.digest();
      return convertByte2HexUpperCase(sha1hash);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(
          "The JVM does not support SHA1 digest mechanism - maybe it's not correct JVM version", e);
    }
  }

  public static String computeHmacSHA1AsHexUpperCase(String key, String text) {
    try {
      Mac mac = Mac.getInstance("HmacSHA1");
      SecretKeySpec secret = new SecretKeySpec(key.getBytes(), "HmacSHA1");
      mac.init(secret);
      byte[] digest = mac.doFinal(text.getBytes());
      return convertByte2HexUpperCase(digest);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("The JVM does algorithm - maybe it's not correct JVM version", e);
    } catch (InvalidKeyException ike) {
      throw new RuntimeException(
          "InvalidKeyException: " + ike + ", maybe the JVM version is not correct", ike);
    }
  }
}
