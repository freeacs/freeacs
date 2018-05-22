package com.github.freeacs.dbi.crypto;

import com.github.freeacs.dbi.Certificate;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Calendar;

/*
 * This class should handle all crypto and crypto-like operations for ACS. There are currently X
 * operations needed:
 * 
 *  1. compute a SHA1 digest - used to store passwords in the ACS database
 *  2. compute a HMAC-SHA1 digest - used to transmit ConnectionPassword securly in TR-111
 *  3. encrypt certificates to send to customers - uses RSA private key from file
 *  4. decrypt certificates - uses RSA public key found in this class
 */

public class Crypto {
	private static final BigInteger mPub = new BigInteger(
			"120269692064713533478389412839671283417190682426870272314449441321184544461014990076810327411338879981623246052223890289623629997288030091485056773717523566355022759812796435413424582764103109105774543772877073128855329339531440780115102907452611266194976734052729897312276845685331050852311259750770004972459");
	private static final BigInteger ePub = new BigInteger("65537");
	public static String PK_PATH = "freeacs-private-RSA-key.txt";

	private static String convertByte2HexUpperCase(byte[] bytes) {
		StringBuffer s = new StringBuffer();
		for (byte b : bytes) {
			s.append(String.format("%02x", b));
		}
		return s.toString().toUpperCase();
	}

	private static byte[] convertHex2Bytes(String hex) {
		byte[] bytes = new byte[hex.length() / 2];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
		}
		return bytes;
	}

	public static String decryptUsingRSAPublicKey(String s) {
		String dec = "";
		while (true) {
			String decStr = null;
			if (s.length() >= 256) {
				decStr = s.substring(0, 256);
				s = s.substring(256);
			} else
				decStr = s;
			dec += decryptChunkUsingRSAPublicKey(decStr);
			if (s.length() < 256)
				break;
		}
		return dec;

	}

	private static String decryptChunkUsingRSAPublicKey(String encryptedHex) {
		try {
			byte[] encryptedBytes = convertHex2Bytes(encryptedHex);
			KeyFactory fact = KeyFactory.getInstance("RSA");
			RSAPublicKeySpec keySpecPub = new RSAPublicKeySpec(mPub, ePub);
			PublicKey publicKey = fact.generatePublic(keySpecPub);
			Cipher dec = Cipher.getInstance("RSA");
			dec.init(Cipher.DECRYPT_MODE, publicKey);
			return new String(dec.doFinal(encryptedBytes));
		} catch (BadPaddingException bpe) {
			throw new RuntimeException("encrypted string has been tampered with - not possible to decrypt");
		} catch (Throwable t) {
			throw new RuntimeException("An error occured while trying to decrypt (may be wrong version of JVM?) " + encryptedHex, t);
		}
	}

	public static String computeSHA1DigestAsHexUpperCase(String text) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA1");
			byte[] sha1hash = new byte[40];
			md.update(text.getBytes(), 0, text.length());
			sha1hash = md.digest();
			return convertByte2HexUpperCase(sha1hash);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("The JVM does not support SHA1 digest mechanism - maybe it's not correct JVM version", e);
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
			throw new RuntimeException("InvalidKeyException: " + ike + ", maybe the JVM version is not correct", ike);
		}
	}

	public static String computeRSACertificate(String s) throws Exception {
		String cert = "";
		while (true) {
			String encStr = null;
			if (s.length() > 117) {
				encStr = s.substring(0, 117);
				s = s.substring(117);
			} else
				encStr = s;
			cert += computeRSACertificateOfChunk(encStr);
			if (encStr.length() < 117)
				break;
		}
		return cert;
	}

	/**
	 * Test
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		CertificateDetails cert1 = new CertificateDetails(com.github.freeacs.dbi.Certificate.CERT_TYPE_REPORT, null, null, null);
		String cert1Str  =computeCertificateFromObject(cert1);
		//		CertificateDetails cert2 = new CertificateDetails(Certificate.CERT_TYPE_REPORT, null, null, null);
		//		String cert2Str = computeCertificateFromObject(cert2);
		System.out.println("Cert: " + cert1Str);
		System.out.println("Decrypted: "  + decryptUsingRSAPublicKey(cert1Str));
	}

	public static String computeCertificateFromObject(CertificateDetails cert) throws Exception {
		try {
			String s = null;
			if (cert.getType().equals(com.github.freeacs.dbi.Certificate.CERT_TYPE_REPORT))
				s = com.github.freeacs.dbi.Certificate.CERT_TYPE_REPORT;
			if (cert.getType().equals(com.github.freeacs.dbi.Certificate.CERT_TYPE_PROVISIONING))
				s = com.github.freeacs.dbi.Certificate.CERT_TYPE_PROVISIONING;
			if (cert.getCustomerName() != null) {
				s += ", issued to " + cert.getCustomerName();
			} else if (cert.getLimitType() != null) {
				if (cert.getLimitType().equals(com.github.freeacs.dbi.Certificate.TRIAL_TYPE_DAYS)) {
					Calendar c = Calendar.getInstance();
					c.add(Calendar.DAY_OF_YEAR, cert.getLimit());
					s += " until " + com.github.freeacs.dbi.Certificate.dateFormat.format(c.getTime());
				} else if (cert.getLimitType().equals(Certificate.TRIAL_TYPE_COUNT))
					s += ", maximum level " + cert.getLimit();
				else {
					throw new IllegalArgumentException("Usage: Crypto Report|Provisioning [time <numberOfDays>] [count <maximumCount>]. Supplied: " + cert.toString());
				}
			}
			System.out.println("Generating: " + s);
			System.out.println("Supplied: " + cert.toString());
			return computeRSACertificate(s);
		} catch (Exception nsae) {
			throw nsae;
		}
	}

	/**
	 * Turns array of bytes into string
	 * 
	 * @param buf
	 *            Array of bytes to convert to hex string
	 * @return Generated hex string
	 */
	private static String computeRSACertificateOfChunk(String s) throws Exception {
		if (s == null)
			System.out.println("Must specify a string to generate certificate of");
		FileReader fr = new FileReader(Crypto.PK_PATH);
		BufferedReader br = new BufferedReader(fr);
		String line = null;
		BigInteger mPriv = null;
		BigInteger ePriv = null;
		while ((line = br.readLine()) != null) {
			if (mPriv == null)
				mPriv = new BigInteger(line);
			else
				ePriv = new BigInteger(line);
		}
		KeyFactory fact = KeyFactory.getInstance("RSA");
		RSAPrivateKeySpec keySpecPriv = new RSAPrivateKeySpec(mPriv, ePriv);
		PrivateKey privateKey2 = fact.generatePrivate(keySpecPriv);

		Cipher c = Cipher.getInstance("RSA");
		// Initiate the Cipher, telling it that it is going to Encrypt, giving
		// it the private key
		c.init(Cipher.ENCRYPT_MODE, privateKey2);
		// Create a secret message
		// Encrypt that message using a new SealedObject and the Cipher we
		// created before
		byte[] encryptedText = c.doFinal(s.getBytes());
		String textToSendToCustomers = convertByte2HexUpperCase(encryptedText);

		return textToSendToCustomers;
	}
}