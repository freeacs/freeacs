package com.github.freeacs.web.app.page.unit;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.File;
import com.github.freeacs.dbi.FileType;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.ProfileParameter;
import com.github.freeacs.dbi.Syslog;
import com.github.freeacs.dbi.SyslogConstants;
import com.github.freeacs.dbi.SyslogEntry;
import com.github.freeacs.dbi.SyslogFilter;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.UnitParameter;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.dbi.util.ProvisioningMode;
import com.github.freeacs.dbi.util.SystemConstants;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.web.Page;
import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.DropDownSingleSelect;
import com.github.freeacs.web.app.input.InputDataIntegrity;
import com.github.freeacs.web.app.input.InputDataRetriever;
import com.github.freeacs.web.app.input.InputSelectionFactory;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.menu.MenuItem;
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.page.syslog.SyslogUtil;
import com.github.freeacs.web.app.page.unittype.UnittypeParameterFlags;
import com.github.freeacs.web.app.page.unittype.UnittypeParameterTypes;
import com.github.freeacs.web.app.table.TableElementMaker;
import com.github.freeacs.web.app.util.ACSLoader;
import com.github.freeacs.web.app.util.SessionCache;
import com.github.freeacs.web.app.util.SessionData;
import com.github.freeacs.web.app.util.TimeFormatter;
import com.github.freeacs.web.app.util.WebConstants;
import com.github.freeacs.web.app.util.WebProperties;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The Class UnitPage. */
public class UnitPage extends AbstractWebPage {
  private static final Logger logger = LoggerFactory.getLogger(UnitPage.class);
  private static final Logger accessLogger = LoggerFactory.getLogger("Access");
  private static DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private static Pattern paramPattern = Pattern.compile("(\\$\\{([^\\}]+)\\})");

  private ACS acs;
  private ACSUnit acsUnit;
  private Unit unit;
  private Profile profile;
  private Unittype unittype;
  private String sessionId;
  private UnitData inputData;
  private String lastConnectTms;
  private String lastConnectDiff;
  private String nextConnectTms;
  private String nextConnectDiff;
  private boolean lateConnect;
  private boolean confidentialsRestricted = WebProperties.getInstance().isConfidentialsRestricted();

  public String getTitle(String page) {
    return super.getTitle(page)
        + (unit != null
            ? " | "
                + unit.getId()
                + " | "
                + unit.getProfile().getName()
                + " | "
                + unit.getProfile().getUnittype().getName()
            : "");
  }

  public List<MenuItem> getShortcutItems(SessionData sessionData) {
    List<MenuItem> list = new ArrayList<MenuItem>();
    list.addAll(super.getShortcutItems(sessionData));
    list.add(new MenuItem("Create new Unit", Page.UNIT).addCommand("create"));
    if (unit != null) {
      list.add(
          new MenuItem("Initiate Provisioning", Page.UNIT)
              .addParameter("unit", unit.getId())
              .addParameter("init_provisioning", "true"));
      list.add(
          new MenuItem("Read all from device", Page.UNIT)
              .addParameter("unit", unit.getId())
              .addParameter("init_readall", "true"));
      if (confidentialsRestricted)
        list.add(
            new MenuItem("Show confidentials", Page.UNIT)
                .addParameter("unit", unit.getId())
                .addParameter("show_confidential", "true"));
      list.add(
          new MenuItem("Last 100 syslog entries", Page.SYSLOG)
              .addCommand("auto") // automatically hit the Search button
              .addParameter("unittype", unit.getUnittype().getName())
              .addParameter("profile", unit.getProfile().getName())
              .addParameter("unit", unit.getId()));
      list.add(
          new MenuItem("Service window", Page.WINDOWUNIT)
              .addParameter("unittype", unit.getUnittype().getName())
              .addParameter("profile", unit.getProfile().getName())
              .addParameter("unit", unit.getId()));
      list.add(
          new MenuItem("Upgrade unit", Page.UPGRADE)
              .addParameter("type", "Unit")
              .addParameter("unittype", unit.getUnittype().getName())
              .addParameter("profile", unit.getProfile().getName())
              .addParameter("unit", unit.getId()));
      list.add(
          new MenuItem("Unit history", Page.UNITSTATUS)
              .addParameter("current", "false")
              .addParameter("history", "true")
              .addParameter("unittype", unit.getUnittype().getName())
              .addParameter("profile", unit.getProfile().getName())
              .addParameter("unit", unit.getId()));
      list.add(
          new MenuItem("Unit dashboard", Page.UNITSTATUS)
              .addParameter("current", "true")
              .addParameter("history", "false")
              .addParameter("unittype", unit.getUnittype().getName())
              .addParameter("profile", unit.getProfile().getName())
              .addParameter("unit", unit.getId()));
    }
    return list;
  }

  // Method for (C)reating (U)pdating and (D)eleting Unit parameters
  /**
   * Action cud parameters.
   *
   * @param req the req
   * @throws Exception the exception
   */
  private void actionCUDParameters(ParameterParser req) throws Exception {
    UnittypeParameter[] utParams = unittype.getUnittypeParameters().getUnittypeParameters();
    Map<String, UnitParameter> uParams = unit.getUnitParameters();

    List<UnitParameter> upDeleteList = new ArrayList<UnitParameter>();
    List<UnitParameter> upUpdateList = new ArrayList<UnitParameter>();

    for (UnittypeParameter utp : utParams) {
      String utpName = utp.getName();
      UnitParameter up = uParams.get(utpName);
      String upValue = null;
      if (up != null) upValue = up.getParameter().getValue();
      if (up != null && req.getParameter("delete::" + utpName) != null) {
        upDeleteList.add(up);
      } else if (up == null && req.getParameter("create::" + utpName) != null) {
        String newValue = req.getParameter("update::" + utpName);
        if (newValue != null) newValue = removeFromStart(newValue, '!');
        up = new UnitParameter(utp, unit.getId(), newValue, profile);
        upUpdateList.add(up);
      } else if (up != null && req.getParameter("update::" + utpName) != null) {
        String updatedValue = req.getParameter("update::" + utpName);
        if (updatedValue != null && updatedValue.equals(upValue)) continue;
        if (updatedValue != null) updatedValue = removeFromStart(updatedValue, '!');
        up.getParameter().setValue(updatedValue);
        upUpdateList.add(up);
      }
    }

    if (upDeleteList.size() > 0) acsUnit.deleteUnitParameters(upDeleteList);
    if (upUpdateList.size() > 0) acsUnit.addOrChangeUnitParameters(upUpdateList, profile);
  }

  /**
   * Action move unit.
   *
   * @param req the req
   * @throws Exception the exception
   */
  private void actionMoveUnit(ParameterParser req) throws Exception {
    Unittype unittype = acs.getUnittype(unit.getUnittype().getId());
    String newProfileStr = req.getParameter("profile");
    Profile newProfile = acs.getProfile(unittype.getName(), newProfileStr);
    Profile oldProfile = acs.getProfile(unit.getProfile().getId());
    if (!oldProfile.getName().equals(newProfile.getName())) {
      List<String> unitList = new ArrayList<String>();
      unitList.add(unit.getId());
      acsUnit.moveUnits(unitList, newProfile);
      unit = acsUnit.getUnitById(unit.getId());
      profile = unit.getProfile();
    }
  }

  /**
   * Needs an updated dbi.
   *
   * @param req the req
   * @throws Exception the exception
   */
  @SuppressWarnings("unused")
  private void actionMoveUnitToAnotherUnitType(ParameterParser req) throws Exception {
    Unittype oldUnittype = unit.getUnittype();
    String newProfileStr = req.getParameter("profile");
    String newUnitTypeString = req.getParameter("unittype");
    Unittype newUnittype = acs.getUnittype(newUnitTypeString);
    Profile newProfile = acs.getProfile(newUnittype.getName(), newProfileStr);
    boolean sameProtocol =
        oldUnittype.getProtocol() != null
            && newUnittype != null
            && oldUnittype.getProtocol().equals(newUnittype.getProtocol());
    boolean sameModel =
        oldUnittype.getName().length() > 7
            && newUnittype != null
            && newUnittype.getName().length() > 7
            && oldUnittype.getName().substring(0, 7).equals(newUnittype.getName().substring(0, 7));
    if (sameProtocol && sameModel) {
      // unit.setUnittype(newUnittype);
      // unit.setProfile(newProfile);
      List<String> unitList = new ArrayList<String>();
      unitList.add(unit.getId());
      acsUnit.moveUnits(unitList, newProfile);
      unit = acsUnit.getUnitById(unit.getId());
      profile = unit.getProfile();
    }
  }

  /**
   * This method will initiate one of the following actions:
   *
   * <p>- provisioning (without any special changes) - provisioning in READALL mode (read all params
   * from device) - reboot device - reset device - change frequency/spread - upgrade fw
   *
   * <p>In any case and in the end - the method will issue a kick-order to the STUN-server, to
   * execute changes or a provisioning immediately. This kick-request may or may not work, depending
   * on the information available in the unit (and if it supports TR-111). The result of the kick
   * will be read back into the IM.Message-parameter (sent from the STUN-server). To avoid reloading
   * the page a number of times, this method will sleep until some changes or error message have
   * been detected.
   */
  private void actionKickAndExecute(Map<String, Object> root) throws Exception {

    ProvisioningMode mode = null;
    boolean publish = false;

    /* If we're in REGULAR mode, but is told to read all,
     *  put us in READALL mode and publish. */
    if (isRegularMode() && inputData.getInitReadAll().getValue() != null) {
      publish = true;
      mode = ProvisioningMode.READALL;
    }

    /* If we're to initiate provisioning, set mode to REGULAR and publish. */
    else if (inputData.getInitProvisioning().getValue() != null) {
      publish = true;
      mode = ProvisioningMode.REGULAR;
    }

    /* Set restart-flag and initiate provisioning in REGULAR mode */
    else if (inputData.getInitRestart().getValue() != null) {
      publish = true;
      mode = ProvisioningMode.REGULAR;
      unit.toWriteQueue(SystemParameters.RESTART, "1");
    }

    /* Set reset-flag and initiate provisioning in REGULAR mode */
    else if (inputData.getInitReset().getValue() != null) {
      publish = true;
      mode = ProvisioningMode.REGULAR;
      unit.toWriteQueue(SystemParameters.RESET, "1");
    }

    /* Change frequency/spread in REGULAR mode */
    else if (inputData.getChangeFreqSpread().getValue() != null) {
      publish = true;
      mode = ProvisioningMode.REGULAR;
      try {
        int freq = Integer.parseInt(inputData.getFrequency().getString());
        String freqStr =
            "" + (freq < 1 ? 1 : (freq > 20000 ? 20000 : freq)); // limit freq between 1 and 20000
        unit.toWriteQueue(SystemParameters.SERVICE_WINDOW_FREQUENCY, freqStr);
        root.put("frequency", freqStr);
      } catch (NumberFormatException nfe) {
        unit.toWriteQueue(
            SystemParameters.SERVICE_WINDOW_FREQUENCY,
            SystemConstants.DEFAULT_SERVICEWINDOW_FREQUENCY_STR); // default value is 7
        root.put("frequency", SystemConstants.DEFAULT_SERVICEWINDOW_FREQUENCY_STR);
      }
      try {
        int spread = Integer.parseInt(inputData.getSpread().getString());
        String spreadStr =
            "" + (spread < 0 ? 0 : (spread > 100 ? 100 : spread)); // limit spread between 0 and 100
        unit.toWriteQueue(SystemParameters.SERVICE_WINDOW_SPREAD, spreadStr);
        root.put("spread", spreadStr);
      } catch (NumberFormatException nfe) {
        unit.toWriteQueue(
            SystemParameters.SERVICE_WINDOW_SPREAD,
            SystemConstants.DEFAULT_SERVICEWINDOW_SPREAD_STR); // default value is 0 spread
        root.put("spread", SystemConstants.DEFAULT_SERVICEWINDOW_SPREAD_STR);
      }
    } else if (inputData.getUnitUpgrade().getString() != null
        && inputData.getUnitUpgrade().getString().endsWith("Upgrade")) {
      publish = true;
      mode = ProvisioningMode.REGULAR;
      /* Ett horribelt jävla fulhack här, men det får duga tills vidare.
       * Problemet är att "getUnitUpgrade().getString()" returnerar filnamnet
       * konkatenerat med texten på upgrade-knappen, separerat med komma.
       * Tänker inte gräva ned mig i input-systemet tillräckligt djupt för
       * att hitta "rätt" sätt att få ut innehållet...
       */
      String[] parts = inputData.getUnitUpgrade().getString().split(",");
      unit.toWriteQueue(SystemParameters.DESIRED_SOFTWARE_VERSION, parts[0]);
    }

    if (mode != null) {
      /* Initiate the kick and Wait for changes... */
      unit.toWriteQueue(SystemParameters.PROVISIONING_MODE, mode.toString());
      acsUnit.addOrChangeUnitParameters(unit.flushWriteQueue(), unit.getProfile());
      publishInspectionMode(unit, sessionId);
      waitForStunServer(root, mode, unit);
    }
  }

  /** Code to hang around and see if unit is updated automatically through kick */
  private void waitForStunServer(Map<String, Object> root, ProvisioningMode mode, Unit unit) {
    String lct = unit.getParameterValue(SystemParameters.LAST_CONNECT_TMS);
    String initialKickResponse = unit.getParameterValue(SystemParameters.INSPECTION_MESSAGE);
    if (initialKickResponse == null) {
      initialKickResponse = SystemConstants.DEFAULT_INSPECTION_MESSAGE;
    }
    String currentKickResponse = initialKickResponse;
    int waitSec = 30;
    if (mode == ProvisioningMode.READALL) waitSec = 60;
    int secCount = 0;
    while (secCount < waitSec) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException ignored) {
        // ignore this exception
      }
      Unit possiblyUpdatedUnit;
      try {
        possiblyUpdatedUnit = acsUnit.getUnitById(unit.getId());
        currentKickResponse =
            possiblyUpdatedUnit.getParameterValue(SystemParameters.INSPECTION_MESSAGE);
        if (!initialKickResponse.equals(currentKickResponse)
            && currentKickResponse != null
            && !currentKickResponse.contains("success")) break; // if kick failed - fail fast
        if (lct != null
            && !lct.equals(
                possiblyUpdatedUnit.getParameterValue(SystemParameters.LAST_CONNECT_TMS))) {
          break;
        }
      } catch (SQLException e) {
        logger.error("Failed to retrieve unit " + unit.getId(), e);
        break;
      } finally {
        secCount++;
      }
    }
    boolean timedOut = secCount == waitSec;
    boolean success = currentKickResponse != null && currentKickResponse.contains("success");
    addKickMessages(root, currentKickResponse, timedOut, success);
  }

  private void addKickMessages(
      Map<String, Object> root, String currentKickResponse, boolean timedOut, boolean success) {
    if (timedOut) { // Timed out - very likely that nothing happened
      root.put("kick_message", "Reboot to initate provisioning");
      root.put("kick_mouseover", "Kick response: " + currentKickResponse);
    } else {
      if (success) { // LCT is updated - a successful kick
        try {
          Thread.sleep(5000); // to allow syslog to be updated assuming kick was successful
        } catch (InterruptedException ignored) {
          // ignore this exception
        }
        root.put("kick_message", "Provisioning was initiated");
        root.put("kick_mouseover", "Kick response: " + currentKickResponse);
      } else { // LCT may or may not be updated, but kick response contains error
        root.put("kick_message", "Reboot to initate provisioning");
        root.put("kick_mouseover", "Kick response: " + currentKickResponse);
      }
    }
  }

  /**
   * Publish inspection mode.
   *
   * @param unit the unit
   * @param sessionId the session id
   */
  public static void publishInspectionMode(Unit unit, String sessionId) {
    DBI dbi = SessionCache.getDBI(sessionId);
    if (dbi != null) {
      dbi.publishKick(unit, SyslogConstants.FACILITY_STUN);
    }
  }

  private boolean isRegularMode() {
    if (unit != null) return unit.getProvisioningMode() == ProvisioningMode.REGULAR;
    return false;
  }

  private boolean displayProvisioningTimestamps() {
    String lctms = unit.getParameterValue(SystemParameters.LAST_CONNECT_TMS);
    String pint = unit.getParameterValue(SystemParameters.PERIODIC_INTERVAL);
    Date then = null;
    Date next, now = new Date();
    try {
      then = dateFormatter.parse(lctms);
      lastConnectDiff =
          "(" + TimeFormatter.convertMs2ApproxTimeString(now.getTime() - then.getTime()) + " ago)";
    } catch (Exception ignore) {
      lastConnectDiff = "(Unknown)";
    }
    try {
      long seconds = Integer.parseInt(pint);
      next = new Date(then.getTime() + seconds * 1000);
      nextConnectTms = dateFormatter.format(next);
      if (now.getTime() > next.getTime()) {
        lateConnect = true;
        nextConnectDiff =
            "("
                + TimeFormatter.convertMs2ApproxTimeString(now.getTime() - next.getTime())
                + " ago)";
      } else {
        lateConnect = false;
        nextConnectDiff =
            "(in " + TimeFormatter.convertMs2ApproxTimeString(next.getTime() - now.getTime()) + ")";
      }
    } catch (Exception ignore) {
      nextConnectDiff = "(Unknown)";
    }

    if (lctms != null && !lctms.equals(lastConnectTms)) {
      lastConnectTms = lctms;
      return true;
    }
    return false;
  }

  /**
   * Action delete unit.
   *
   * @throws Exception the exception
   */
  private void actionDeleteUnit() throws Exception {
    acsUnit.deleteUnit(unit);
    unit = null;
  }

  /**
   * Action populate.
   *
   * @throws Exception the exception
   */
  private void actionPopulate() throws Exception {
    if (inputData.getUnit().notNullNorValue("")) {
      unit = acsUnit.getUnitById(inputData.getUnit().getString());
      if (unit != null) {
        profile = unit.getProfile();
        if (inputData.getProfile().getString() == null)
          inputData.getProfile().setValue(profile.getName());
        unittype = unit.getUnittype();
        inputData.getUnittype().setValue(unittype.getName());
      }
    }

    if (unittype == null && inputData.getUnittype().getString() != null) {
      unittype = acs.getUnittype(inputData.getUnittype().getString());
    }

    if (profile == null && inputData.getProfile().getString() != null && unittype != null) {
      profile = unittype.getProfiles().getByName(inputData.getProfile().getString());
    }
  }

  public void process(
      ParameterParser params,
      Output outputHandler,
      DataSource xapsDataSource,
      DataSource syslogDataSource)
      throws Exception {
    // Get important var
    inputData = (UnitData) InputDataRetriever.parseInto(new UnitData(), params);

    sessionId = params.getSession().getId();

    acs = ACSLoader.getXAPS(sessionId, xapsDataSource, syslogDataSource);
    if (acs == null) {
      outputHandler.setRedirectTarget(WebConstants.LOGIN_URI);
      return;
    }

    InputDataIntegrity.loadAndStoreSession(
        params,
        outputHandler,
        inputData,
        inputData.getUnittype(),
        inputData.getProfile(),
        inputData.getUnit());

    acsUnit = ACSLoader.getACSUnit(sessionId, xapsDataSource, syslogDataSource);

    boolean isCreate = inputData.getCmd().isValue("create");

    Map<String, Object> root = outputHandler.getTemplateMap();

    if (isCreate) {
      // Clear unit id by setting the input value to null
      inputData.getUnit().setValue(null);

      actionPopulate();

      outputHandler.setTemplatePath("/unit/create.ftl");

      if (inputData.getFormSubmit().hasValue("Create unit")) {
        String unitId = params.getParameter("new_unit");
        if (!isValidString(unitId)) {
          root.put("error", "Please enter a unitId");
        } else if (acsUnit.getUnitById(unitId) == null) {
          List<String> unitIds = new ArrayList<String>();
          unitIds.add(unitId);
          acsUnit.addUnits(unitIds, profile);
          unit = acsUnit.getUnitById(unitId, unittype, profile);
          SessionCache.getSessionData(sessionId).setUnitId(unit.getId());
          outputHandler.setDirectToPage(Page.UNIT);
          return;
        } else {
          root.put("error", "Unit <i>" + unitId + "</i> already exists.");
        }
      }

      displayCreate(root);

    } else {
      actionPopulate();

      outputHandler.setTemplatePath("/unit/details.ftl");
      if (unit != null) {
        if (inputData.getInitRefreshPage().getValue() == null) {
          /* If we got a simple refresh, do absolutely nothing. */
          if (inputData.getFormSubmit().hasValue(WebConstants.UPDATE_PARAMS))
            actionCUDParameters(params);
          else if (inputData.getUnitMove().hasValue("Move to profile")) actionMoveUnit(params);
          else if (inputData.getUnitDelete().hasValue(WebConstants.DELETE)) {
            actionDeleteUnit();
            SessionCache.getSessionData(sessionId).setUnitId(null);
            outputHandler.setDirectToPage(Page.SEARCH);
            return;
          } else {
            actionKickAndExecute(root);
            if (inputData.getInitReadAll().getValue() != null) {
              root.put("mode_readall", true);
            }
          }
        }
        unit = acsUnit.getUnitById(unit.getId());

        displayUnit(root, xapsDataSource, syslogDataSource);

      } else if (inputData.getUnit().notNullNorValue("")) {
        root.put("unitId", inputData.getUnit().getString());
        outputHandler.setTemplatePath("/unit-status/notfound.ftl");
      } else {
        outputHandler.setDirectToPage(Page.UNIT, "cmd=create");
      }
    }
  }

  /**
   * Display create.
   *
   * @param root the root
   * @throws IllegalArgumentException the illegal argument exception
   * @throws SecurityException the security exception the no available connection exception
   * @throws SQLException the sQL exception
   * @throws IllegalAccessException the illegal access exception
   * @throws InvocationTargetException the invocation target exception
   * @throws NoSuchMethodException the no such method exception
   */
  private void displayCreate(Map<String, Object> root)
      throws IllegalArgumentException, SecurityException, SQLException, IllegalAccessException,
          InvocationTargetException, NoSuchMethodException {
    DropDownSingleSelect<Unittype> unittypeDropdown =
        InputSelectionFactory.getUnittypeSelection(inputData.getUnittype(), acs);
    root.put("unittypes", unittypeDropdown);
    root.put("unit", inputData.getUnit().getString());
    root.put(
        "profiles",
        InputSelectionFactory.getProfileSelection(
            inputData.getProfile(), inputData.getUnittype(), acs));
  }

  private void displayGUIURL(Map<String, Object> root) {
    String guiurlOrg = unit.getParameterValue(SystemParameters.GUI_URL);
    if (guiurlOrg != null) {
      root.put("natdetected", isNatDetected(unit));
      Matcher m = paramPattern.matcher(guiurlOrg);
      String guiurlMod = "";
      int previousEnd = 0;
      while (m.find()) {
        String varName = m.group(2);
        guiurlMod += guiurlOrg.substring(previousEnd, m.start());
        if (unit.getParameterValue(varName) != null) {
          guiurlMod += unit.getParameterValue(varName);
        } else {
          guiurlMod += "${PARAM-VALUE-NOT-FOUND}";
        }
        previousEnd = m.end();
      }
      if (previousEnd < guiurlOrg.length()) {
        guiurlMod += guiurlOrg.substring(previousEnd);
      }
      root.put("guiurl", guiurlMod);
    }
  }

  protected static boolean isNatDetected(final Unit unit) {
    boolean natdetected = false;
    String deviceParam = "ManagementServer.ConnectionRequestURL";
    String crequrl = unit.getParameterValue("InternetGatewayDevice." + deviceParam, false);
    if (crequrl == null) {
      crequrl = unit.getParameterValue("Device." + deviceParam, false);
    }
    String publicip = unit.getParameterValue(SystemParameters.IP_ADDRESS, false);
    if (crequrl != null && publicip != null && !crequrl.contains(publicip)) {
      natdetected = true;
    }
    return natdetected;
  }

  private void displayFrequencySpread(Map<String, Object> root) {
    int frequency = SystemConstants.DEFAULT_SERVICEWINDOW_FREQUENCY_INT;
    int spread = SystemConstants.DEFAULT_SERVICEWINDOW_SPREAD_INT;
    // Make sure root-object is populated with frequency + spread - no matter what is read from Unit
    // object or request
    // Make sure we have the latest values for frequency + spread, either from request, database or
    // defaults
    if (root.get("frequency") != null) { // set in request - see actionKickAndExecute
      try {
        frequency = Integer.parseInt((String) root.get("frequency"));
      } catch (NumberFormatException nfe) {
        // ignore - use default
      }
    } else {
      if (unit.getParameterValue(SystemParameters.SERVICE_WINDOW_FREQUENCY) != null) {
        try {
          frequency =
              Integer.parseInt(unit.getParameterValue(SystemParameters.SERVICE_WINDOW_FREQUENCY));
        } catch (NumberFormatException nfe) {
          // ignore - use default
        }
      }
      root.put("frequency", "" + frequency);
    }
    if (root.get("spread") != null) { // set in request - see actionKickAndExecute
      try {
        spread = Integer.parseInt((String) root.get("spread"));
      } catch (NumberFormatException nfe) {
        // ignore - use default
      }
    } else {
      if (unit.getParameterValue(SystemParameters.SERVICE_WINDOW_SPREAD) != null) {
        try {
          spread = Integer.parseInt(unit.getParameterValue(SystemParameters.SERVICE_WINDOW_SPREAD));
        } catch (NumberFormatException nfe) {
          // ignore - use default
        }
      }
      root.put("spread", "" + spread);
    }
    int intervalSec = 7 * 86400 / frequency;
    int spreadSec = intervalSec * spread / 100;
    root.put("frequency_interval", intervalSec + "s +/- " + spreadSec + "s");
  }

  /**
   * Decides and prepares to display the unit page.
   *
   * @param root The template map
   * @param xapsDataSource mainds
   * @param syslogDataSource syslogds
   * @throws Exception the exception
   */
  private void displayUnit(
      Map<String, Object> root, DataSource xapsDataSource, DataSource syslogDataSource)
      throws Exception {
    root.put("unit", unit);
    root.put("new_unit", inputData.getNewUnit().getString());
    // Not sure if this is necessary, could perhaps use the object instance fields
    Unittype unittype = unit.getUnittype();
    Profile profile = unit.getProfile();
    root.put("unittype", unittype.getName());
    root.put("syslogdate", SyslogUtil.getDateString());
    List<Profile> profiles =
        getAllowedProfiles(sessionId, unittype, xapsDataSource, syslogDataSource);
    root.put(
        "profiles",
        InputSelectionFactory.getDropDownSingleSelect(
            inputData.getProfile(),
            unittype.getProfiles().getByName(inputData.getProfile().getString()),
            profiles));
    UnittypeParameter[] utParams = unittype.getUnittypeParameters().getUnittypeParameters();
    UnitParameter[] uParams = unit.getUnitParameters().values().toArray(new UnitParameter[] {});
    ProfileParameter[] pParams = profile.getProfileParameters().getProfileParameters();
    UnitParameter[] uSessionParams =
        unit.getSessionParameters().values().toArray(new UnitParameter[] {});
    root.put(
        "params",
        new TableElementMaker().getParameters(utParams, uParams, pParams, uSessionParams));
    root.put("state_ready", displayProvisioningTimestamps());
    root.put("lastconnecttimestamp", lastConnectTms);
    root.put("lastconnectdiff", lastConnectDiff);
    root.put("nextconnecttimestamp", nextConnectTms);
    root.put("nextconnectdiff", nextConnectDiff);
    root.put("lateconnect", lateConnect);
    root.put("autofilter", WebProperties.getInstance().isUnitConfigAutofilter());
    String selectedFlag =
        inputData.getFilterFlag().getString() != null
            ? inputData.getFilterFlag().getString()
            : "All";
    DropDownSingleSelect<String> flags =
        InputSelectionFactory.getDropDownSingleSelect(
            inputData.getFilterFlag(), selectedFlag, UnittypeParameterFlags.toList());
    root.put("flags", flags);
    String selectedType =
        inputData.getFilterType().getString() != null
            ? inputData.getFilterType().getString()
            : (unit.getUnitParameters().values().size() > 0 ? "Configured" : "All");
    DropDownSingleSelect<String> types =
        InputSelectionFactory.getDropDownSingleSelect(
            inputData.getFilterType(), selectedType, UnittypeParameterTypes.toList());
    root.put("types", types);
    root.put(
        "string",
        inputData.getFilterString().getString() != null
            ? inputData.getFilterString().getString()
            : "");
    displayFrequencySpread(root);
    displayGUIURL(root);

    List<FileElement> fileElements = new ArrayList<>();
    // We need this "raw" list later on to check version membership
    List<File> files =
        new ArrayList<>(Arrays.asList(unittype.getFiles().getFiles(FileType.SOFTWARE)));
    // Create a list of FileElements from our filelist.
    for (File f : files) {
      fileElements.add(new FileElement(f.getVersion(), f));
    }

    // Getting the parameter values. These may be null.
    String currentSoftwareVersion = unit.getParameterValue(SystemParameters.SOFTWARE_VERSION);
    String desiredSoftwareVersion =
        unit.getParameterValue(SystemParameters.DESIRED_SOFTWARE_VERSION);

    // Attempts to retrieve the file objects. Again, these may be null.
    File currentSoftware =
        unittype.getFiles().getByVersionType(currentSoftwareVersion, FileType.SOFTWARE);
    //		File desiredSoftware = unittype.getFiles().getByVersionType(desiredSoftwareVersion,
    // FileType.SOFTWARE);

    // We need a reference to the "currently selected" element in the file elements list.
    // It should point to either 1) the currentSoftware object, or a "(unknown)" object.
    FileElement currentSoftwareElement = null;

    // In this case, the unit have never reported a version number.
    // We have no idea what software it runs.
    if (currentSoftwareVersion == null) {
      currentSoftwareElement = new FileElement("(Unknown)", null);
      fileElements.add(currentSoftwareElement);
    } else {
      // The unit reports a software version, but does it correspond to a file?
      if (currentSoftware == null) {
        // It does not. Create a "dummy" entry in the dropdown list.
        currentSoftwareElement = new FileElement(currentSoftwareVersion, null);
        fileElements.add(currentSoftwareElement);
      } else {
        // It does. We don't need to add anything to the file element list,
        // just find the correct default value.
        for (FileElement fe : fileElements) {
          if (fe.getVersion().equals(currentSoftwareVersion)) {
            currentSoftwareElement = fe;
            break;
          }
        }
      }
    }
    // We're now assured that the currentSoftwareElement is non-null.
    root.put(
        "files",
        InputSelectionFactory.getDropDownSingleSelect(
            inputData.getUnitUpgrade(), currentSoftwareElement, fileElements));
    root.put("desiredsoftware", desiredSoftwareVersion);
    root.put("currentsoftware", currentSoftwareVersion);

    Syslog syslog = acs.getSyslog();
    Date twodaysago = new Date(new Date().getTime() - 1000 * 60 * 60 * 24 * 2);
    SyslogFilter filter = new SyslogFilter();
    filter.setUnitId("^" + unit.getId() + "$");
    filter.setMessage("^ProvMsg: PP:");
    filter.setCollectorTmsStart(twodaysago);
    filter.setMaxRows(20);
    List<SyslogEntry> syslogEntries = syslog.read(filter, acs);
    List<HistoryElement> history = new ArrayList<>();
    for (SyslogEntry entry : syslogEntries) {
      try {
        HistoryElement element =
            new HistoryElement(entry.getCollectorTimestamp(), entry.getContent());
        history.add(element);
      } catch (IllegalArgumentException ex) {
        logger.warn("UnitPage failed creating HistoryElement: ", ex);
      }
    }
    root.put("history", history);
    root.put("historystarttms", dateFormatter.format(twodaysago));

    // Send the "showconfidential"-flag if necessary.
    root.put("confidentialsrestricted", confidentialsRestricted);
    if (inputData.getShowConfidential().getBoolean()) {
      accessLogger.info(
          "User \""
              + acs.getUser()
              + "\" accessed confidential "
              + "params on unit \""
              + unit.getId()
              + "\"");
      root.put("showconfidential", true);
    }
  }
}
