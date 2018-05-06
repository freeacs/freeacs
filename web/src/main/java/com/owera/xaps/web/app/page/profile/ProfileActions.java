package com.owera.xaps.web.app.page.profile;

import java.sql.SQLException;

import com.owera.common.log.Logger;
import com.owera.xaps.dbi.Profile;
import com.owera.xaps.dbi.ProfileParameter;
import com.owera.xaps.dbi.ProfileParameters;
import com.owera.xaps.dbi.Profiles;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.UnittypeParameter;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.web.app.input.DropDownSingleSelect;
import com.owera.xaps.web.app.input.ParameterParser;
import com.owera.xaps.web.app.page.AbstractWebPage;
import com.owera.xaps.web.app.util.SessionCache;
import com.owera.xaps.web.app.util.WebConstants;


/**
 * This class contains most of the protected methods and variables that are used by the profile pages.
 * 
 * @author Jarl Andre Hubenthal
 *
 */
public abstract class ProfileActions extends AbstractWebPage{
	
	/**
	 * The Enum ProfileStatus.
	 */
	enum ProfileStatus {
		
		/** The PROFIL e_ created. */
		PROFILE_CREATED,
		
		/** The PROFIL e_ nam e_ unspecified. */
		PROFILE_NAME_UNSPECIFIED, 
		
		/** The PROFIL e_ deleted. */
		PROFILE_DELETED, 
		
		/** The PROFIL e_ param s_ updated. */
		PROFILE_PARAMS_UPDATED, 
		
		/** The NONE. */
		NONE
	}
	
	/** The logger. */
	Logger logger = new Logger();
	
	/**
	 * Action create profile.
	 *
	 * @param sessionId the session id
	 * @param inputData the input data
	 * @param xaps the xaps
	 * @param unittypes the unittypes
	 * @param profiles the profiles
	 * @return the profile status
	 * @throws Exception the exception
	 */
	ProfileStatus actionCreateProfile(String sessionId,ProfileData inputData,XAPS xaps,DropDownSingleSelect<Unittype> unittypes,DropDownSingleSelect<Profile> profiles) throws Exception {
		if (inputData.getProfilename().notNullNorValue(WebConstants.ALL_ITEMS_OR_DEFAULT)) {
			String pName = inputData.getProfilename().getString();
			Profiles profilesObject = unittypes.getSelected().getProfiles();
			profilesObject.addOrChangeProfile(new Profile(pName, unittypes.getSelected()), xaps);
			String profileCopy = inputData.getProfileCopy().getString();
			if (inputData.getProfileCopy().notNullNorValue(WebConstants.ALL_ITEMS_OR_DEFAULT)) {
				Profile profileC = unittypes.getSelected().getProfiles().getByName(profileCopy);
				ProfileParameters pParamsC = profileC.getProfileParameters();
				profiles.setSelected(unittypes.getSelected().getProfiles().getByName(pName));
				ProfileParameters pParams = profiles.getSelected().getProfileParameters();
				for (ProfileParameter ppC : pParamsC.getProfileParameters()) {
					ProfileParameter pp = new ProfileParameter(profiles.getSelected(), ppC.getUnittypeParameter(), ppC.getValue());
					pParams.addOrChangeProfileParameter(pp, xaps);
				}
			} else {
				profiles.setSelected(unittypes.getSelected().getProfiles().getByName(pName));
			}
			SessionCache.getSessionData(sessionId).setProfileName(pName);
			return ProfileStatus.PROFILE_CREATED;
		}else{
			return ProfileStatus.PROFILE_NAME_UNSPECIFIED;
		}
	}
	
	/**
	 * Action delete profile.
	 *
	 * @param sessionId the session id
	 * @param xaps the xaps
	 * @param unittypes the unittypes
	 * @param profiles the profiles
	 * @return the profile status
	 * @throws Exception the exception
	 */
	ProfileStatus actionDeleteProfile(String sessionId,XAPS xaps,DropDownSingleSelect<Unittype> unittypes,DropDownSingleSelect<Profile> profiles) throws Exception {
		try {
			unittypes.getSelected().getProfiles().deleteProfile(profiles.getSelected(), xaps, true);
			profiles.setSelected(null);
			SessionCache.getSessionData(sessionId).setProfileName(null);
		} catch (SQLException ex) {
			throw new SQLException("Could not delete profile. Delete units first.<br />Caused by: " + ex.getMessage());
		}
		return ProfileStatus.PROFILE_DELETED;
	}

	/**
	 * Action cud parameters.
	 *
	 * @param req the req
	 * @param xaps the xaps
	 * @param unittypes the unittypes
	 * @param profiles the profiles
	 * @return the profile status
	 * @throws Exception the exception
	 */
	ProfileStatus actionCUDParameters(ParameterParser req,XAPS xaps,DropDownSingleSelect<Unittype> unittypes,DropDownSingleSelect<Profile> profiles) throws Exception {
		UnittypeParameter[] upParams = unittypes.getSelected().getUnittypeParameters().getUnittypeParameters();
		ProfileParameters pParams = profiles.getSelected().getProfileParameters();
		int nDeleted = 0;
		int nUpdated = 0;
		int nCreated = 0;
		for (UnittypeParameter utp : upParams) {
			String upName = utp.getName();
			ProfileParameter pp = pParams.getByName(upName);
			String pValue = null;
			if (pp != null)
				pValue = pp.getValue();
			if (pp != null && req.getParameter("delete::" + upName) != null) {
				pParams.deleteProfileParameter(pp, xaps);
				nDeleted++;
			} else if (pp == null && req.getParameter("create::" + upName) != null) {
				String newValue = req.getParameter("update::" + upName);
				if (newValue != null)
					newValue = removeFromStart(newValue, '!');
				pp = new ProfileParameter(profiles.getSelected(), utp, newValue);
				pParams.addOrChangeProfileParameter(pp, xaps);
				nCreated++;
			} else if (pp != null && req.getParameter("update::" + upName) != null) {
				String updatedValue = req.getParameter("update::" + upName);
				if (updatedValue != null && updatedValue.equals(pValue))
					continue;
				if (updatedValue != null)
					updatedValue = removeFromStart(updatedValue, '!');
				pp.setValue(updatedValue);
				pParams.addOrChangeProfileParameter(pp, xaps);
				nUpdated++;
			}
		}
		logger.debug("actionCUDParameters(): nDeleted: "+nDeleted+" / nUpdated: "+nUpdated+" / nCreated: "+nCreated);
		if(nDeleted>0 || nUpdated>0 || nCreated>0)
			return ProfileStatus.PROFILE_PARAMS_UPDATED;
		return ProfileStatus.NONE;
	}
}
