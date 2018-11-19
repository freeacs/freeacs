package com.github.freeacs.web.app.page.job;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.web.Page;
import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.InputDataIntegrity;
import com.github.freeacs.web.app.input.InputDataRetriever;
import com.github.freeacs.web.app.input.InputSelectionFactory;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.menu.MenuItem;
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.table.TableElementMaker;
import com.github.freeacs.web.app.util.ACSLoader;
import com.github.freeacs.web.app.util.SessionData;
import com.github.freeacs.web.app.util.WebConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

/**
 * Job overview.
 *
 * @author Jarl Andre Hubenthal
 */
public class JobsPage extends AbstractWebPage {
  public List<MenuItem> getShortcutItems(SessionData sessionData) {
    List<MenuItem> list = new ArrayList<>(super.getShortcutItems(sessionData));
    list.add(new MenuItem("Create new Job", Page.JOB).addCommand("create"));
    return list;
  }

  public void process(
      ParameterParser req,
      Output outputHandler,
      DataSource xapsDataSource,
      DataSource syslogDataSource)
      throws Exception {
    JobsData inputData = (JobsData) InputDataRetriever.parseInto(new JobsData(), req);

    String sessionId = req.getSession().getId();

    ACS acs = ACSLoader.getXAPS(sessionId, xapsDataSource, syslogDataSource);
    if (acs == null) {
      outputHandler.setRedirectTarget(WebConstants.LOGIN_URI);
      return;
    }

    InputDataIntegrity.loadAndStoreSession(
        req,
        outputHandler,
        inputData,
        inputData.getUnittype(),
        inputData.getProfile(),
        inputData.getUnit());

    Map<String, Object> root = outputHandler.getTemplateMap();
    root.put("unittypes", InputSelectionFactory.getUnittypeSelection(inputData.getUnittype(), acs));

    Unittype unittype = null;
    if (inputData.getUnittype().notNullNorValue(WebConstants.ALL_ITEMS_OR_DEFAULT)) {
      unittype = acs.getUnittype(inputData.getUnittype().getString());
    }
    if (unittype != null) {
      root.put("params", new TableElementMaker().getJobs(unittype));
    }

    outputHandler.setTemplatePath("job/list");
  }
}
