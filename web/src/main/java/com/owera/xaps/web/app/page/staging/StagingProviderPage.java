package com.owera.xaps.web.app.page.staging;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import com.owera.common.db.NoAvailableConnectionException;
import com.owera.xaps.dbi.File;
import com.owera.xaps.dbi.FileType;
import com.owera.xaps.dbi.Group;
import com.owera.xaps.dbi.Job;
import com.owera.xaps.dbi.JobFlag.JobType;
import com.owera.xaps.dbi.Jobs;
import com.owera.xaps.dbi.Profile;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.dbi.util.SystemParameters;
import com.owera.xaps.web.Page;
import com.owera.xaps.web.app.Output;
import com.owera.xaps.web.app.input.DropDownSingleSelect;
import com.owera.xaps.web.app.input.InputData;
import com.owera.xaps.web.app.input.InputDataIntegrity;
import com.owera.xaps.web.app.input.InputDataRetriever;
import com.owera.xaps.web.app.input.InputSelectionFactory;
import com.owera.xaps.web.app.input.ParameterParser;
import com.owera.xaps.web.app.util.WebConstants;
import com.owera.xaps.web.app.util.XAPSLoader;

/**
 * The Class StagingProviderPage.
 */
public class StagingProviderPage extends StagingActions {

	/** The input data. */
	private StagingProviderData inputData;

	/** The xaps. */
	private XAPS xaps;

	/** The session id. */
	private String sessionId;

	/** The distributors. */
	private DropDownSingleSelect<Unittype> distributors;

	/** The providers. */
	private DropDownSingleSelect<Profile> providers;

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.WebPage#process(com.owera.xaps.web.app.input.ParameterParser, com.owera.xaps.web.app.output.ResponseHandler)
	 */
	public void process(ParameterParser params, Output outputHandler) throws Exception {
		inputData = (StagingProviderData) InputDataRetriever.parseInto(new StagingProviderData(), params);

		sessionId = params.getSession().getId();

		xaps = XAPSLoader.getXAPS(sessionId);
		if (xaps == null) {
			outputHandler.setRedirectTarget(WebConstants.DB_LOGIN_URL);
			return;
		}

		InputDataIntegrity.loadAndStoreSession(params, outputHandler, inputData, inputData.getUnittype(), inputData.getProfile());

		/**
		 * Prepare necessary objects needed for further processing
		 */
		distributors = InputSelectionFactory.getUnittypeSelection(inputData.getUnittype(), xaps);
		Profile provider = distributors.getSelected() != null && inputData.getProfile().notNullNorValue("Default") ? distributors.getSelected().getProfiles()
				.getByName(inputData.getProfile().getString()) : null;
		providers = InputSelectionFactory.getDropDownSingleSelect(inputData.getProfile(), provider, getAllowedProfilesExceptDefault(distributors.getSelected(), sessionId));
		List<File> files = distributors.getSelected() != null ? Arrays.asList(distributors.getSelected().getFiles().getFiles()) : null;
		File fromFile = distributors.getSelected() != null ? distributors.getSelected().getFiles().getByVersionType(inputData.getFromSoftware().getString(), FileType.SOFTWARE) : null;
		DropDownSingleSelect<File> fromSoftware = InputSelectionFactory.getDropDownSingleSelect(inputData.getFromSoftware(), fromFile, files);
		File toFile = distributors.getSelected() != null ? distributors.getSelected().getFiles().getByVersionType(inputData.getToSoftware().getString(), FileType.SOFTWARE) : null;
		DropDownSingleSelect<File> toSoftware = InputSelectionFactory.getDropDownSingleSelect(inputData.getToSoftware(), toFile, files);

		Map<String, Object> root = outputHandler.getTemplateMap();

		/**
		 * If distributor and provider is selected, get shipments and jobs, and add them to the root map
		 */
		if (distributors.getSelected() != null && providers.getSelected() != null) {
			if (!inputData.isAddOperation() && !inputData.isUpdateOperation()) // If not add or update operation
				inputData.populateFieldData(distributors.getSelected(), providers.getSelected());
			DropDownSingleSelect<Shipment> shipments = getShipments(distributors.getSelected(), providers.getSelected(), inputData.getShipment(), xaps);
			List<Job> jobs = getJobs(distributors.getSelected(), providers.getSelected());
			root.put("shipments", shipments);
			root.put("jobs", jobs);
		}

		String actionResponse = null;
		if (inputData.isDeleteOperation()) // We dont need no validation for this use case
			actionResponse = actionDeleteProvider(distributors.getSelected(), providers.getSelected(), params.getSession());
		else if (inputData.isAddOperation() && inputData.bindAndValidate(root))
			actionResponse = actionCreateProvider(distributors.getSelected(), providers.getSelected(), params.getSession());
		else if (inputData.isUpdateOperation() && inputData.bindAndValidate(root))
			actionResponse = actionUpdateProvider(distributors.getSelected(), providers.getSelected());
		else if (inputData.isAddUpgradeJobOperation() && providers.getSelected() != null)
			actionResponse = actionCreateProvider(distributors.getSelected(), providers.getSelected(), params.getSession());
		else if (inputData.getFormSubmit().notNullNorValue("")) // A button was pressed but validation failed
			errors.putAll(inputData.getErrors()); // Add all validation errors to the staging specific error map
		else
			// No button was pressed, bind the current inputdata to the form
			inputData.bindForm(root);

		/**
		 * An action has been processed
		 */
		if (actionResponse == null) {
			actionResponse = (String) params.getSession().getAttribute(InputData.INFO);
			root.put("outputHandler", actionResponse);
		} else {
			params.getSession().setAttribute(InputData.INFO, actionResponse);
			String url = null;
			if (providers.getSelected() != null)
				url += "&profile=" + providers.getSelected().getName();
			outputHandler.setDirectToPage(Page.STAGINGPROVIDERS, url); // Avoid postback warnings
			return;
		}

		root.put("unittypes", distributors);
		root.put("profiles", providers);
		root.put("fromsoftware", fromSoftware);
		root.put("tosoftware", toSoftware);

		root.put("warnings", generateWarningList());
		root.put("errors", generateErrorList());

		boolean isProfilesLimited = isProfilesLimited(distributors.getSelected(), sessionId);

		root.put("DESIRED_SOFTWARE_VERSION", SystemParameters.DESIRED_SOFTWARE_VERSION);
		root.put("PROFILES_LIMITED", isProfilesLimited);

		root.put("providerDefaultOption", isProfilesLimited ? "Select " : "Create ");

		root.put("groupsize", new GetGroupSizeMethod(distributors.getSelected(), sessionId));
		root.put("firstindex", new FirstIndexOfMethod());

		outputHandler.setTemplatePathWithIndex("providers");
	}

	/**
	 * Action create provider.
	 *
	 * @param unittype the unittype
	 * @param profile the profile
	 * @param session the session
	 * @return the string
	 * @throws Exception the exception
	 */
	private String actionCreateProvider(Unittype unittype, Profile profile, HttpSession session) throws Exception {
		if (unittype == null)
			return "Unittype is null";

		String providername = null;
		if (profile == null && (providername = inputData.getProviderName().getString()) == null) {
			return "Please specify the unittype provider name.";
		} else if (inputData.getProviderEmail().getError() != null) {
			return "Incorrect email format.";
		} else if (profile != null) {
			providername = profile.getName();
		}

		String fromSoftware = inputData.getFromSoftware().getString();

		String toSoftware = inputData.getToSoftware().getString();

		String provider_wsurl = inputData.getProviderWsurl().getString();
		String provider_wsuser = inputData.getProviderWsuser().getString();
		String provider_wspass = inputData.getProviderWspass().getString();
		String provider_email = inputData.getProviderEmail().getEmail();
		String provider_unittype = inputData.getProviderUnittype().getString();
		String provider_profile = inputData.getProviderProfile().getString();
		//		String provider_serialnumber = inputData.getProviderSerial().getString();
		//		String provider_protocol = inputData.getProviderProtocol().getString();
		//		String provider_secret = inputData.getProviderSecret().getString();

		WebServiceParams params = new WebServiceParams(provider_wsurl, provider_wsuser, provider_wspass, provider_email, provider_unittype, provider_profile/*,provider_serialnumber, provider_protocol,provider_secret*/);

		String outputHandlerToReturn = null;

		try {
			actionCreateProvider(unittype, sessionId, providername, params, fromSoftware, toSoftware);

			if (errors.size() == 0 && warnings.size() == 0)
				outputHandlerToReturn = "Successfully created new upgrade job";
			else if (errors.size() == 0)
				outputHandlerToReturn = "Successfully created new upgrade job, but some warnings occured.";
			else
				return null;
			providers.setSelected(unittype.getProfiles().getByName(providername));
		} catch (Exception e) {
			outputHandlerToReturn = "Error occured while creating new upgrade job: " + providername + " -> " + e.getLocalizedMessage();
		}

		return outputHandlerToReturn;
	}

	/**
	 * Action update provider.
	 *
	 * @param unittype the unittype
	 * @param profile the profile
	 * @return the string
	 * @throws SQLException the sQL exception
	 * @throws NoAvailableConnectionException the no available connection exception
	 */
	private String actionUpdateProvider(Unittype unittype, Profile profile) throws SQLException, NoAvailableConnectionException {
		setProfileParameter(SystemParameters.STAGING_PROVIDER_WSURL, inputData.getProviderWsurl().getString(), unittype, profile, xaps);
		setProfileParameter(SystemParameters.STAGING_PROVIDER_WSUSER, inputData.getProviderWsuser().getString(), unittype, profile, xaps);
		setProfileParameter(SystemParameters.STAGING_PROVIDER_WSPASSWORD, inputData.getProviderWspass().getString(), unittype, profile, xaps);
		setProfileParameter(SystemParameters.STAGING_PROVIDER_EMAIL, inputData.getProviderEmail().getEmail(), unittype, profile, xaps);
		setProfileParameter(SystemParameters.STAGING_PROVIDER_UNITTYPE, inputData.getProviderUnittype().getString(), unittype, profile, xaps);
		setProfileParameter(SystemParameters.STAGING_PROVIDER_PROFILE, inputData.getProviderProfile().getString(), unittype, profile, xaps);
		//		setProfileParameter(SystemParameters.STAGING_PROVIDER_SNPARAMETER, inputData.getProviderSerial().getString(), unittype, profile, xaps);
		//		setProfileParameter(SystemParameters.STAGING_PROVIDER_PROTOCOL, inputData.getProviderProtocol().getString(), unittype, profile, xaps);
		//		setProfileParameter(SystemParameters.STAGING_PROVIDER_SECPARAMETER, inputData.getProviderSecret().getString(), unittype, profile, xaps);
		return "Successfully updated provider";
	}

	/**
	 * Action delete provider.
	 *
	 * @param unittype the unittype
	 * @param profile the profile
	 * @param session the session
	 * @return the string
	 * @throws Exception the exception
	 */
	private String actionDeleteProvider(Unittype unittype, Profile profile, HttpSession session) throws Exception {
		if (unittype != null && profile != null) {
			actionDeleteProvider(unittype, profile, session.getId());
			if (errors.size() == 0 && warnings.size() == 0) {
				inputData.clearInputs();
				return "Successfully deleted provider, " + profile.getName();
			} else if (errors.size() == 0) {
				inputData.clearInputs();
				return "Successfully deleted provider, " + profile.getName() + ", but some warnings occured.";
			} else {
				return "Errors occured";
			}
		}
		return null;
	}

	/**
	 * Gets the jobs.
	 *
	 * @param unittype the unittype
	 * @param profile the profile
	 * @return the jobs
	 * @throws Exception the exception
	 */
	private List<Job> getJobs(Unittype unittype, Profile profile) throws Exception {
		Jobs jobs = unittype.getJobs();
		List<Job> upgradeJobs = new ArrayList<Job>();
		for (Group group : unittype.getGroups().getGroups()) {
			if (group.getProfile() == profile) {
				Job[] jobsArr = jobs.getGroupJobs(group.getId());
				for (Job job : jobsArr) {
					if (job.getFlags().getType() == JobType.SOFTWARE)
						upgradeJobs.add(job);
				}
			}
		}
		return upgradeJobs;
	}

}
