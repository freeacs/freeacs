package com.github.freeacs.web.app.page.group;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.web.Page;
import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.InputDataIntegrity;
import com.github.freeacs.web.app.input.InputDataRetriever;
import com.github.freeacs.web.app.input.InputSelectionFactory;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.menu.MenuItem;
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.table.TableElement;
import com.github.freeacs.web.app.table.TableElementMaker;
import com.github.freeacs.web.app.util.SessionData;
import com.github.freeacs.web.app.util.WebConstants;
import com.github.freeacs.web.app.util.XAPSLoader;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * A page for Group Overview.
 * 
 * @author Jarl Andre Hubenthal
 */
public class GroupsPage extends AbstractWebPage {

	/** The xaps. */
	private ACS acs;

	/** The input data. */
	private GroupsData inputData;

	/** The unittype. */
	private Unittype unittype;

	/** The profile. */
	private Profile profile;

	/** The session id. */
	private String sessionId;
	
	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.AbstractWebPage#getShortcutItems(com.owera.xaps.web.app.util.SessionData)
	 */
	public List<MenuItem> getShortcutItems(SessionData sessionData){
		List<MenuItem> list = new ArrayList<MenuItem>();
		list.addAll(super.getShortcutItems(sessionData));
		list.add(new MenuItem("Create new Group",Page.GROUP)
			.addCommand("create")
		);
		return list;
	}
        
	/**
	 * The Class GroupProfileMethod.
	 */
	public class GroupProfileMethod implements TemplateMethodModel {

		/* (non-Javadoc)
		 * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
		 */
		@SuppressWarnings("rawtypes")
		public TemplateModel exec(List arg0) throws TemplateModelException {
			String groupName = (String) arg0.get(0);

			Group group = unittype.getGroups().getByName(groupName);

			Group lastgroup = null;
			Group current = group;
			while ((current = current.getParent()) != null) {
				lastgroup = current;
			}

			Profile profile = null;
			if (lastgroup != null && lastgroup.getProfile() != null)
				profile = unittype.getProfiles().getById(lastgroup.getProfile().getId());
			else if (group.getProfile() != null)
				profile = unittype.getProfiles().getById(group.getProfile().getId());

			if (profile != null)
				return new SimpleScalar(profile.getName());
			return new SimpleScalar("All profiles");
		}
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.WebPage#process(com.owera.xaps.web.app.input.ParameterParser, com.owera.xaps.web.app.output.ResponseHandler)
	 */
	public void process(ParameterParser req, Output outputHandler, DataSource xapsDataSource, DataSource syslogDataSource) throws Exception {
		// important object var
		inputData = (GroupsData) InputDataRetriever.parseInto(new GroupsData(),req);

		sessionId = req.getSession().getId();
		
		acs = XAPSLoader.getXAPS(sessionId, xapsDataSource, syslogDataSource);
		if (acs == null) {
			outputHandler.setRedirectTarget(WebConstants.DB_LOGIN_URL);
			return;
		}

		InputDataIntegrity.loadAndStoreSession(req,outputHandler,inputData, inputData.getUnittype(),inputData.getProfile(), inputData.getGroup());

		// Action
		if (inputData.getUnittype().notNullNorValue(WebConstants.ALL_ITEMS_OR_DEFAULT)) {
			unittype = acs.getUnittype(inputData.getUnittype().getString());
			if (unittype != null) {
				profile = unittype.getProfiles().getByName(inputData.getProfile().getString());
			}
		}

		Map<String, Object> root = outputHandler.getTemplateMap();
		root.put("unittypes", InputSelectionFactory.getUnittypeSelection(inputData.getUnittype(), acs));
		if (unittype != null) {
			root.put("findprofile", new GroupProfileMethod());
			root.put("profiles", InputSelectionFactory.getProfileSelection(inputData.getProfile(), inputData.getUnittype(), acs));
			List<TableElement> params = new TableElementMaker().getGroups(unittype);
			List<TableElement> copy = new ArrayList<TableElement>();
			copy.addAll(params);
			for (TableElement elm : copy) {
				if (!isGroupInProfile(elm.getGroup(), profile))
					params.remove(elm);
				else
					continue;
			}

			root.put("params", params);
		}

		outputHandler.setTemplatePath("group/list");
	}

	/**
	 * Checks if is group in profile.
	 *
	 * @param g the g
	 * @param p the p
	 * @return true, if is group in profile
	 */
	private boolean isGroupInProfile(Group g, Profile p) {
		if (profile == null)
			return true;
		if (g.getProfile() != null && g.getProfile().getName().equals(p.getName()))
			return true;
		else if (g.getProfile() == null && g.getTopParent().getProfile() != null && g.getTopParent().getProfile().getName().equals(p.getName()))
			return true;
		else if (g.getProfile() == null && g.getTopParent().getProfile() == null)
			return true;
		return false;
	}
}