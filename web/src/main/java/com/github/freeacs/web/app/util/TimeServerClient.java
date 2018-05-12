package com.github.freeacs.web.app.util;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;


/**
 * Time server client implementation.
 * 
 * @author Jarl Andre Hubenthal
 *
 */
public abstract class TimeServerClient {
	
	/** The Constant DEFAULT_TIME_SERVER. */
	private static final String DEFAULT_TIME_SERVER = "pool.ntp.org";
	
	/** The Constant DEFAULT_TIMEOUT. */
	private static final int DEFAULT_TIMEOUT = 2000;
    
    /**
     * Gets the reference time stamp.
     *
     * @param info the info
     * @return the reference time stamp
     */
    private static Date getReferenceTimeStamp(TimeInfo info){
    	TimeStamp refNtpTime = info.getMessage().getReferenceTimeStamp();
    	return refNtpTime.getDate();
    }
    
    /**
     * Gets the time from server.
     *
     * @return the time from server
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Date getTimeFromServer() throws IOException{
    	return getTimeFromServer(DEFAULT_TIME_SERVER,DEFAULT_TIMEOUT);
    }

    /**
     * Gets the time from server.
     *
     * @param server the server
     * @param defaultTimeout the default timeout
     * @return the time from server
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Date getTimeFromServer(String server, int defaultTimeout) throws IOException
    {
        NTPUDPClient client = new NTPUDPClient();
        client.setDefaultTimeout(defaultTimeout);
        InetAddress hostAddr = InetAddress.getByName(server);
        TimeInfo info = client.getTime(hostAddr);
        client.close();
        return getReferenceTimeStamp(info);
    }
}