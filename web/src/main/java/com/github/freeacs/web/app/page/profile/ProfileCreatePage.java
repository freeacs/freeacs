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
import com.github.freeacs.web.app.util.ACSLoader;
import com.github.freeacs.web.app.util.SessionData;
import com.github.freeacs.web.app.util.WebConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

/**
 * What the name implies, a page for creating a profile.
 *
 * @author Jarl Andre Hubenthal
 */
public class ProfileCreatePage extends ProfileActions {
  public List<MenuItem> getShortcutItems(SessionData sessionData) {
    List<MenuItem> list = new ArrayList<>(super.getShortcutItems(sessionData));
    list.add(new MenuItem("Profile overview", Page.PROFILEOVERVIEW));
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

    Map<String, Object> root = outputHandler.getTemplateMap();

    DropDownSingleSelect<Unittype> unittypes =
        InputSelectionFactory.getUnittypeSelection(inputData.getUnittype(), acs);

    DropDownSingleSelect<Profile> profiles =
        InputSelectionFactory.getProfileSelection(
            inputData.getProfile(), inputData.getUnittype(), acs);

    DropDownSingleSelect<Profile> profilestocopyfrom =
        InputSelectionFactory.getProfileSelection(
            inputData.getProfileCopy(), inputData.getUnittype(), acs);

    root.put("unittypes", unittypes);

    if (unittypes.getSelected() != null) {
      root.put("profilestocopyfrom", profilestocopyfrom);
    }

    if (inputData.getFormSubmit().isValue("Create profile")) {
      if (isProfilesLimited(unittypes.getSelected(), sessionId, xapsDataSource, syslogDataSource)) {
        throw new Exception("You are not allowed to create profiles!");
      }
      if (unittypes.getSelected() != null
          && unittypes.getSelected().getProfiles().getByName(inputData.getProfilename().getString())
              == null) {
        ProfileStatus status = actionCreateProfile(sessionId, inputData, acs, unittypes, profiles);
        if (status == ProfileStatus.PROFILE_CREATED) {
          outputHandler.setDirectToPage(Page.PROFILE);
          return;
        } else if (status == ProfileStatus.PROFILE_NAME_UNSPECIFIED) {
          root.put("error", "The profile name was not specified");
        }
      } else {
        root.put(
            "error", "The profile " + inputData.getProfilename().getString() + " already exists.");
      }
    }

    outputHandler.setTemplatePath("/profile/create");
  }
}
