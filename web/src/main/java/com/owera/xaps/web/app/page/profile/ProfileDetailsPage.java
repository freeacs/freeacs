package com.owera.xaps.web.app.page.profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.owera.xaps.dbi.Profile;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.web.Page;
import com.owera.xaps.web.app.Output;
import com.owera.xaps.web.app.input.DropDownSingleSelect;
import com.owera.xaps.web.app.input.InputDataIntegrity;
import com.owera.xaps.web.app.input.InputDataRetriever;
import com.owera.xaps.web.app.input.InputSelectionFactory;
import com.owera.xaps.web.app.input.ParameterParser;
import com.owera.xaps.web.app.menu.MenuItem;
import com.owera.xaps.web.app.page.syslog.SyslogUtil;
import com.owera.xaps.web.app.page.unittype.UnittypeParameterFlags;
import com.owera.xaps.web.app.page.unittype.UnittypeParameterTypes;
import com.owera.xaps.web.app.table.TableElementMaker;
import com.owera.xaps.web.app.util.SessionData;
import com.owera.xaps.web.app.util.WebConstants;
import com.owera.xaps.web.app.util.XAPSLoader;


/**
 * This is the profile configuration page, or earlier known as the profile details page.
 * 
 * @author Jarl Andre Hubenthal
 *
 */
public class ProfileDetailsPage extends ProfileActions {
	
	/** The current profile. */
	private Profile currentProfile;
	
	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.AbstractWebPage#getTitle(java.lang.String)
	 */
	public String getTitle(String page){
		return super.getTitle(page)+(currentProfile!=null?" | "+currentProfile.getName()+" | "+currentProfile.getUnittype().getName():"");
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.AbstractWebPage#getShortcutItems(com.owera.xaps.web.app.util.SessionData)
	 */
	public List<MenuItem> getShortcutItems(SessionData sessionData) {
		List<MenuItem> list = new ArrayList<MenuItem>();
		list.addAll(super.getShortcutItems(sessionData));
		list.add(new MenuItem("Create new profile", Page.PROFILECREATE));
		list.add(new MenuItem("Profile overview", Page.PROFILEOVERVIEW));
		if(currentProfile!=null){ // just to avoid problems on disruptive changes
			list.add(new MenuItem("Last 100 syslog entries", Page.SYSLOG)
				.addCommand("auto") // automatically hit the Search button
				.addParameter("unittype", currentProfile.getUnittype().getName())
				.addParameter("profile", currentProfile.getName())
			);
			list.add(new MenuItem("Upgrade profile",Page.UPGRADE)
				.addParameter("type", "Profile")
				.addParameter("unittype", currentProfile.getUnittype().getName())
				.addParameter("profile", currentProfile.getName())
			);
			list.add(new MenuItem("Service window",Page.WINDOWPROFILE)
				.addParameter("unittype", currentProfile.getUnittype().getName())
				.addParameter("profile", currentProfile.getName())
			);
		}
		return list;
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.WebPage#process(com.owera.xaps.web.app.input.ParameterParser, com.owera.xaps.web.app.output.ResponseHandler)
	 */
	public void process(ParameterParser params, Output outputHandler) throws Exception {
		ProfileData inputData = (ProfileData) InputDataRetriever.parseInto(new ProfileData(), params);

		String sessionId = params.getSession().getId();

		XAPS xaps = XAPSLoader.getXAPS(sessionId);

		if (xaps == null) {
			outputHandler.setRedirectTarget(WebConstants.DB_LOGIN_URL);
			return;
		}

		InputDataIntegrity.loadAndStoreSession(params, outputHandler, inputData, inputData.getUnittype(), inputData.getProfile(), inputData.getUnit());

		DropDownSingleSelect<Unittype> unittypes = InputSelectionFactory.getUnittypeSelection(inputData.getUnittype(), xaps);
		Unittype unittype = xaps.getUnittype(inputData.getUnittype().getString());
		DropDownSingleSelect<Profile> profiles = InputSelectionFactory.getProfileSelection(inputData.getProfile(), unittype);

		Map<String, Object> root = outputHandler.getTemplateMap();

		root.put("unittypes", unittypes);
		root.put("profiles", profiles);

		ProfileStatus status = null;
		if (inputData.getFormSubmit().isValue(WebConstants.DELETE))
			status = actionDeleteProfile(sessionId, xaps, unittypes, profiles);
		else if (inputData.getFormSubmit().isValue(WebConstants.UPDATE_PARAMS)) {
			status = actionCUDParameters(params, xaps, unittypes, profiles);
		}

		currentProfile = profiles.getSelected();

		if (status == ProfileStatus.PROFILE_DELETED) {
			outputHandler.setDirectToPage(Page.PROFILEOVERVIEW);
			return;
		}
		else if (profiles.getSelected() == null) {
			outputHandler.setDirectToPage(Page.PROFILECREATE);
			return;
		} else if (status == ProfileStatus.PROFILE_PARAMS_UPDATED) {
			outputHandler.setDirectToPage(Page.PROFILE);
			return;
		} else {
			root.put("syslogdate", SyslogUtil.getDateString());
			root.put("params", new TableElementMaker().getParameters(profiles.getSelected().getUnittype().getUnittypeParameters().getUnittypeParameters(), profiles.getSelected().getProfileParameters().getProfileParameters()));
			root.put("profile", profiles.getSelected());
			String selectedFlag = inputData.getFilterFlag().getString("All");
			DropDownSingleSelect<String> flags = InputSelectionFactory.getDropDownSingleSelect(inputData.getFilterFlag(), selectedFlag, UnittypeParameterFlags.toList());
			root.put("flags", flags);
			String selectedType = inputData.getFilterType().getString("Configured");
			DropDownSingleSelect<String> types = InputSelectionFactory.getDropDownSingleSelect(inputData.getFilterType(), selectedType, UnittypeParameterTypes.toList());
			root.put("types", types);
			root.put("string", inputData.getFilterString());
		}

		outputHandler.setTemplatePath("/profile/details");
	}
}