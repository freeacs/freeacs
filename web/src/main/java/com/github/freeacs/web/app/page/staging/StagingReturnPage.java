package com.github.freeacs.web.app.page.staging;

import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.XAPS;
import com.github.freeacs.web.app.input.*;
import com.github.freeacs.web.app.util.SessionCache;
import com.github.freeacs.web.app.util.WebConstants;
import com.github.freeacs.web.app.util.XAPSLoader;
import com.owera.xaps.web.app.Output;
import com.owera.xaps.web.app.input.*;
import com.owera.xaps.web.app.util.SessionCache;
import com.owera.xaps.web.app.util.WebConstants;
import com.owera.xaps.web.app.util.XAPSLoader;
import org.apache.commons.fileupload.FileItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;



/**
 * The Class StagingReturnPage.
 */
public class StagingReturnPage extends StagingActions {

	/** The input data. */
	private StagingReturnData inputData;
	
	/** The session id. */
	private String sessionId;
	
	/** The xaps. */
	private XAPS xaps;

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.WebPage#process(com.owera.xaps.web.app.input.ParameterParser, com.owera.xaps.web.app.output.ResponseHandler)
	 */
	public void process(ParameterParser params, Output outputHandler) throws Exception {
		inputData = (StagingReturnData) InputDataRetriever.parseInto(new StagingReturnData(), params);

		sessionId = params.getSession().getId();
		
		xaps = XAPSLoader.getXAPS(sessionId);
		if (xaps == null) {
			outputHandler.setRedirectTarget(WebConstants.DB_LOGIN_URL);
			return;
		}
		
		InputDataIntegrity.loadAndStoreSession(params,outputHandler,inputData, inputData.getUnittype(), inputData.getProfile());
		
		DropDownSingleSelect<Unittype> distributors = InputSelectionFactory.getUnittypeSelection(inputData.getUnittype(), xaps);
		Profile provider = distributors.getSelected()!=null&&inputData.getProfile().notNullNorValue("Default")?distributors.getSelected().getProfiles().getByName(inputData.getProfile().getString()):null;
		DropDownSingleSelect<Profile> providers = InputSelectionFactory.getDropDownSingleSelect(inputData.getProfile(), provider, getAllowedProfilesExceptDefault(distributors.getSelected(), sessionId));
		
		Map<String, Object> root = outputHandler.getTemplateMap();
		
		String actionResponse = null;
		if (inputData.getFormSubmit().hasValue("Return units") && inputData.validateReturnShipment()) {
			actionResponse = actionCreateReturn(distributors.getSelected(), providers.getSelected(), params, root);
		}else if (inputData.getFormSubmit().hasValue("Confirm return")) {
			actionResponse = confirmReturn(distributors.getSelected());
		} else{
			for(Entry<String,String> error: inputData.getErrors().entrySet())
				errors.put(error.getKey(), error.getValue());
		}
		
		root.put("unittypes",distributors);
		root.put("profiles",providers);
		root.put("errors", generateErrorList());
		root.put("warnings", generateWarningList());
		root.put("outputHandler", actionResponse);
		
		inputData.bindForm(root);
		
		outputHandler.setTemplatePathWithIndex("return");
	}

	/**
	 * Confirm return.
	 *
	 * @param unittype the unittype
	 * @return the string
	 */
	private String confirmReturn(Unittype unittype) {
		try {
			ShipmentCache shipment = actionConfirmReturn(unittype, unittype.getProfiles().getByName("Default"),sessionId);
			SessionCache.getSessionData(sessionId).setShipmentCache(null);
			return "Successfully returned "+shipment.getUnits().size()+" units";
		} catch (Exception e) {
			return "Error occured while trying to return units:<br />" + e.getLocalizedMessage();
		}
	}

	/**
	 * Action create return.
	 *
	 * @param unittype the unittype
	 * @param profile the profile
	 * @param params the params
	 * @param root the root
	 * @return the string
	 */
	private String actionCreateReturn(Unittype unittype, Profile profile, ParameterParser params, Map<String, Object> root) {
		if (unittype == null)
			return "Unittype is null";
		if (profile == null)
			return "Profile is null";
		
		List<String> units = new ArrayList<String>();

		try {
			FileItem item = inputData.getUnitList().getFile();
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
				return "Error while creating return shipment: no units was uploaded.";
			else {
				ShipmentCache shipment = actionCreateReturn(unittype, profile, sessionId, units);
				SessionCache.getSessionData(sessionId).setShipmentCache(shipment);
				root.put("confirmshipment", shipment);
			}
		} catch (Exception e) {
			return "Error while creating return shipment: "+e.getLocalizedMessage();
		}
		
		return null;
	}

}
