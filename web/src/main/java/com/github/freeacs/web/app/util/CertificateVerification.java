package com.github.freeacs.web.app.util;

import com.github.freeacs.common.db.NoAvailableConnectionException;
import com.github.freeacs.dbi.Certificate;
import com.github.freeacs.dbi.Certificates;
import com.github.freeacs.dbi.XAPS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;


/**
 * Verifies certificates.
 *
 * @author Jarl Andre Hubenthal
 */
public class CertificateVerification {
	
	/** The logger. */
	private static final Logger logger = LoggerFactory.getLogger(CertificateVerification.class);
	
	/**
	 * Checks if is certificate valid.
	 *
	 * @param name the name
	 * @param sessionId the session id
	 * @return true, if is certificate valid
	 * @throws NoAvailableConnectionException the no available connection exception
	 * @throws SQLException the sQL exception
	 */
	private static boolean isCertificateValid(String name,String sessionId) throws NoAvailableConnectionException, SQLException{
		XAPS xaps = XAPSLoader.getXAPS(sessionId);
		
		Certificates certs = xaps.getCertificates();
		
		Certificate cert = certs.getCertificate(name);
		
		if(cert==null) 
			return false;
			
		if(cert.isTrial() && cert.isValid(null)){ // Trial certificate
			if(cert.getDateLimit()!=null){ // Date limit trial
				try {
					SessionData sessionData = SessionCache.getSessionData(sessionId);
					if(sessionData.getTimeServerDate()==null)
						sessionData.setTimeServerDate(TimeServerClient.getTimeFromServer());
					if(sessionData.getTimeServerDate()!=null && cert.getDateLimit().after(sessionData.getTimeServerDate()))
						return true;
				} catch (Throwable e) {
					logger.debug("NTP time server request failed. Silently ignoring it.", e);
					return true;
				}
			}else{ // Trial without date limit
				return true;
			}
		}else if(cert.isValid(null)){ // Production certificate
			return true;
		}
		
		return false;
	}
}
