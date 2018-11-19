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
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.util.ACSLoader;
import com.github.freeacs.web.app.util.SessionData;
import com.github.freeacs.web.app.util.WebConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

/**
 * The profile overview page.
 *
 * @author Jarl Andre Hubenthal
 */
public class ProfileOverviewPage extends AbstractWebPage {
  public List<MenuItem> getShortcutItems(SessionData sessionData) {
    List<MenuItem> list = new ArrayList<>(super.getShortcutItems(sessionData));
    list.add(new MenuItem("Create new Profile", Page.PROFILECREATE));
    return list;
  }

  @Override
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
    DropDownSingleSelect<Profile> profiles =
        InputSelectionFactory.getProfileSelection(
            inputData.getProfile(), inputData.getUnittype(), acs);

    Map<String, Object> map = outputHandler.getTemplateMap();

    map.put("unittypes", unittypes);
    map.put("profiles", profiles);

    outputHandler.setTemplatePath("/profile/index");
  }
}
