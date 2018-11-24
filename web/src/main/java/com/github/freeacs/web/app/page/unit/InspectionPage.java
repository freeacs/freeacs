package com.github.freeacs.web.app.page.unit;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.UnitParameter;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;
import com.github.freeacs.web.app.input.InputDataIntegrity;
import com.github.freeacs.web.app.input.InputDataRetriever;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.util.ACSLoader;

import java.util.Collections;
import java.util.Map;
import javax.sql.DataSource;

/**
 * Used by the {@link UnitPage} to continuously poll for changes in the inspection mode and state.
 *
 * @author Jarl Andre Hubenthal
 */
public class InspectionPage extends AbstractWebPage {
  /** The xaps. */
  private ACS acs;

  /** The xaps unit. */
  private ACSUnit acsUnit;

  /** The unit. */
  private Unit unit;

  /** The profile. */
  private Profile profile;

  /** The unittype. */
  private Unittype unittype;

  /** The input data. */
  private InspectionData inputData;

  /**
   * Action populate.
   *
   * @throws Exception the exception
   */
  private void actionPopulate() throws Exception {
    if (inputData.getUnit().getString() != null) {
      unit = acsUnit.getUnitById(inputData.getUnit().getString());
      if (unit != null) {
        profile = unit.getProfile();
        if (inputData.getProfile().getString() == null) {
          inputData.getProfile().setValue(profile.getName());
        }
        unittype = unit.getUnittype();
        inputData.getUnittype().setValue(unittype.getName());
      }
      if (unittype == null && inputData.getUnittype().getString() != null) {
        unittype = acs.getUnittype(inputData.getUnittype().getString());
      }
      if (profile == null && inputData.getProfile().getString() != null && unittype != null) {
        profile = unittype.getProfiles().getByName(inputData.getProfile().getString());
      }
    }
  }

  public void process(
      ParameterParser params, Output res, DataSource xapsDataSource, DataSource syslogDataSource)
      throws Exception {
    inputData = (InspectionData) InputDataRetriever.parseInto(new InspectionData(), params);
    res.setContentType("text/html");
    try {
      String sessionId = params.getSession().getId();

      acs = ACSLoader.getXAPS(sessionId, xapsDataSource, syslogDataSource);
      acsUnit = ACSLoader.getACSUnit(sessionId, xapsDataSource, syslogDataSource);

      if (acs == null || acsUnit == null) {
        throw new Exception("Could not load xaps objects!");
      }

      InputDataIntegrity.rememberAndCheck(
          params.getSession().getId(), inputData.getUnittype(), inputData.getProfile());

      actionPopulate();

      String message = null;
      if (unit != null && profile != null) {
        UnitParameter up = unit.getUnitParameters().get(SystemParameters.INSPECTION_MESSAGE);
        if (up != null && !"N/A".equals(up.getValue())) {
          message = up.getValue();
          up.setValue("N/A");
          acsUnit.addOrChangeUnitParameters(Collections.singletonList(up), profile);
        }
      }

      UnitParameter mode = unit.getUnitParameters().get(SystemParameters.PROVISIONING_MODE);
      //			UnitParameter state = unit.getUnitParameters().get(SystemParameters.PROVISIONING_STATE);
      if (mode == null /* || state == null */) {
        res.setDirectResponse("relo");
      } else {
        String modeString = inputData.getMode().getString();
        //				String stateString = inputData.getState().getString();
        if (mode.getParameter()
            .getValue()
            .equals(modeString) /* && state.getParameter().getValue().equals(stateString) */) {
          res.setDirectResponse(
              "wait" + (message != null && !message.trim().isEmpty() ? message : ""));
        } else {
          res.setDirectResponse("relo");
        }
      }
    } catch (Throwable e) {
      res.setDirectResponse(e.getLocalizedMessage());
    }
  }

  /** The Class InspectionData. */
  public class InspectionData extends InputData {
    /** The mode. */
    private Input mode = Input.getStringInput("mode");

    /** The state. */
    private Input state = Input.getStringInput("state");

    /**
     * Gets the mode.
     *
     * @return the mode
     */
    public Input getMode() {
      return this.mode;
    }

    /**
     * Sets the mode.
     *
     * @param mode the new mode
     */
    public void setMode(Input mode) {
      this.mode = mode;
    }

    /**
     * Gets the state.
     *
     * @return the state
     */
    public Input getState() {
      return this.state;
    }

    /**
     * Sets the state.
     *
     * @param state the new state
     */
    public void setState(Input state) {
      this.state = state;
    }

    @Override
    public void bindForm(Map<String, Object> root) {}

    @Override
    public boolean validateForm() {
      return false;
    }
  }
}
