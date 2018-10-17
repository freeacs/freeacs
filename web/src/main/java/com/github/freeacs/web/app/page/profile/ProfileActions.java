package com.github.freeacs.web.app.page.profile;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.ProfileParameter;
import com.github.freeacs.dbi.ProfileParameters;
import com.github.freeacs.dbi.Profiles;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.web.app.input.DropDownSingleSelect;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.util.SessionCache;
import com.github.freeacs.web.app.util.WebConstants;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains most of the protected methods and variables that are used by the profile
 * pages.
 *
 * @author Jarl Andre Hubenthal
 */
public abstract class ProfileActions extends AbstractWebPage {
  /** The Enum ProfileStatus. */
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
  private static final Logger logger = LoggerFactory.getLogger(ProfileActions.class);

  /**
   * Action create profile.
   *
   * @param sessionId the session id
   * @param inputData the input data
   * @param acs the xaps
   * @param unittypes the unittypes
   * @param profiles the profiles
   * @return the profile status
   * @throws Exception the exception
   */
  ProfileStatus actionCreateProfile(
      String sessionId,
      ProfileData inputData,
      ACS acs,
      DropDownSingleSelect<Unittype> unittypes,
      DropDownSingleSelect<Profile> profiles)
      throws Exception {
    if (inputData.getProfilename().notNullNorValue(WebConstants.ALL_ITEMS_OR_DEFAULT)) {
      String pName = inputData.getProfilename().getString();
      Profiles profilesObject = unittypes.getSelected().getProfiles();
      profilesObject.addOrChangeProfile(new Profile(pName, unittypes.getSelected()), acs);
      String profileCopy = inputData.getProfileCopy().getString();
      if (inputData.getProfileCopy().notNullNorValue(WebConstants.ALL_ITEMS_OR_DEFAULT)) {
        Profile profileC = unittypes.getSelected().getProfiles().getByName(profileCopy);
        ProfileParameters pParamsC = profileC.getProfileParameters();
        profiles.setSelected(unittypes.getSelected().getProfiles().getByName(pName));
        ProfileParameters pParams = profiles.getSelected().getProfileParameters();
        for (ProfileParameter ppC : pParamsC.getProfileParameters()) {
          ProfileParameter pp =
              new ProfileParameter(
                  profiles.getSelected(), ppC.getUnittypeParameter(), ppC.getValue());
          pParams.addOrChangeProfileParameter(pp, acs);
        }
      } else {
        profiles.setSelected(unittypes.getSelected().getProfiles().getByName(pName));
      }
      SessionCache.getSessionData(sessionId).setProfileName(pName);
      return ProfileStatus.PROFILE_CREATED;
    } else {
      return ProfileStatus.PROFILE_NAME_UNSPECIFIED;
    }
  }

  /**
   * Action delete profile.
   *
   * @param sessionId the session id
   * @param acs the xaps
   * @param unittypes the unittypes
   * @param profiles the profiles
   * @return the profile status
   * @throws Exception the exception
   */
  ProfileStatus actionDeleteProfile(
      String sessionId,
      ACS acs,
      DropDownSingleSelect<Unittype> unittypes,
      DropDownSingleSelect<Profile> profiles)
      throws Exception {
    try {
      unittypes.getSelected().getProfiles().deleteProfile(profiles.getSelected(), acs, true);
      profiles.setSelected(null);
      SessionCache.getSessionData(sessionId).setProfileName(null);
    } catch (SQLException ex) {
      throw new SQLException(
          "Could not delete profile. Delete units first.<br />Caused by: " + ex.getMessage());
    }
    return ProfileStatus.PROFILE_DELETED;
  }

  /**
   * Action cud parameters.
   *
   * @param req the req
   * @param acs the xaps
   * @param unittypes the unittypes
   * @param profiles the profiles
   * @return the profile status
   * @throws Exception the exception
   */
  ProfileStatus actionCUDParameters(
      ParameterParser req,
      ACS acs,
      DropDownSingleSelect<Unittype> unittypes,
      DropDownSingleSelect<Profile> profiles)
      throws Exception {
    UnittypeParameter[] upParams =
        unittypes.getSelected().getUnittypeParameters().getUnittypeParameters();
    ProfileParameters pParams = profiles.getSelected().getProfileParameters();
    int nDeleted = 0;
    int nUpdated = 0;
    int nCreated = 0;
    for (UnittypeParameter utp : upParams) {
      String upName = utp.getName();
      ProfileParameter pp = pParams.getByName(upName);
      String pValue = null;
      if (pp != null) {
        pValue = pp.getValue();
      }
      if (pp != null && req.getParameter("delete::" + upName) != null) {
        pParams.deleteProfileParameter(pp, acs);
        nDeleted++;
      } else if (pp == null && req.getParameter("create::" + upName) != null) {
        String newValue = req.getParameter("update::" + upName);
        if (newValue != null) {
          newValue = removeFromStart(newValue, '!');
        }
        pp = new ProfileParameter(profiles.getSelected(), utp, newValue);
        pParams.addOrChangeProfileParameter(pp, acs);
        nCreated++;
      } else if (pp != null && req.getParameter("update::" + upName) != null) {
        String updatedValue = req.getParameter("update::" + upName);
        if (updatedValue != null && updatedValue.equals(pValue)) {
          continue;
        }
        if (updatedValue != null) {
          updatedValue = removeFromStart(updatedValue, '!');
        }
        pp.setValue(updatedValue);
        pParams.addOrChangeProfileParameter(pp, acs);
        nUpdated++;
      }
    }
    logger.debug(
        "actionCUDParameters(): nDeleted: "
            + nDeleted
            + " / nUpdated: "
            + nUpdated
            + " / nCreated: "
            + nCreated);
    if (nDeleted > 0 || nUpdated > 0 || nCreated > 0) {
      return ProfileStatus.PROFILE_PARAMS_UPDATED;
    }
    return ProfileStatus.NONE;
  }
}
