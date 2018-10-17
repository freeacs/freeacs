package com.github.freeacs.stun;

/**
 * These parameters holds a special meaning for the TR-069 Server. The server MUST know the value of
 * these CPE-parameters, regardless of what configuration is found in the database. If the database
 * only asks for parameter X and Y, but none of these parameters, the server will inject these
 * parameters in the GetParameterValueRequest to the CPE.
 *
 * <p>The reason for this special interest is that these parameters controls the following important
 * features of the server: - download (config/firmware) - periodic inform interval (spread/static) -
 * line authentication (has the subscriber moved the CPE?)
 *
 * @author Morten
 */
public class CPEParameters {
  /** The config version of the CPE. */
  public String CONFIG_VERSION;
  /** The software/firmware version of the CPE. */
  public String SOFTWARE_VERSION;
  /** The periodic inform interval on the CPE. */
  public String PERIODIC_INFORM_INTERVAL;
  /** The connection url (for kick, ip-address). */
  public String CONNECTION_URL;
  /** The connection username (for kick, using authentication). */
  public String CONNECTION_USERNAME;
  /** The connection password (for kick, using authentication). */
  public String CONNECTION_PASSWORD;
  /** The UDP Connection URL (for kick through NAT). */
  public String UDP_CONNECTION_URL;

  /**
   * // The phone number public String PHONE_NUMBER; // The voice service enabled parameter public
   * String VOICE_ENABLED;.
   */
  public CPEParameters(String keyRoot) {
    CONFIG_VERSION = keyRoot + "DeviceInfo.VendorConfigFile.1.Version";
    SOFTWARE_VERSION = keyRoot + "DeviceInfo.SoftwareVersion";
    PERIODIC_INFORM_INTERVAL = keyRoot + "ManagementServer.PeriodicInformInterval";
    CONNECTION_URL = keyRoot + "ManagementServer.ConnectionRequestURL";
    CONNECTION_PASSWORD = keyRoot + "ManagementServer.ConnectionRequestPassword";
    CONNECTION_USERNAME = keyRoot + "ManagementServer.ConnectionRequestUsername";
    // Optional - only for TR-111
    UDP_CONNECTION_URL = keyRoot + "ManagementServer.UDPConnectionRequestAddress";
  }
}
