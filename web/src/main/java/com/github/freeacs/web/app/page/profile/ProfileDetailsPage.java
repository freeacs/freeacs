package com.github.freeacs.web.app.page.profile;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.web.Page;
import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.DropDownSingleSelect;
import com.github.freeacs.web.app.input.InputDataIntegrity;
import com.github.freeacs.web.app.input.InputDataRetriever;
import com.github.freeacs.web.app.input.InputSelectionFactory;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.menu.MenuItem;
import com.github.freeacs.web.app.page.syslog.SyslogUtil;
import com.github.freeacs.web.app.page.unittype.UnittypeParameterFlags;
import com.github.freeacs.web.app.page.unittype.UnittypeParameterTypes;
import com.github.freeacs.web.app.table.TableElementMaker;
import com.github.freeacs.web.app.util.ACSLoader;
import com.github.freeacs.web.app.util.SessionData;
import com.github.freeacs.web.app.util.WebConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

/**
 * This is the profile configuration page, or earlier known as the profile details page.
 *
 * @author Jarl Andre Hubenthal
 */
public class ProfileDetailsPage extends ProfileActions {
  /** The current profile. */
  private Profile currentProfile;

  public String getTitle(String page) {
    return super.getTitle(page)
        + (currentProfile != null
            ? " | " + currentProfile.getName() + " | " + currentProfile.getUnittype().getName()
            : "");
  }

  public List<MenuItem> getShortcutItems(SessionData sessionData) {
    List<MenuItem> list = new ArrayList<>(super.getShortcutItems(sessionData));
    list.add(new MenuItem("Create new profile", Page.PROFILECREATE));
    list.add(new MenuItem("Profile overview", Page.PROFILEOVERVIEW));
    if (currentProfile != null) { // just to avoid problems on disruptive changes
      list.add(
          new MenuItem("Last 100 syslog entries", Page.SYSLOG)
              .addCommand("auto") // automatically hit the Search button
              .addParameter("unittype", currentProfile.getUnittype().getName())
              .addParameter("profile", currentProfile.getName()));
      list.add(
          new MenuItem("Upgrade profile", Page.UPGRADE)
              .addParameter("type", "Profile")
              .addParameter("unittype", currentProfile.getUnittype().getName())
              .addParameter("profile", currentProfile.getName()));
      list.add(
          new MenuItem("Service window", Page.WINDOWPROFILE)
              .addParameter("unittype", currentProfile.getUnittype().getName())
              .addParameter("profile", currentProfile.getName()));
    }
    return list;
  }

  public void process(
      ParameterParser params,
      Output outputHandler,
      DataSource xapsDataSource,
      DataSource syslogDataSource)
      throws Exception {
    ProfileData inputData = (ProfileData) InputDataRetriever.parseInto(new ProfileData(), params);

    String sessionId = params.getSession().getId();

    ACS acs = ACSLoader.getXAPS(sessionId, xapsDataSource, syslogDataSource);

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

    DropDownSingleSelect<Unittype> unittypes =
        InputSelectionFactory.getUnittypeSelection(inputData.getUnittype(), acs);
    Unittype unittype = acs.getUnittype(inputData.getUnittype().getString());
    DropDownSingleSelect<Profile> profiles =
        InputSelectionFactory.getProfileSelection(inputData.getProfile(), unittype);

    Map<String, Object> root = outputHandler.getTemplateMap();

    root.put("unittypes", unittypes);
    root.put("profiles", profiles);

    ProfileStatus status = null;
    if (inputData.getFormSubmit().isValue(WebConstants.DELETE)) {
      status = actionDeleteProfile(sessionId, acs, unittypes, profiles);
    } else if (inputData.getFormSubmit().isValue(WebConstants.UPDATE_PARAMS)) {
      status = actionCUDParameters(params, acs, unittypes, profiles);
    }

    currentProfile = profiles.getSelected();

    if (status == ProfileStatus.PROFILE_DELETED) {
      outputHandler.setDirectToPage(Page.PROFILEOVERVIEW);
      return;
    } else if (profiles.getSelected() == null) {
      outputHandler.setDirectToPage(Page.PROFILECREATE);
      return;
    } else if (status == ProfileStatus.PROFILE_PARAMS_UPDATED) {
      outputHandler.setDirectToPage(Page.PROFILE);
      return;
    } else {
      root.put("syslogdate", SyslogUtil.getDateString());
      root.put(
          "params",
          new TableElementMaker()
              .getParameters(
                  profiles
                      .getSelected()
                      .getUnittype()
                      .getUnittypeParameters()
                      .getUnittypeParameters(),
                  profiles.getSelected().getProfileParameters().getProfileParameters()));
      root.put("profile", profiles.getSelected());
      String selectedFlag = inputData.getFilterFlag().getString("All");
      DropDownSingleSelect<String> flags =
          InputSelectionFactory.getDropDownSingleSelect(
              inputData.getFilterFlag(), selectedFlag, UnittypeParameterFlags.toList());
      root.put("flags", flags);
      String selectedType = inputData.getFilterType().getString("Configured");
      DropDownSingleSelect<String> types =
          InputSelectionFactory.getDropDownSingleSelect(
              inputData.getFilterType(), selectedType, UnittypeParameterTypes.toList());
      root.put("types", types);
      root.put("string", inputData.getFilterString());
    }

    outputHandler.setTemplatePath("/profile/details");
  }
}
