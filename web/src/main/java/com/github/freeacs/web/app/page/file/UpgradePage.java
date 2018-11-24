package com.github.freeacs.web.app.page.file;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.FileType;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.ProfileParameter;
import com.github.freeacs.dbi.ProfileParameters;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.UnitParameter;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.DropDownSingleSelect;
import com.github.freeacs.web.app.input.InputDataIntegrity;
import com.github.freeacs.web.app.input.InputDataRetriever;
import com.github.freeacs.web.app.input.InputSelectionFactory;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.util.ACSLoader;
import com.github.freeacs.web.app.util.SessionCache;
import com.github.freeacs.web.app.util.WebConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

/**
 * The Class UpgradePage.
 *
 * @author Jarl Andre Hubenthal
 */
public class UpgradePage extends AbstractWebPage {
  /** The xaps. */
  private ACS acs;

  /** The input data. */
  private UpgradeData inputData = new UpgradeData();

  /** The xaps unit. */
  private ACSUnit acsUnit;

  /** The parameter version. */
  private String parameterVersion = SystemParameters.DESIRED_SOFTWARE_VERSION;

  /** The unittypes. */
  private DropDownSingleSelect<Unittype> unittypes;

  /**
   * Update database for unit.
   *
   * @param root the root
   * @throws Exception the exception
   */
  private void updateDatabaseForUnit(Map<String, Object> root) throws Exception {
    Unit u = acsUnit.getUnitById(inputData.getUnit().getString(), unittypes.getSelected(), null);

    if (u == null) {
      throw new Exception("The unit <i>" + inputData.getUnit().getString() + "</i> was not found");
    }

    Map<String, UnitParameter> params = acsUnit.getUnitById(u.getId()).getUnitParameters();

    UnitParameter version = params.get(parameterVersion);

    String versionNumber = inputData.getFirmware().getString();

    if (version == null) {
      UnittypeParameter utp =
          acs.getUnittypeParameter(inputData.getUnittype().getString(), parameterVersion);
      if (utp == null) {
        throw new Exception(
            "Invalid unittype. Missing Owera specific parameters. Cannot add Unit parameters.");
      }
      version =
          new UnitParameter(utp, u.getId(), versionNumber, acs.getProfile(u.getProfile().getId()));
    } else {
      version.getParameter().setValue(versionNumber);
    }

    List<UnitParameter> updatedParams = new ArrayList<>();
    updatedParams.add(version);
    acsUnit.addOrChangeUnitParameters(updatedParams, u.getProfile());

    root.put("message", "Saved successfully");
  }

  /**
   * Update database for profile.
   *
   * @param root the root
   * @throws Exception the exception
   */
  private void updateDatabaseForProfile(Map<String, Object> root) throws Exception {
    Profile profile =
        acs.getProfile(inputData.getUnittype().getString(), inputData.getProfile().getString());
    ProfileParameters params = profile.getProfileParameters();
    ProfileParameter version = params.getByName(parameterVersion);

    String value = "";
    if (inputData.getFirmware().getString() != null) {
      value =
          unittypes
              .getSelected()
              .getFiles()
              .getByVersionType(inputData.getFirmware().getString(), FileType.SOFTWARE)
              .getVersion();
    }

    if (version == null) {
      UnittypeParameter utp =
          acs.getUnittypeParameter(inputData.getUnittype().getString(), parameterVersion);
      if (utp == null) {
        throw new Exception(
            "Invalid unittype. Missing Owera specific parameters. Cannot add Profile parameters.");
      }
      version = new ProfileParameter(profile, utp, value);
    } else {
      version.setValue(value);
    }

    params.addOrChangeProfileParameter(version, acs);
    root.put("message", "Saved successfully");
  }

  public void process(
      ParameterParser params,
      Output outputHandler,
      DataSource xapsDataSource,
      DataSource syslogDataSource)
      throws Exception {
    inputData = (UpgradeData) InputDataRetriever.parseInto(new UpgradeData(), params);

    String sessionId = params.getSession().getId();

    acs = ACSLoader.getXAPS(sessionId, xapsDataSource, syslogDataSource);
    if (acs == null) {
      outputHandler.setRedirectTarget(WebConstants.LOGIN_URI);
      return;
    }

    acsUnit = ACSLoader.getACSUnit(sessionId, xapsDataSource, syslogDataSource);

    InputDataIntegrity.loadAndStoreSession(
        params, outputHandler, inputData, inputData.getUnittype());

    unittypes = InputSelectionFactory.getUnittypeSelection(inputData.getUnittype(), acs);

    Map<String, Object> root = outputHandler.getTemplateMap();

    try {
      if (inputData.getFormSubmit().hasValue("Upgrade")) {
        if (inputData.getUpgradeType().isValue("Unit")) {
          updateDatabaseForUnit(root);
        } else if (inputData.getUpgradeType().isValue("Profile")) {
          updateDatabaseForProfile(root);
        }
      }
    } catch (Exception e) {
      root.put("error", e.getLocalizedMessage());
    }

    root.put("unittypes", unittypes);

    if (unittypes.getSelected() != null) {
      root.put(
          "types",
          InputSelectionFactory.getDropDownSingleSelect(
              inputData.getUpgradeType(),
              inputData.getUpgradeType().getString(),
              Arrays.asList("Profile", "Unit")));
      if (inputData.getUpgradeType().hasValue("Profile")) {
        root.put(
            "profiles",
            InputSelectionFactory.getProfileSelection(
                inputData.getProfile(), unittypes.getSelected()));
      } else if (inputData.getUpgradeType().hasValue("Unit")) {
        root.put("unit", inputData.getUnit().getString());
      }
      root.put(
          "softwares",
          InputSelectionFactory.getDropDownSingleSelect(
              inputData.getFirmware(),
              unittypes
                  .getSelected()
                  .getFiles()
                  .getByVersionType(inputData.getFirmware().getString(), FileType.SOFTWARE),
              Arrays.asList(unittypes.getSelected().getFiles().getFiles(FileType.SOFTWARE))));
    }

    outputHandler.setTemplatePathWithIndex("upgrade");
  }
}
