package com.github.freeacs.dbi.crypto;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * This class should handle all crypto and crypto-like operations for ACS. There are currently X
 * operations needed:
 *
 * <ol>
 *    <li>compute a SHA-256 digest - used to store passwords in the ACS database</li>
 *    <li>compute a HmacSHA256</li>
 * </ol>
 * digest - used to transmit ConnectionPassword securly in TR-111
 */
public class Crypto {
  private static final String HASH_ALGORITHM = "SHA-256"; // Updated to SHA-256, previously SHA-1
  private static final String HMAC_ALGORITHM = "HmacSHA256"; // Updated to HmacSHA256, previously HmacSHA1

  private static String convertByte2HexUpperCase(byte[] bytes) {
    StringBuilder s = new StringBuilder();
    for (byte b : bytes) {
      s.append(String.format("%02x", b));
    }
    return s.toString().toUpperCase();
  }

  public static String computeDigestAsHexUpperCase(String text) {
    try {
      MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
      byte[] hash = md.digest(text.getBytes(StandardCharsets.UTF_8));
      return convertByte2HexUpperCase(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Failed to compute hash: " + e.getMessage(), e);
    }
  }

  public static String computeHmacAsHexUpperCase(String key, String text) {
    try {
      Mac mac = Mac.getInstance(HMAC_ALGORITHM);
      SecretKeySpec secret = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
      mac.init(secret);
      byte[] hmac = mac.doFinal(text.getBytes(StandardCharsets.UTF_8));
      return convertByte2HexUpperCase(hmac);
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new RuntimeException("Failed to compute HMAC: " + e.getMessage(), e);
    }
  }

  public static void main(String[] args) {
    System.out.println(computeDigestAsHexUpperCase("freeacs"));
    System.out.println(computeHmacAsHexUpperCase("key", "text"));
  }
}
