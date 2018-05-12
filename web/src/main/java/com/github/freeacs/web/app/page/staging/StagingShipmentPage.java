package com.github.freeacs.web.app.page.staging;

import com.github.freeacs.dbi.*;
import com.github.freeacs.web.app.input.*;
import com.github.freeacs.web.app.util.SessionCache;
import com.github.freeacs.web.app.util.WebConstants;
import com.github.freeacs.web.app.util.XAPSLoader;
import com.owera.xaps.dbi.*;
import com.owera.xaps.web.app.Output;
import com.owera.xaps.web.app.input.*;
import com.owera.xaps.web.app.util.SessionCache;
import com.owera.xaps.web.app.util.WebConstants;
import com.owera.xaps.web.app.util.XAPSLoader;
import org.apache.commons.fileupload.FileItem;

import javax.mail.MessagingException;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;



/**
 * The Class StagingShipmentPage.
 */
public class StagingShipmentPage extends StagingActions {
	
	/** The input data. */
	private StagingShipmentData inputData;
	
	/** The xaps. */
	private XAPS xaps;
	
	/** The session id. */
	private String sessionId;
	
	/** The shipment name format. */
	private static SimpleDateFormat shipmentNameFormat = new SimpleDateFormat("yyyy-MM");

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.WebPage#process(com.owera.xaps.web.app.input.ParameterParser, com.owera.xaps.web.app.output.ResponseHandler)
	 */
	public void process(ParameterParser params, Output outputHandler) throws Exception {
		inputData = (StagingShipmentData) InputDataRetriever.parseInto(new StagingShipmentData(), params);

		sessionId = params.getSession().getId();

		xaps = XAPSLoader.getXAPS(sessionId);
		if (xaps == null) {
			outputHandler.setRedirectTarget(WebConstants.DB_LOGIN_URL);
			return;
		}

		InputDataIntegrity.loadAndStoreSession(params,outputHandler,inputData, inputData.getUnittype(), inputData.getProfile());

		DropDownSingleSelect<Unittype> distributors = InputSelectionFactory.getUnittypeSelection(inputData.getUnittype(), xaps);
		Profile provider = distributors.getSelected() != null && inputData.getProfile().notNullNorValue("Default") ? distributors.getSelected().getProfiles().getByName(inputData.getProfile().getString()) : null;
		List<Profile> profiles = new ArrayList<Profile>(getAllowedProfiles(sessionId, distributors.getSelected()));
		if(distributors.getSelected()!=null)
			profiles.remove(distributors.getSelected().getProfiles().getByName("Default"));
		DropDownSingleSelect<Profile> providers = InputSelectionFactory.getDropDownSingleSelect(inputData.getProfile(), provider, profiles);

		Map<String, Object> root = outputHandler.getTemplateMap();

		String actionResponse = null;
		if (inputData.getFormSubmit().hasValue("Add new shipment") && inputData.validateAddShipment()) {
			actionResponse = actionCreateShipment(distributors.getSelected(), providers.getSelected(), params, params.getSession(), root);
		} else if (inputData.getFormSubmit().hasValue("Confirm shipment")) {
			actionResponse = confirmShipment(distributors.getSelected(), providers.getSelected(), sessionId);
		} else if (inputData.getFormSubmit().hasValue("Cancel shipment")) {
			actionResponse = cancelShipment();
		} else if (inputData.getFormSubmit().hasValue("cancelunit") && inputData.validateCancelUnit()) { // A link with &formsubmit=cancelunit
			actionResponse = cancelShipmentOfAUnit(distributors.getSelected(), providers.getSelected(), sessionId);
		} else {
			for (Entry<String, String> error : inputData.getErrors().entrySet())
				errors.put(error.getKey(), error.getValue());
		}

		root.put("unittypes", distributors);
		root.put("profiles", providers);
		root.put("errors", generateErrorList());
		root.put("warnings", generateWarningList());
		root.put("response", actionResponse);
		root.put("firstindex", new FirstIndexOfMethod());
		root.put("lastindex", new LastIndexOfMethod());

		inputData.bindForm(root);

		displayShipments(distributors.getSelected(), providers.getSelected(), root);

		outputHandler.setTemplatePathWithIndex("shipments");
	}

	/**
	 * Cancel shipment.
	 *
	 * @return the string
	 */
	private String cancelShipment() {
		SessionCache.getSessionData(sessionId).setShipmentCache(null);
		return "Successfully canceled shipment";
	}

	/**
	 * Cancel shipment of a unit.
	 *
	 * @param unittype the unittype
	 * @param profile the profile
	 * @param sessionId the session id
	 * @return the string
	 */
	private String cancelShipmentOfAUnit(Unittype unittype, Profile profile, String sessionId) {
		try {
			String unitId = inputData.getUnit().getString();
			XAPSUnit xapsUnit = XAPSLoader.getXAPSUnit(sessionId);
			Profile defaultProfile = unittype.getProfiles().getByName("Default");
			List<String> unitIds = new ArrayList<String>();
			unitIds.add(unitId);
			xapsUnit.moveUnits(unitIds, defaultProfile);
			return "Successfully cancelled shipment of UnitId " + unitId + ".";
		} catch (Exception e) {
			return "Error occurred while trying to cancel a shipment of a unit";
		}
	}

	/**
	 * Confirm shipment.
	 *
	 * @param unittype the unittype
	 * @param profile the profile
	 * @param sessionId the session id
	 * @return the string
	 */
	private String confirmShipment(Unittype unittype, Profile profile, String sessionId) {
		try {
			ShipmentCache shipment = actionConfirmShipment(unittype, profile, sessionId);
			if (shipment == null) {
				return "No shipment was found to confirm";
			}
			SessionCache.getSessionData(sessionId).setShipmentCache(null);
			if (errors.size() > 0)
				return "Error occured while trying to confirm shipment [" + (shipment != null ? shipment.getShipmentname() : "") + "]<br />" + errors.get(0);
			return "Successfully created shipment [" + shipment.getShipmentname() + "]";
		} catch (MessagingException e) {
			ShipmentCache shipment = SessionCache.getSessionData(sessionId).getShipmentCache();
			return "Successfully created shipment [" + (shipment != null ? shipment.getShipmentname() : "") + "],<br /> but could not send email notification -> " + e.getLocalizedMessage();
		} catch (Throwable t) {
			ShipmentCache shipment = SessionCache.getSessionData(sessionId).getShipmentCache();
			return "Error occured while trying to confirm shipment [" + (shipment != null ? shipment.getShipmentname() : "") + "]<br />" + t.getLocalizedMessage();
		}  
	}

	/**
	 * Action create shipment.
	 *
	 * @param unittype the unittype
	 * @param profile the profile
	 * @param req the req
	 * @param session the session
	 * @param root the root
	 * @return the string
	 * @throws Exception the exception
	 */
	private String actionCreateShipment(Unittype unittype, Profile profile, ParameterParser req, HttpSession session, Map<String, Object> root) throws Exception {
		if (unittype == null)
			return "Unittype is null";
		if (profile == null)
			return "Profile is null";
		String shipmentname = inputData.getShipmentName().getString();
		if (shipmentname == null)
			shipmentname = shipmentNameFormat.format(new Date());

		List<String> units = new ArrayList<String>();

		FileItem item = null;
		try {
			item = inputData.getUnitList().getFile();
			BufferedReader br = new BufferedReader(new InputStreamReader(item.getInputStream()));
			while (br.ready()) {
				String unit = br.readLine();
				if (unit != null && (unit = unit.trim()).length() > 0) {
					unit = unit.split("\\s+")[0];
					units.add(unit.trim());
				}
			}
		} catch (IOException ex) {
			return "Could not read uploaded file: " + ex.getLocalizedMessage();
		}
		if (units.size() == 0) {
			String mac = inputData.getMac().getString();
			if (mac == null) {
				return "No MAC found in neither file nor MAC field";
			} else {
				mac = mac.replace(":", "");
				units.add(mac.trim());
			}
		}

		try {
			if (units.size() == 0)
				return "Error while creating shipment " + shipmentname + ": no units was uploaded.";
			else {
				ShipmentCache shipment = actionCreateShipment(unittype, profile, sessionId, shipmentname, units, true);
				SessionCache.getSessionData(sessionId).setShipmentCache(shipment);
				root.put("confirmshipment", shipment);
				root.put("shipmentname", shipmentname);
			}
		} catch (Exception e) {
			return "Error while creating shipment: " + shipmentname + " -> " + e.getLocalizedMessage();
		}

		return null;
	}

	/**
	 * Display shipments.
	 *
	 * @param unittype the unittype
	 * @param profile the profile
	 * @param root the root
	 * @throws Exception the exception
	 */
	private void displayShipments(Unittype unittype, Profile profile, Map<String, Object> root) throws Exception {
		if (unittype != null) {
			if (profile != null) {
				root.put("shipments", getShipments(unittype, profile, inputData.getShipment(), xaps));
				String shipmentname = inputData.getShipment().getString();
				String shipmentname_short = getShipmentName(shipmentname);
				Group shipmentGroup = unittype.getGroups().getByName(shipmentname_short);
				if (unittype != null && profile != null && isValidString(shipmentname) && shipmentGroup != null && shipmentGroup.getTopParent().getProfile() == profile) {
					List<ShippedUnit> shippedUnits = actionRetrieveShipmentData(unittype, profile, sessionId, shipmentname_short, SessionCache.getXAPSConnectionProperties(sessionId));
					Collections.sort(shippedUnits, new ShippedUnitComparator());
					Map<String, Object> details = new HashMap<String, Object>();
					details.put("full", shipmentname);
					details.put("name", shipmentname_short);
					details.put("list", shippedUnits);
					details.put("listsize", shippedUnits.size());
					root.put("units", details);
				}
			}
		}
	}

	/**
	 * Gets the shipment name.
	 *
	 * @param shipmentname the shipmentname
	 * @return the shipment name
	 */
	private String getShipmentName(String shipmentname) {
		if (shipmentname == null)
			return null;
		if (shipmentname.startsWith("canceled/"))
			return shipmentname.substring(9);
		return shipmentname;
	}

}
