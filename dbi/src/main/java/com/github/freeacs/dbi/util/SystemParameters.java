package com.github.freeacs.dbi.util;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.dbi.UnittypeParameterFlag;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

public class SystemParameters {
  /**
   * Control provisioning (secret, encryption, download, reboot, reset) through these parameters.
   */
  // The desired config version for a unit
  public static final String DESIRED_TR069_SCRIPT = "System.X_FREEACS-COM.TR069Script.";
  /** The desired software/firmware version for a unit. */
  public static final String DESIRED_SOFTWARE_VERSION =
      "System.X_FREEACS-COM.DesiredSoftwareVersion";
  /**
   * The target filename in the event of a download - not necessarily used (mostly for download of
   * script/vendor config file) public static final String TARGET_FILENAME =
   * "System.X_FREEACS-COM.Download.TargetFileName"; public static final String
   * TARGET_TR069_SCRIPT_FILENAME =
   * "System.X_FREEACS-COM.TR069Script.DesiredTR069ScriptTargetFileName"; The config url (used in
   * download-request) public static final String SCRIPT_URL = "System.X_FREEACS-COM.ScriptURL"; The
   * software/firmware url (used in download request)
   */
  public static final String SOFTWARE_URL = "System.X_FREEACS-COM.SoftwareURL";
  /** The secret, both for TR-069 and OPP. Will replace both TR069_SECRET and OPP_SECRET */
  public static final String SECRET = "System.X_FREEACS-COM.Secret";
  /** A scheme to be used to encrypt the reponse from the server - probably only used for TFTP. */
  public static final String SECRET_SCHEME = "System.X_FREEACS-COM.SecretScheme";
  /** The reboot flag, set to reboot the Device. */
  public static final String RESTART = "System.X_FREEACS-COM.Restart";
  /** The reset flag, set to factory reset the Device. */
  public static final String RESET = "System.X_FREEACS-COM.Reset";
  /**
   * The discovery flag, set to run GetParameterNames on device - update the unittype configuration.
   */
  public static final String DISCOVER = "System.X_FREEACS-COM.Discover";

  public static final String COMMENT = "System.X_FREEACS-COM.Comment";

  // The provisioning priority: Can have these values:
  // Reset-Reboot-Download-Config (default)
  // Config-Download-Reboot-Reset
  //	public static final String PROVISIONING_PRIORITY = "System.X_FREEACS-COM.ProvisioningPriority";

  /** Data about the provisioning client connect/provisioning. */
  public static final String FIRST_CONNECT_TMS = "System.X_FREEACS-COM.FirstConnectTms";

  public static final String LAST_CONNECT_TMS = "System.X_FREEACS-COM.LastConnectTms";

  /** The provisioning mode and state parameter, use the MODE_ constants and STATE_ constants. */
  public static final String PROVISIONING_MODE = "System.X_FREEACS-COM.ProvisioningMode";

  public static final String INSPECTION_MESSAGE = "System.X_FREEACS-COM.IM.Message";

  /**
   * The service window parameters regulate when a provisioning of a certain type can be performed.
   */
  public static final String SERVICE_WINDOW_ENABLE = "System.X_FREEACS-COM.ServiceWindow.Enable";

  public static final String SERVICE_WINDOW_REGULAR = "System.X_FREEACS-COM.ServiceWindow.Regular";
  public static final String SERVICE_WINDOW_DISRUPTIVE =
      "System.X_FREEACS-COM.ServiceWindow.Disruptive";
  public static final String SERVICE_WINDOW_FREQUENCY =
      "System.X_FREEACS-COM.ServiceWindow.Frequency";
  public static final String SERVICE_WINDOW_SPREAD = "System.X_FREEACS-COM.ServiceWindow.Spread";

  /**
   * The conversation log parameter, enables an operator to turn on conversation logging on certain
   * units to a special debug-file (in TR-069 server).
   */
  public static final String DEBUG = "System.X_FREEACS-COM.Debug";

  /** The current job and the history of jobs. */
  public static final String JOB_CURRENT = "System.X_FREEACS-COM.Job.Current";

  public static final String JOB_CURRENT_KEY = "System.X_FREEACS-COM.Job.CurrentKey";
  public static final String JOB_HISTORY = "System.X_FREEACS-COM.Job.History";
  public static final String JOB_DISRUPTIVE = "System.X_FREEACS-COM.Job.Disruptive";

  /** The staging parameters - only used for staging server. */
  public static final String STAGING_SHIPMENT_YEAR = "System.X_FREEACS-COM.Staging.Shipment.Year";

  public static final String STAGING_SHIPMENT_MONTH = "System.X_FREEACS-COM.Staging.Shipment.Month";
  public static final String STAGING_SHIPMENT_DATE = "System.X_FREEACS-COM.Staging.Shipment.Date";
  public static final String STAGING_SHIPMENT_REGISTERED =
      "System.X_FREEACS-COM.Staging.Shipment.Registered";
  public static final String STAGING_PROVIDER_WSURL =
      "System.X_FREEACS-COM.Staging.Provider.WS.URL";
  public static final String STAGING_PROVIDER_WSPASSWORD =
      "System.X_FREEACS-COM.Staging.Provider.WS.Password";
  public static final String STAGING_PROVIDER_WSUSER =
      "System.X_FREEACS-COM.Staging.Provider.WS.User";
  public static final String STAGING_PROVIDER_UNITTYPE =
      "System.X_FREEACS-COM.Staging.Provider.Unittype";
  public static final String STAGING_PROVIDER_PROFILE =
      "System.X_FREEACS-COM.Staging.Provider.Profile";
  /**
   * Public static final String STAGING_PROVIDER_SNPARAMETER =
   * "System.X_FREEACS-COM.Staging.Provider.SerialNumberParameter"; public static final String
   * STAGING_PROVIDER_SECPARAMETER = "System.X_FREEACS-COM.Staging.Provider.SecretParameter";
   */
  public static final String STAGING_PROVIDER_EMAIL = "System.X_FREEACS-COM.Staging.Provider.Email";
  //	public static final String STAGING_PROVIDER_PROTOCOL =
  // "System.X_FREEACS-COM.Staging.Provider.Protocol";

  /** Device parameters/info - stored under System parameter for cross-protocol compatibility. */
  //	public static final String MAC = "System.X_FREEACS-COM.Device.MAC";
  public static final String SERIAL_NUMBER = "System.X_FREEACS-COM.Device.SerialNumber";

  public static final String SOFTWARE_VERSION = "System.X_FREEACS-COM.Device.SoftwareVersion";
  public static final String PERIODIC_INTERVAL = "System.X_FREEACS-COM.Device.PeriodicInterval";
  public static final String IP_ADDRESS = "System.X_FREEACS-COM.Device.PublicIPAddress";
  public static final String PROTOCOL = "System.X_FREEACS-COM.Device.PublicProtocol";
  public static final String PORT = "System.X_FREEACS-COM.Device.PublicPort";
  public static final String GUI_URL = "System.X_FREEACS-COM.Device.GUIURL";

  /** TR069 Test parameters. */
  // 0 or 1
  public static final String TEST_ENABLE = "System.X_FREEACS-COM.TR069Test.Enable";
  /** 0 or 1. */
  public static final String TEST_RESET_ON_STARTUP =
      "System.X_FREEACS-COM.TR069Test.FactoryResetOnStartup";
  /**
   * Can specify GET, SET, REBOOT or RESET as a comma separated list, no requirements about
   * ordering.
   */
  public static final String TEST_STEPS = "System.X_FREEACS-COM.TR069Test.Steps";
  /** VALUE, ATTRIBUTE or CUSTOM, default is VALUE. */
  public static final String TEST_METHOD = "System.X_FREEACS-COM.TR069Test.Method";
  /** Any string that can match a parameter name in test cases. */
  public static final String TEST_PARAM_FILTER = "System.X_FREEACS-COM.TR069Test.ParamFilter";
  /**
   * Any set of strings, each string enclosed by square brackets [ ], will match tags in test cases.
   */
  public static final String TEST_TAG_FILTER = "System.X_FREEACS-COM.TR069Test.TagFilter";

  // OPP-secret
  //	public static final String OPP_SECRET = "System.X_FREEACS-COM.OPP.Connector.FDSecret.Unit";

  /** Telnet provisioning parameters. */
  public static final String TELNET_IP = "System.X_FREEACS-COM.Telnet.IPAddress";

  public static final String TELNET_PORT = "System.X_FREEACS-COM.Telnet.Port";
  public static final String TELNET_USERNAME = "System.X_FREEACS-COM.Telnet.Username";
  public static final String TELNET_PASSWORD = "System.X_FREEACS-COM.Telnet.Password";
  public static final String TELNET_DESIRED_SCRIPT_VERSION =
      "System.X_FREEACS-COM.Telnet.DesiredScriptVersion";

  public static Map<String, UnittypeParameterFlag> commonParameters = new TreeMap<>();
  public static Map<String, UnittypeParameterFlag> stagingParameters = new TreeMap<>();

  private static UnittypeParameterFlag X = new UnittypeParameterFlag("X");
  private static UnittypeParameterFlag XC = new UnittypeParameterFlag("XC");

  static {
    commonParameters.put(DESIRED_SOFTWARE_VERSION, X);
    commonParameters.put(SOFTWARE_URL, X);
    commonParameters.put(PROVISIONING_MODE, X);
    commonParameters.put(INSPECTION_MESSAGE, X);
    commonParameters.put(SERVICE_WINDOW_ENABLE, X);
    commonParameters.put(SERVICE_WINDOW_REGULAR, X);
    commonParameters.put(SERVICE_WINDOW_DISRUPTIVE, X);
    commonParameters.put(SERVICE_WINDOW_FREQUENCY, X);
    commonParameters.put(SERVICE_WINDOW_SPREAD, X);
    commonParameters.put(DEBUG, X);
    commonParameters.put(JOB_CURRENT, X);
    commonParameters.put(JOB_CURRENT_KEY, X);
    commonParameters.put(JOB_HISTORY, X);
    commonParameters.put(JOB_DISRUPTIVE, X);
    commonParameters.put(FIRST_CONNECT_TMS, X);
    commonParameters.put(LAST_CONNECT_TMS, X);
    commonParameters.put(SERIAL_NUMBER, X);
    commonParameters.put(RESTART, X);
    commonParameters.put(RESET, X);
    commonParameters.put(DISCOVER, X);
    commonParameters.put(COMMENT, X);
    commonParameters.put(SECRET, XC);
    commonParameters.put(SECRET_SCHEME, X);
    commonParameters.put(SOFTWARE_VERSION, X);
    commonParameters.put(PERIODIC_INTERVAL, X);
    commonParameters.put(IP_ADDRESS, X);
    commonParameters.put(PROTOCOL, X);
    commonParameters.put(PORT, X);
    commonParameters.put(GUI_URL, X);
    commonParameters.put(TELNET_IP, X);
    commonParameters.put(TELNET_PASSWORD, XC);
    commonParameters.put(TELNET_PORT, X);
    commonParameters.put(TELNET_USERNAME, X);
    commonParameters.put(TELNET_DESIRED_SCRIPT_VERSION, X);
    commonParameters.put(TEST_ENABLE, X);
    commonParameters.put(TEST_RESET_ON_STARTUP, X);
    commonParameters.put(TEST_STEPS, X);
    commonParameters.put(TEST_METHOD, X);
    commonParameters.put(TEST_PARAM_FILTER, X);
    commonParameters.put(TEST_TAG_FILTER, X);

    stagingParameters.put(STAGING_SHIPMENT_YEAR, X);
    stagingParameters.put(STAGING_SHIPMENT_MONTH, X);
    stagingParameters.put(STAGING_SHIPMENT_DATE, X);
    stagingParameters.put(STAGING_SHIPMENT_REGISTERED, X);
    stagingParameters.put(STAGING_PROVIDER_WSURL, X);
    stagingParameters.put(STAGING_PROVIDER_WSUSER, X);
    stagingParameters.put(STAGING_PROVIDER_WSPASSWORD, X);
    stagingParameters.put(STAGING_PROVIDER_PROFILE, X);
    stagingParameters.put(STAGING_PROVIDER_UNITTYPE, X);
    stagingParameters.put(STAGING_PROVIDER_EMAIL, X);
  }

  public enum TR069ScriptType {
    Version,
    TargetFileName,
    URL
  }

  /**
   * Converts from targetName to the appropriate name used in the System-parameter. The conversion
   * rules are simple: Use the part of the targetName after the last slash or backslash. If no slash
   * or backslash present, use the whole targetName.
   *
   * <p>The allows you to retrieve two different TR069Script-parameters, Version or TargetFileName
   *
   * <p>If the unittype parameter does not exist, it will be created
   *
   * @param targetName
   * @return
   * @throws SQLException
   */
  public static UnittypeParameter getTR069ScriptParameter(
      String targetName, TR069ScriptType type, ACS acs, Unittype unittype) throws SQLException {
    String parameterName = getTR069ScriptParameterName(targetName, type);
    UnittypeParameter parameter = unittype.getUnittypeParameters().getByName(parameterName);
    if (parameter == null) {
      parameter = new UnittypeParameter(unittype, parameterName, X);
      unittype.getUnittypeParameters().addOrChangeUnittypeParameter(parameter, acs);
    }
    return parameter;
  }

  /**
   * Converts from name to the appropriate name used in the System-parameter. The conversion rules
   * are simple: Use the part of the name after the last slash or backslash. If no slash or
   * backslash present, use the whole name.
   *
   * <p>The allows you to retrieve three different TR069Script-parameters: Version, TargetFileName
   * and URL
   *
   * @param name
   * @param type
   * @return
   */
  public static String getTR069ScriptParameterName(String name, TR069ScriptType type) {
    if (name == null) {
      throw new IllegalArgumentException(
          "Not possible to create the DesiredTR069Script-parameter without a name");
    }
    int lastSlash = name.lastIndexOf('/');
    int lastBackslash = name.lastIndexOf('\\');
    int startPos = lastSlash > lastBackslash ? lastSlash : lastBackslash;
    if (startPos == -1) {
      startPos = 0;
    }
    String scriptName = name.substring(startPos);
    return DESIRED_TR069_SCRIPT + scriptName + "." + type;
  }

  /**
   * Returns true if parameterName matches "System.X_FREEACS-COM.TR069Script.<ScriptName>.Version"
   */
  public static boolean isTR069ScriptVersionParameter(String parameterName) {
    return parameterName.contains(DESIRED_TR069_SCRIPT)
        && parameterName.contains(TR069ScriptType.Version.toString());
  }

  /**
   * Extracts and return <Scriptname> from a parameter
   * "System.X_FREEACS-COM.TR069Script.<ScriptName>.Version"
   */
  public static String getTR069ScriptName(String parameterName) {
    int startPos = DESIRED_TR069_SCRIPT.length();
    int endPos = parameterName.lastIndexOf(TR069ScriptType.Version.toString());
    return parameterName.substring(startPos, endPos - 1);
  }
}
