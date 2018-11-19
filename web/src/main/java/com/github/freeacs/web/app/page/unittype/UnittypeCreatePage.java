package com.github.freeacs.web.app.page.unittype;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.Unittype.ProvisioningProtocol;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.dbi.UnittypeParameters;
import com.github.freeacs.web.Page;
import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.DropDownSingleSelect;
import com.github.freeacs.web.app.input.InputDataIntegrity;
import com.github.freeacs.web.app.input.InputDataRetriever;
import com.github.freeacs.web.app.input.InputSelectionFactory;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.menu.MenuItem;
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.util.ACSLoader;
import com.github.freeacs.web.app.util.SessionCache;
import com.github.freeacs.web.app.util.SessionData;
import com.github.freeacs.web.app.util.WebConstants;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;

/** The Class UnittypeCreatePage. */
public class UnittypeCreatePage extends AbstractWebPage {
  public List<MenuItem> getShortcutItems(SessionData sessionData) {
    List<MenuItem> list = new ArrayList<>(super.getShortcutItems(sessionData));
    list.add(new MenuItem("Unit Type overview", Page.UNITTYPEOVERVIEW));
    return list;
  }

  @Override
  public void process(
      ParameterParser params,
      Output outputHandler,
      DataSource xapsDataSource,
      DataSource syslogDataSource)
      throws Exception {
    UnittypeCreateData inputData =
        (UnittypeCreateData) InputDataRetriever.parseInto(new UnittypeCreateData(), params);

    inputData.getUnittype().setValue(null);

    String sessionId = params.getSession().getId();

    ACS acs = ACSLoader.getXAPS(sessionId, xapsDataSource, syslogDataSource);
    if (acs == null) {
      outputHandler.setRedirectTarget(WebConstants.LOGIN_URI);
      return;
    }

    InputDataIntegrity.loadAndStoreSession(params, outputHandler, inputData);

    DropDownSingleSelect<Unittype> unittypesToCopyFrom =
        InputSelectionFactory.getDropDownSingleSelect(
            inputData.getUnittypeToCopyFrom(),
            acs.getUnittype(inputData.getUnittypeToCopyFrom().getString()),
            getUnittypesWithProtocol(
                sessionId,
                inputData.getNewProtocol().getString(),
                xapsDataSource,
                syslogDataSource));

    if (inputData.getFormSubmit().hasValue("Create")) {
      if (isUnittypesLimited(sessionId, xapsDataSource, syslogDataSource)) {
        outputHandler.setDirectResponse("You are not allowed to create unittypes!");
        return;
      }
      String description = inputData.getNewDescription().getString();
      String modelName = inputData.getNewModelname().getString();
      String protocol = inputData.getNewProtocol().getString();
      String vendor = inputData.getNewVendor().getString();
      //			String matcherId = modelName;
      //			if (protocol != null && protocol.equals("OPP"))
      //				matcherId = inputData.getNewMatcherid().getString();

      if (vendor != null && modelName != null) {
        String copyFrom = inputData.getUnittypeToCopyFrom().getString();
        Unittype unittypeToCopyFrom = null;
        if (copyFrom != null) {
          unittypeToCopyFrom = acs.getUnittype(copyFrom);
        }

        if (unittypeToCopyFrom != null && !unittypeToCopyFrom.getProtocol().equals(protocol)) {
          outputHandler.setDirectResponse(
              "Cannot copy parameters from a unittype of different protocol.");
          return;
        }

        Unittype unittype =
            new Unittype(modelName, vendor, description, ProvisioningProtocol.toEnum(protocol));
        acs.getUnittypes().addOrChangeUnittype(unittype, acs);

        if (unittypeToCopyFrom != null) {
          for (UnittypeParameter utp :
              getParametersFromUnittype(unittypeToCopyFrom).getUnittypeParameters()) {
            if (unittype.getUnittypeParameters().getByName(utp.getName()) == null) {
              UnittypeParameter newUtp =
                  new UnittypeParameter(unittype, utp.getName(), utp.getFlag());
              if (utp.getValues() != null) {
                newUtp.setValues(utp.getValues());
              }
              unittype.getUnittypeParameters().addOrChangeUnittypeParameter(newUtp, acs);
            }
          }
        }

        inputData.getUnittype().setValue(unittype.getName());
        SessionCache.getSessionData(sessionId).setUnittypeName(unittype.getName());

        outputHandler.setDirectToPage(Page.UNITTYPE);
        return;
      }
    }

    outputHandler.getTemplateMap().put("unittypesInProtocol", unittypesToCopyFrom);
    DropDownSingleSelect<String> protocols =
        InputSelectionFactory.getDropDownSingleSelect(
            inputData.getNewProtocol(),
            inputData.getNewProtocol().getString(),
            Arrays.asList(UnittypePage.NA_PROTOCOL, UnittypePage.TR069_PROTOCOL));
    outputHandler.getTemplateMap().put("protocols", protocols);

    outputHandler.setTemplatePath("unit-type/create");
  }

  /**
   * Gets the parameters from unittype.
   *
   * @param unittypeToCopyFrom the unittype to copy from
   * @return the parameters from unittype
   */
  private UnittypeParameters getParametersFromUnittype(Unittype unittypeToCopyFrom) {
    if (unittypeToCopyFrom != null) {
      return unittypeToCopyFrom.getUnittypeParameters();
    }
    return null;
  }

  /**
   * Gets the unittypes with protocol.
   *
   * @param sessionId the session id
   * @param protocol the protocol
   * @param xapsDataSource
   * @param syslogDataSource
   * @return the unittypes with protocol the no available connection exception
   * @throws SQLException the sQL exception
   */
  private List<Unittype> getUnittypesWithProtocol(
      String sessionId, String protocol, DataSource xapsDataSource, DataSource syslogDataSource)
      throws SQLException {
    List<Unittype> unittypes = getAllowedUnittypes(sessionId, xapsDataSource, syslogDataSource);
    List<Unittype> allowedUnittypes = new ArrayList<>();
    for (Unittype ut : unittypes) {
      if (ut.getProtocol().equals(protocol == null ? UnittypePage.NA_PROTOCOL : protocol)) {
        allowedUnittypes.add(ut);
      }
    }
    return allowedUnittypes;
  }
}
