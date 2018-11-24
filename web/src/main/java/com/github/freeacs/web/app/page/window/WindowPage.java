package com.github.freeacs.web.app.page.window;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.ProfileParameter;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.UnitParameter;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.dbi.UnittypeParameterFlag;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.web.Page;
import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.InputDataIntegrity;
import com.github.freeacs.web.app.input.InputDataRetriever;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.util.ACSLoader;
import com.github.freeacs.web.app.util.SessionCache;
import com.github.freeacs.web.app.util.WebConstants;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

/**
 * This class handles the service window for the two use cases: unit, profile.
 *
 * @author Jarl Andre Hubenthal
 */
public class WindowPage extends AbstractWebPage {
  /** The input data. */
  private WindowData inputData;

  /** The xaps. */
  private ACS acs;

  /** The xaps unit. */
  private ACSUnit acsUnit;

  /** The Constant ServiceWindowDownload. */
  private static final String ServiceWindowDownload = SystemParameters.SERVICE_WINDOW_DISRUPTIVE;

  /** The Constant ServiceWindowFrequency. */
  private static final String ServiceWindowFrequency = SystemParameters.SERVICE_WINDOW_FREQUENCY;

  /** The Constant ServiceWindowRegular. */
  private static final String ServiceWindowRegular = SystemParameters.SERVICE_WINDOW_REGULAR;

  /** The Constant ServiceWindowSpread. */
  private static final String ServiceWindowSpread = SystemParameters.SERVICE_WINDOW_SPREAD;

  /** The unit. */
  private Unit unit;

  /** The profile. */
  private Profile profile;

  /** The unittype. */
  private Unittype unittype;

  /** The session id. */
  private String sessionId;

  /** The frequency. */
  private String frequency;

  /** The spread. */
  private String spread;

  /** The unit download. */
  private UnitParameter unitDownload;

  /** The unit regular. */
  private UnitParameter unitRegular;

  /** The unit frequency. */
  private UnitParameter unitFrequency;

  /** The unit spread. */
  private UnitParameter unitSpread;

  /** The profile download. */
  private ProfileParameter profileDownload;

  /** The profile frequency. */
  private ProfileParameter profileFrequency;

  /** The profile regular. */
  private ProfileParameter profileRegular;

  /** The profile spread. */
  private ProfileParameter profileSpread;

  /** The Constant days. */
  private static final String[] days = {"mo", "tu", "we", "th", "fr", "sa", "su"};

  /** The Constant hours. */
  private static final String[] hours = {
    "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15",
    "16", "17", "18", "19", "20", "21", "23", "24"
  };

  /** The Constant minutes. */
  private static final String[] minutes = {"00", "15", "30", "45"};

  /** The hour minute strings. */
  private static String[] hourMinuteStrings = new String[hours.length * minutes.length];

  /** The Constant submitText. */
  private static final String submitText = "Update service window";

  /** The window page. */
  private String windowPage;

  /** The fromday download. */
  private String fromdayDownload;

  /** The today download. */
  private String todayDownload;

  /** The fromhour download. */
  private String fromhourDownload;

  /** The fromday regular. */
  private String fromdayRegular;

  /** The tohour download. */
  private String tohourDownload;

  /** The today regular. */
  private String todayRegular;

  /** The fromhour regular. */
  private String fromhourRegular;

  /** The tohour regular. */
  private String tohourRegular;

  static {
    int count = 0;
    for (String hour : hours) {
      if ("24".equals(hour)) {
        hourMinuteStrings[count] = hour + "00";
        count++;
        continue;
      }
      for (String minute : minutes) {
        hourMinuteStrings[count] = hour + minute;
        count++;
      }
    }
    hourMinuteStrings = Arrays.asList(hourMinuteStrings).subList(0, count).toArray(new String[] {});
  }

  public void process(
      ParameterParser params,
      Output outputHandler,
      DataSource xapsDataSource,
      DataSource syslogDataSource)
      throws Exception {
    Map<String, Object> root = outputHandler.getTemplateMap();

    inputData = (WindowData) InputDataRetriever.parseInto(new WindowData(), params);

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

    frequency = params.getParameter("frequency");
    spread = params.getParameter("spread");
    fromdayDownload = params.getParameter("fromday::download");
    todayDownload = params.getParameter("today::download");
    fromhourDownload = params.getParameter("fromhour::download");
    tohourDownload = params.getParameter("tohour::download");
    fromdayRegular = params.getParameter("fromday::regular");
    todayRegular = params.getParameter("today::regular");
    fromhourRegular = params.getParameter("fromhour::regular");
    tohourRegular = params.getParameter("tohour::regular");

    if (inputData.getPage().startsWith("profile")
        && inputData.getUnittype().getString() != null
        && inputData.getProfile().getString() != null) {
      unittype = acs.getUnittype(inputData.getUnittype().getString());
      profile = unittype.getProfiles().getByName(inputData.getProfile().getString());
      if (profile != null) {
        profileDownload = profile.getProfileParameters().getByName(ServiceWindowDownload);
        if (profileDownload != null && profileDownload.getValue() != null) {
          updateDownloadFields(profileDownload.getValue());
        }
        profileRegular = profile.getProfileParameters().getByName(ServiceWindowRegular);
        if (profileRegular != null && profileRegular.getValue() != null) {
          updateRegularFields(profileRegular.getValue());
        }
        profileFrequency = profile.getProfileParameters().getByName(ServiceWindowFrequency);
        if (frequency == null && profileFrequency != null && profileFrequency.getValue() != null) {
          frequency = profileFrequency.getValue();
        }
        profileSpread = profile.getProfileParameters().getByName(ServiceWindowSpread);
        if (spread == null && profileSpread != null && profileSpread.getValue() != null) {
          spread = profileSpread.getValue();
        }
      }
    } else if (inputData.getPage().startsWith("unit") && inputData.getUnit().getString() != null) {
      acsUnit = ACSLoader.getACSUnit(sessionId, xapsDataSource, syslogDataSource);
      unit = acsUnit.getUnitById(inputData.getUnit().getString());
      if (unit != null) {
        unitDownload = unit.getUnitParameters().get(ServiceWindowDownload);
        if (unitDownload != null && unitDownload.getValue() != null) {
          updateDownloadFields(unitDownload.getValue());
        }
        unitRegular = unit.getUnitParameters().get(ServiceWindowRegular);
        if (unitRegular != null && unitRegular.getValue() != null) {
          updateRegularFields(unitRegular.getValue());
        }
        unitFrequency = unit.getUnitParameters().get(ServiceWindowFrequency);
        if (frequency == null && unitFrequency != null && unitFrequency.getValue() != null) {
          frequency = unitFrequency.getValue();
        }
        unitSpread = unit.getUnitParameters().get(ServiceWindowSpread);
        if (spread == null && unitSpread != null && unitSpread.getValue() != null) {
          spread = unitSpread.getValue();
        }
      }
    }

    if (frequency != null && (!isNumber(frequency) || frequency.startsWith("-"))) {
      frequency = null;
    }
    if (spread != null && (!isNumber(spread) || spread.startsWith("-"))) {
      spread = null;
    }

    if (inputData.getPage().hasValue("profilewindow") && unittype != null && profile != null) {
      windowPage = "profile";
    } else if (inputData.getPage().hasValue("unitwindow") && unit != null) {
      windowPage = "unit";
    } else {
      outputHandler.setDirectToPage(Page.SEARCH);
      return;
    }

    try {
      root.put("message", action());
    } catch (Exception e) {
      root.put("error", e.getLocalizedMessage());
    }

    root.put("unittype", unittype);
    root.put("profile", profile);
    root.put("unit", unit);

    root.put("windowPage", windowPage);

    Map<String, Object> windowMap = new HashMap<>();
    windowMap.put("days", days);
    windowMap.put("hours", hourMinuteStrings);
    windowMap.put("button", submitText);
    root.put("window", windowMap);

    root.put("frequency", frequency);
    root.put("spread", spread);
    root.put("fromdayDownload", fromdayDownload);
    root.put("todayDownload", todayDownload);
    root.put("fromhourDownload", fromhourDownload);
    root.put("tohourDownload", tohourDownload);
    root.put("fromdayRegular", fromdayRegular);
    root.put("todayRegular", todayRegular);
    root.put("fromhourRegular", fromhourRegular);
    root.put("tohourRegular", tohourRegular);

    outputHandler.setTemplatePath("/window/edit.ftl");
  }

  /**
   * Update regular fields.
   *
   * @param value the value
   */
  private void updateRegularFields(String value) {
    String[] args = value.split(":");
    if (args.length == 2) {
      String[] daySpan = args[0].split("-");
      if (daySpan.length == 2) {
        if (fromdayRegular == null) {
          fromdayRegular = daySpan[0];
          todayRegular = daySpan[1];
        }
      } else if (daySpan.length == 1 && fromdayRegular == null) {
        fromdayRegular = daySpan[0];
      }
      String[] hourSpan = args[1].split("-");
      if (hourSpan.length == 2 && fromhourRegular == null) {
        fromhourRegular = hourSpan[0];
        tohourRegular = hourSpan[1];
      }
    }
  }

  /**
   * Update download fields.
   *
   * @param value the value
   */
  private void updateDownloadFields(String value) {
    String[] args = value.split(":");
    if (args.length == 2) {
      String[] daySpan = args[0].split("-");
      if (daySpan.length == 2) {
        if (fromdayDownload == null) {
          fromdayDownload = daySpan[0];
          todayDownload = daySpan[1];
        }
      } else if (daySpan.length == 1 && fromdayDownload == null) {
        fromdayDownload = daySpan[0];
      }
      String[] hourSpan = args[1].split("-");
      if (hourSpan.length == 2 && fromhourDownload == null) {
        fromhourDownload = hourSpan[0];
        tohourDownload = hourSpan[1];
      }
    }
  }

  /**
   * Action.
   *
   * @return the string
   * @throws SQLException the sQL exception the no available connection exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws IllegalArgumentException the illegal argument exception
   * @throws SecurityException the security exception
   * @throws IllegalAccessException the illegal access exception
   * @throws InvocationTargetException the invocation target exception
   * @throws NoSuchMethodException the no such method exception
   */
  private String action()
      throws SQLException, IOException, IllegalAccessException, InvocationTargetException,
          NoSuchMethodException {
    if (inputData.getFormSubmit().hasValue(submitText)) {
      String download = getDownload();
      String regular = getRegular();

      if (unittype != null && profile != null) {
        boolean updated = false;

        if (download == null && profileDownload != null) {
          profile.getProfileParameters().deleteProfileParameter(profileDownload, acs);
          updated = true;
        } else if (profileDownload == null && download != null) {
          UnittypeParameter utp = unittype.getUnittypeParameters().getByName(ServiceWindowDownload);
          if (utp == null) {
            UnittypeParameterFlag flag = new UnittypeParameterFlag("X");
            utp = new UnittypeParameter(unittype, ServiceWindowDownload, flag);
            unittype.getUnittypeParameters().addOrChangeUnittypeParameter(utp, acs);
          }
          profileDownload = new ProfileParameter(profile, utp, getDownload());
          profile.getProfileParameters().addOrChangeProfileParameter(profileDownload, acs);
          updated = true;
        } else if (download != null) {
          profileDownload.setValue(getDownload());
          profile.getProfileParameters().addOrChangeProfileParameter(profileDownload, acs);
          updated = true;
        }

        if (regular == null && profileRegular != null) {
          profile.getProfileParameters().deleteProfileParameter(profileRegular, acs);
          updated = true;
        } else if (profileRegular == null && regular != null) {
          UnittypeParameter utp = unittype.getUnittypeParameters().getByName(ServiceWindowRegular);
          if (utp == null) {
            UnittypeParameterFlag flag = new UnittypeParameterFlag("X");
            utp = new UnittypeParameter(unittype, ServiceWindowRegular, flag);
            unittype.getUnittypeParameters().addOrChangeUnittypeParameter(utp, acs);
          }
          profileRegular = new ProfileParameter(profile, utp, getRegular());
          profile.getProfileParameters().addOrChangeProfileParameter(profileRegular, acs);
          updated = true;
        } else if (regular != null) {
          profileRegular.setValue(getRegular());
          profile.getProfileParameters().addOrChangeProfileParameter(profileRegular, acs);
          updated = true;
        }

        if (frequency == null && profileFrequency != null) {
          profile.getProfileParameters().deleteProfileParameter(profileFrequency, acs);
          updated = true;
        } else if (profileFrequency == null && frequency != null) {
          UnittypeParameter utp =
              unittype.getUnittypeParameters().getByName(ServiceWindowFrequency);
          if (utp == null) {
            UnittypeParameterFlag flag = new UnittypeParameterFlag("X");
            utp = new UnittypeParameter(unittype, ServiceWindowFrequency, flag);
            unittype.getUnittypeParameters().addOrChangeUnittypeParameter(utp, acs);
          }
          profileFrequency = new ProfileParameter(profile, utp, frequency);
          profile.getProfileParameters().addOrChangeProfileParameter(profileFrequency, acs);
          updated = true;
        } else if (frequency != null) {
          profileFrequency.setValue(frequency);
          profile.getProfileParameters().addOrChangeProfileParameter(profileFrequency, acs);
          updated = true;
        }

        if (spread == null && profileSpread != null) {
          profile.getProfileParameters().deleteProfileParameter(profileSpread, acs);
          updated = true;
        } else if (profileSpread == null && spread != null) {
          UnittypeParameter utp = unittype.getUnittypeParameters().getByName(ServiceWindowSpread);
          if (utp == null) {
            UnittypeParameterFlag flag = new UnittypeParameterFlag("X");
            utp = new UnittypeParameter(unittype, ServiceWindowSpread, flag);
            unittype.getUnittypeParameters().addOrChangeUnittypeParameter(utp, acs);
          }
          profileSpread = new ProfileParameter(profile, utp, spread);
          profile.getProfileParameters().addOrChangeProfileParameter(profileSpread, acs);
          updated = true;
        } else if (spread != null) {
          profileSpread.setValue(spread);
          profile.getProfileParameters().addOrChangeProfileParameter(profileSpread, acs);
          updated = true;
        }

        if (updated) {
          return "Service Window for profile was updated";
        }
      } else if (unit != null) {
        unittype = acs.getUnittype(unit.getUnittype().getId());
        Profile p = unittype.getProfiles().getById(unit.getProfile().getId());
        boolean updated = false;
        if (download == null && unitDownload != null) {
          acsUnit.deleteUnitParameters(Arrays.asList(new UnitParameter[] {unitDownload}));
          updated = true;
        } else if (unitDownload == null && download != null) {
          UnittypeParameter utp = unittype.getUnittypeParameters().getByName(ServiceWindowDownload);
          if (utp == null) {
            UnittypeParameterFlag flag = new UnittypeParameterFlag("X");
            utp = new UnittypeParameter(unittype, ServiceWindowDownload, flag);
            unittype.getUnittypeParameters().addOrChangeUnittypeParameter(utp, acs);
          }
          unitDownload = new UnitParameter(utp, unit.getId(), getDownload(), p);
          List<UnitParameter> toAdd = new ArrayList<>();
          toAdd.add(unitDownload);
          acsUnit.addOrChangeUnitParameters(toAdd, p);
          updated = true;
        } else if (download != null) {
          unitDownload.getParameter().setValue(getDownload());
          List<UnitParameter> toAdd = new ArrayList<>();
          toAdd.add(unitDownload);
          acsUnit.addOrChangeUnitParameters(toAdd, p);
          updated = true;
        }

        if (regular == null && unitRegular != null) {
          acsUnit.deleteUnitParameters(Arrays.asList(new UnitParameter[] {unitRegular}));
          updated = true;
        } else if (unitRegular == null && regular != null) {
          UnittypeParameter utp = unittype.getUnittypeParameters().getByName(ServiceWindowRegular);
          if (utp == null) {
            UnittypeParameterFlag flag = new UnittypeParameterFlag("X");
            utp = new UnittypeParameter(unittype, ServiceWindowRegular, flag);
            unittype.getUnittypeParameters().addOrChangeUnittypeParameter(utp, acs);
          }
          unitRegular = new UnitParameter(utp, unit.getId(), getRegular(), p);
          List<UnitParameter> toAdd = new ArrayList<>();
          toAdd.add(unitRegular);
          acsUnit.addOrChangeUnitParameters(toAdd, p);
          updated = true;
        } else if (regular != null) {
          unitRegular.getParameter().setValue(getRegular());
          List<UnitParameter> toAdd = new ArrayList<>();
          toAdd.add(unitRegular);
          acsUnit.addOrChangeUnitParameters(toAdd, p);
          updated = true;
        }

        if (frequency == null && unitFrequency != null) {
          acsUnit.deleteUnitParameters(Arrays.asList(new UnitParameter[] {unitFrequency}));
          updated = true;
        } else if (unitFrequency == null && frequency != null) {
          UnittypeParameter utp =
              unittype.getUnittypeParameters().getByName(ServiceWindowFrequency);
          if (utp == null) {
            UnittypeParameterFlag flag = new UnittypeParameterFlag("X");
            utp = new UnittypeParameter(unittype, ServiceWindowFrequency, flag);
            unittype.getUnittypeParameters().addOrChangeUnittypeParameter(utp, acs);
          }
          unitFrequency = new UnitParameter(utp, unit.getId(), frequency, p);
          List<UnitParameter> toAdd = new ArrayList<>();
          toAdd.add(unitFrequency);
          acsUnit.addOrChangeUnitParameters(toAdd, p);
          updated = true;
        } else if (frequency != null) {
          unitFrequency.getParameter().setValue(frequency);
          List<UnitParameter> toAdd = new ArrayList<>();
          toAdd.add(unitFrequency);
          acsUnit.addOrChangeUnitParameters(toAdd, p);
          updated = true;
        }

        if (spread == null && unitSpread != null) {
          acsUnit.deleteUnitParameters(Arrays.asList(new UnitParameter[] {unitSpread}));
          updated = true;
        } else if (unitSpread == null && spread != null) {
          UnittypeParameter utp = unittype.getUnittypeParameters().getByName(ServiceWindowSpread);
          if (utp == null) {
            UnittypeParameterFlag flag = new UnittypeParameterFlag("X");
            utp = new UnittypeParameter(unittype, ServiceWindowSpread, flag);
            unittype.getUnittypeParameters().addOrChangeUnittypeParameter(utp, acs);
          }
          unitSpread = new UnitParameter(utp, unit.getId(), spread, p);
          List<UnitParameter> toAdd = new ArrayList<>();
          toAdd.add(unitSpread);
          acsUnit.addOrChangeUnitParameters(toAdd, p);
          updated = true;
        } else if (spread != null) {
          unitSpread.getParameter().setValue(spread);
          List<UnitParameter> toAdd = new ArrayList<>();
          toAdd.add(unitSpread);
          acsUnit.addOrChangeUnitParameters(toAdd, p);
          updated = true;
        }

        if (updated) {
          return "Service Window for unit was updated";
        }
      }
    }
    return null;
  }

  /**
   * Gets the regular.
   *
   * @return the regular
   */
  private String getRegular() {
    StringBuilder string = new StringBuilder();
    if (isValidString(fromdayRegular)
        && isValidString(fromhourRegular)
        && isValidString(tohourRegular)) {
      string.append(fromdayRegular);
      if (isValidString(todayRegular)) {
        string.append("-").append(todayRegular);
      } else {
        todayRegular = null;
      }
      string.append(":");
      string.append(fromhourRegular).append("-").append(tohourRegular);
    } else {
      fromdayRegular = null;
      todayRegular = null;
      tohourRegular = null;
      fromhourRegular = null;
    }
    if (string.toString().isEmpty()) {
      return null;
    }
    return string.toString();
  }

  /**
   * Gets the download.
   *
   * @return the download
   */
  private String getDownload() {
    StringBuilder string = new StringBuilder();
    if (isValidString(fromdayDownload)
        && isValidString(fromhourDownload)
        && isValidString(tohourDownload)) {
      string.append(fromdayDownload);
      if (isValidString(todayDownload)) {
        string.append("-").append(todayDownload);
      } else {
        todayDownload = null;
      }
      string.append(":");
      string.append(fromhourDownload).append("-").append(tohourDownload);
    } else {
      fromdayDownload = null;
      todayDownload = null;
      fromhourDownload = null;
      tohourDownload = null;
    }
    if (string.toString().isEmpty()) {
      return null;
    }
    return string.toString();
  }
}
