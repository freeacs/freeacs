package com.github.freeacs.web.app.page.unittype;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.dbi.UnittypeParameterFlag;
import com.github.freeacs.dbi.UnittypeParameterValues;
import com.github.freeacs.web.Page;
import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.InputDataIntegrity;
import com.github.freeacs.web.app.input.InputDataRetriever;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.util.ACSLoader;
import com.github.freeacs.web.app.util.StackTraceFormatter;
import com.github.freeacs.web.app.util.WebConstants;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

public class UnittypeParametersPage extends AbstractWebPage {

  private UnittypeParameter utParam;
  private boolean utpAdded;
  private String error;

  /** The Session keys. */
  private static final String SESSION_SAVE_BOOLEAN = "utp-save-complete";

  private static final String SESSION_SAVE_ERRORS = "utp-save-error";

  private DataSource mainDataSource;

  public UnittypeParametersPage() {
    this.mainDataSource = null;
  }

  /**
   * For use by jQuery on the search page for the "Add new parameter" in advanced mode.
   *
   * @param unittype
   * @param term
   * @param session
   * @return A toString()'ed JSON object
   * @throws SQLException
   */
  public String getUnittypeParameters(String unittype, String term, HttpSession session)
      throws SQLException, JsonProcessingException {
    ACS acs = ACSLoader.getXAPS(session.getId(), mainDataSource, mainDataSource);
    List<Unittype> allowedUnittypes = Arrays.asList(acs.getUnittypes().getUnittypes());
    Unittype unittypeFromRequest = acs.getUnittype(unittype);
    if (allowedUnittypes.contains(unittypeFromRequest)) {
      UnittypeParameter[] utpArr =
          unittypeFromRequest.getUnittypeParameters().getUnittypeParameters();
      List<ValueHolder> values = new ArrayList<>();
      for (UnittypeParameter utp : utpArr) {
        if (utp.getName().toLowerCase().contains(term.toLowerCase())) {
          ValueHolder param = new ValueHolder(utp.getName());
          values.add(param);
        }
      }
      return OBJECT_MAPPER.writeValueAsString(values);
    }
    return null;
  }

  public void process(
      ParameterParser params,
      Output outputHandler,
      DataSource xapsDataSource,
      DataSource syslogDataSource)
      throws Exception {
    UnittypeParametersData inputData =
        (UnittypeParametersData) InputDataRetriever.parseInto(new UnittypeParametersData(), params);

    /** The session id. */
    String sessionId = params.getSession().getId();

    ACS acs = ACSLoader.getXAPS(sessionId, xapsDataSource, syslogDataSource);
    if (acs == null) {
      outputHandler.setRedirectTarget(WebConstants.LOGIN_URI);
      return;
    }

    InputDataIntegrity.loadAndStoreSession(
        params, outputHandler, inputData, inputData.getUnittype());

    Unittype unittype = null;

    String utName = inputData.getUnittype().getString();
    if (isValidString(utName)) {
      unittype = acs.getUnittype(utName);
    }

    if (unittype == null) {
      outputHandler.setDirectToPage(Page.UNITTYPE);
      return;
    }

    Map<String, Object> root = outputHandler.getTemplateMap();

    root.put("unittype", unittype);

    if (params.getParameter("utp") != null && isNumber(params.getParameter("utp"))) {
      utParam =
          unittype.getUnittypeParameters().getById(Integer.parseInt(params.getParameter("utp")));
    } else if (params.getParameter("utp") != null) {
      utParam = unittype.getUnittypeParameters().getByName(params.getParameter("utp"));
    }

    if (inputData.getFormSubmit().isValue("Save parameter")) {
      saveParameter(params, unittype, acs);
    }

    if (params.getSession().getAttribute(SESSION_SAVE_ERRORS) != null) {
      error = (String) params.getSession().getAttribute(SESSION_SAVE_ERRORS);
      params.getSession().removeAttribute(SESSION_SAVE_ERRORS);
    }

    if (params.getSession().getAttribute(SESSION_SAVE_BOOLEAN) != null) {
      utpAdded = (Boolean) params.getSession().getAttribute(SESSION_SAVE_BOOLEAN);
      params.getSession().removeAttribute(SESSION_SAVE_BOOLEAN);
    }

    root.put("added", utpAdded);
    root.put("error", error);
    root.put("param", utParam);

    outputHandler.setTemplatePath("/unit-type/addparameter.ftl");
  }

  private void saveParameter(ParameterParser params, Unittype unittype, ACS acs) {
    String name = params.getParameter("name::1");
    String old_name = params.getParameter("param.name");
    String flag = params.getParameter("flag::1");

    if (isValidString(name) && isValidString(flag)) {
      try {
        utParam = unittype.getUnittypeParameters().getByName(name);
        if (utParam != null) {
          utParam.getFlag().setFlag(flag);
        } else if ((utParam = unittype.getUnittypeParameters().getByName(old_name)) != null) {
          utParam.setName(name.replaceAll(" ", "_"));
          utParam.getFlag().setFlag(flag);
        } else {
          UnittypeParameterFlag utpFlag = new UnittypeParameterFlag(flag);
          utParam = new UnittypeParameter(unittype, name.replaceAll(" ", "_"), utpFlag);
        }

        if (!utParam.getFlag().isReadOnly()) {
          UnittypeParameterValues utpVals = utParam.getValues();
          if (utpVals == null) {
            utParam.setValues(new UnittypeParameterValues());
          }
          List<String> values = new ArrayList<>();
          String[] tmp = params.getStringParameterArray("value::1::field");
          if (tmp != null) {
            for (String s : tmp) {
              String trimmed = s.trim();
              if (!trimmed.isEmpty()) {
                values.add(trimmed);
              }
            }
          }
          utParam.getValues().setValues(values);
        } else if (utParam.getValues() != null) {
          utParam.getValues().setValues(new ArrayList<String>());
        }

        unittype.getUnittypeParameters().addOrChangeUnittypeParameter(utParam, acs);

        utpAdded = true;
      } catch (SQLException e) {
        error += "Failed to add parameter " + name + ": " + e.getLocalizedMessage();
      } catch (Exception ex) {
        error += ex.getLocalizedMessage() + StackTraceFormatter.getStackTraceAsHTML(ex);
      }
    }
  }

  public void setMainDataSource(DataSource mainDataSource) {
    this.mainDataSource = mainDataSource;
  }
}
