package com.github.freeacs.web.app.page.unittype;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.Unittype.ProvisioningProtocol;
import com.github.freeacs.dbi.UnittypeParameter;
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
import com.github.freeacs.web.app.table.TableElementMaker;
import com.github.freeacs.web.app.util.ACSLoader;
import com.github.freeacs.web.app.util.SessionCache;
import com.github.freeacs.web.app.util.SessionData;
import com.github.freeacs.web.app.util.WebConstants;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

/** The Class UnittypePage. */
public class UnittypePage extends AbstractWebPage {
  protected static final String NA_PROTOCOL = "N/A";
  protected static final String TR069_PROTOCOL = "TR-069";

  /** The input data. */
  private UnittypeData inputData;

  /** The xaps. */
  private ACS acs;

  /** The unittype. */
  private Unittype unittype;

  /** The session id. */
  private String sessionId;

  /** The unittype updated. */
  private boolean unittypeUpdated;

  public String getTitle(String page) {
    return super.getTitle(page) + (unittype != null ? " | " + unittype.getName() : "");
  }

  public List<MenuItem> getShortcutItems(SessionData sessionData) {
    List<MenuItem> list = new ArrayList<>(super.getShortcutItems(sessionData));
    list.add(new MenuItem("Create new Unit Type", Page.UNITTYPECREATE));
    list.add(new MenuItem("Unit Type overview", Page.UNITTYPEOVERVIEW));
    list.add(new MenuItem("Trigger Overview", Page.TRIGGEROVERVIEW));
    if (unittype != null) {
      list.add(
          new MenuItem("Last 100 syslog entries", Page.SYSLOG)
              .addCommand("auto") // automatically hit the Search button
              .addParameter("unittype", unittype.getName()));
      list.add(
          new MenuItem("Add new parameter", Page.UNITTYPEPARAMETERS)
              .addParameter("unittype", unittype.getName()));
      list.add(
          new MenuItem("Manage syslog events", Page.SYSLOGEVENTS)
              .addParameter("unittype", unittype.getName()));
      list.add(
          new MenuItem("Create new trigger", Page.CREATETRIGGER)
              .addParameter("unittype", unittype.getName()));
    }
    return list;
  }

  /**
   * Action cud unittype.
   *
   * @throws Exception the exception
   */
  private void actionCUDUnittype() throws Exception {
    if (unittype == null) {
      return;
    }

    if (inputData.getFormSubmit().isValue(WebConstants.UPDATE)) {
      ProvisioningProtocol protocol =
          ProvisioningProtocol.toEnum(inputData.getProtocol().getString());
      unittype.setProtocol(protocol);
      //			if (protocol != null && protocol.equals("OPP") && inputData.getMatcherId().getString() !=
      // null)
      //				unittype.setMatcherId(inputData.getMatcherId().getString());
      //			if (protocol != null && !protocol.equalsIgnoreCase("unspecified"))
      //				unittype.setProtocol(protocol);
      //			else if (protocol != null && protocol.equalsIgnoreCase("unspecified"))
      //				unittype.setProtocol(null);
      unittype.setVendor(inputData.getVendor().getString());
      unittype.setDescription(inputData.getDescription().getString());
      acs.getUnittypes().addOrChangeUnittype(unittype, acs);
      unittypeUpdated = true;
    }
    if (inputData.getFormSubmit().isValue(WebConstants.DELETE)) {
      try {
        acs.getUnittypes().deleteUnittype(unittype, acs, true);
        inputData.getUnittype().setValue(null);
        SessionCache.getSessionData(sessionId).setUnittypeName(null);
        unittype = null;
      } catch (SQLException ex) {
        throw new SQLException(
            "Could not delete Unit Type ["
                + unittype.getName()
                + "]. Delete profile, units, groups and/or firmwares first.");
      }
    }
  }

  /**
   * Action cud parameters.
   *
   * @param params the params
   * @throws Exception the exception
   */
  private void actionCUDParameters(ParameterParser params) throws Exception {
    if (unittype == null) {
      return;
    }
    if (inputData.getFormSubmit().isValue(WebConstants.UPDATE_PARAMS)) {
      UnittypeParameter[] upParams = unittype.getUnittypeParameters().getUnittypeParameters();
      for (UnittypeParameter utp : upParams) {
        String upName = utp.getName();
        String upFlag = utp.getFlag().getFlag();
        if (params.getParameter("delete::" + upName) != null) {
          try {
            unittype.getUnittypeParameters().deleteUnittypeParameter(utp, acs);
          } catch (SQLException ex) {
            throw new SQLException(
                "Could not delete Unit Type parameter ["
                    + utp.getName()
                    + "]. Delete profile, unit, group and/or job parameters first.");
          }
        } else if (params.getParameter("update::" + upName) != null) {
          String updatedFlag = params.getParameter("update::" + upName).trim();
          if (!upFlag.equals(updatedFlag)) {
            utp.getFlag().setFlag(updatedFlag);
            unittype.getUnittypeParameters().addOrChangeUnittypeParameter(utp, acs);
          }
        }
      }
      unittypeUpdated = true;
    }
  }

  /**
   * Checks if is protocol.
   *
   * @param ut the ut
   * @param protocol the protocol
   * @return true, if is protocol
   */
  public static boolean isProtocol(Unittype ut, String protocol) {
    return ut != null && ut.getProtocol() != null && ut.getProtocol().equals(protocol);
  }

  public void process(
      ParameterParser params,
      Output outputHandler,
      DataSource xapsDataSource,
      DataSource syslogDataSource)
      throws Exception {
    // Get important var
    inputData = (UnittypeData) InputDataRetriever.parseInto(new UnittypeData(), params);

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

    Map<String, Object> root = outputHandler.getTemplateMap();

    String template = null;

    if (inputData.getUnittype().getString() != null) {
      unittype = acs.getUnittype(inputData.getUnittype().getString());
      if (unittype == null) {
        SessionCache.getSessionData(sessionId).setUnittypeName(null);
      }
    }

    actionCUDUnittype();

    actionCUDParameters(params);

    if (unittypeUpdated) {
      outputHandler.setDirectToPage(Page.UNITTYPE);
      return;
    }

    List<Unittype> unittypes = getAllowedUnittypes(sessionId, xapsDataSource, syslogDataSource);

    if (unittype != null) {
      template = "/unit-type/details.ftl";
      root.put("syslogdate", SyslogUtil.getDateString());
      root.put("unittypes", unittypes);
      DropDownSingleSelect<String> protocols =
          InputSelectionFactory.getDropDownSingleSelect(
              inputData.getProtocol(),
              getDisplayValueFromProtocol(unittype.getProtocol()),
              Arrays.asList(NA_PROTOCOL, TR069_PROTOCOL));
      root.put("protocols", protocols);
      root.put("unittype", unittype);
      root.put(
          "params",
          new TableElementMaker()
              .getParameters(unittype.getUnittypeParameters().getUnittypeParameters()));
      String selectedFlag =
          inputData.getFilterFlag().getString() != null
              ? inputData.getFilterFlag().getString()
              : "All";
      DropDownSingleSelect<String> flags =
          InputSelectionFactory.getDropDownSingleSelect(
              inputData.getFilterFlag(), selectedFlag, UnittypeParameterFlags.toList());
      root.put("flags", flags);
      root.put(
          "string",
          inputData.getFilterString().getString() != null
              ? inputData.getFilterString().getString()
              : "");
      root.put("tmsstart", SyslogUtil.getDateString());
      outputHandler.setTemplatePath(template);
    } else if (inputData.getUnittype().notNullNorValue("")) {
      outputHandler.setDirectResponse("Invalid Unit Type");
    } else {
      outputHandler.setDirectToPage(Page.UNITTYPEOVERVIEW);
    }
  }

  private static String getDisplayValueFromProtocol(ProvisioningProtocol protocol) {
    switch (protocol) {
      case NA:
        return NA_PROTOCOL;
      case TR069:
        return TR069_PROTOCOL;
      default:
        return protocol.toString();
    }
  }
}
