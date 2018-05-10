package com.owera.xaps.web.app.page.unittype;

import com.owera.common.db.NoAvailableConnectionException;
import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.Unittype.ProvisioningProtocol;
import com.owera.xaps.dbi.UnittypeParameter;
import com.owera.xaps.dbi.UnittypeParameters;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.web.Page;
import com.owera.xaps.web.app.Output;
import com.owera.xaps.web.app.input.*;
import com.owera.xaps.web.app.menu.MenuItem;
import com.owera.xaps.web.app.page.AbstractWebPage;
import com.owera.xaps.web.app.util.SessionCache;
import com.owera.xaps.web.app.util.SessionData;
import com.owera.xaps.web.app.util.WebConstants;
import com.owera.xaps.web.app.util.XAPSLoader;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The Class UnittypeCreatePage.
 */
public class UnittypeCreatePage extends AbstractWebPage {

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.AbstractWebPage#getShortcutItems(com.owera.xaps.web.app.util.SessionData)
	 */
	public List<MenuItem> getShortcutItems(SessionData sessionData) {
		List<MenuItem> list = new ArrayList<MenuItem>();
		list.addAll(super.getShortcutItems(sessionData));
		list.add(new MenuItem("Unit Type overview", Page.UNITTYPEOVERVIEW));
		return list;
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.WebPage#process(com.owera.xaps.web.app.input.ParameterParser, com.owera.xaps.web.app.output.ResponseHandler)
	 */
	@Override
	public void process(ParameterParser params, Output outputHandler) throws Exception {
		UnittypeCreateData inputData = (UnittypeCreateData) InputDataRetriever.parseInto(new UnittypeCreateData(), params);

		inputData.getUnittype().setValue(null);

		String sessionId = params.getSession().getId();

		XAPS xaps = XAPSLoader.getXAPS(sessionId);
		if (xaps == null) {
			outputHandler.setRedirectTarget(WebConstants.DB_LOGIN_URL);
			return;
		}

		InputDataIntegrity.loadAndStoreSession(params, outputHandler, inputData);

		DropDownSingleSelect<Unittype> unittypesToCopyFrom = InputSelectionFactory.getDropDownSingleSelect(inputData.getUnittypeToCopyFrom(),
				xaps.getUnittype(inputData.getUnittypeToCopyFrom().getString()), getUnittypesWithProtocol(xaps, sessionId, inputData.getNewProtocol().getString()));

		if (inputData.getFormSubmit().hasValue("Create")) {
			if (isUnittypesLimited(sessionId)) {
				outputHandler.setDirectResponse("You are not allowed to create unittypes!");
				return;
			}
			String description = inputData.getNewDescription().getString();
			String modelName = inputData.getNewModelname().getString();
			String protocol = inputData.getNewProtocol().getString();
			String vendor = inputData.getNewVendor().getString();
			//			String matcherId = modelName;
			//			if (protocol != null && protocol.equals("OPP"))
			//				matcherId = inputData.getNewMatcherid().getString();

			if (vendor != null && modelName != null) {

				String copyFrom = inputData.getUnittypeToCopyFrom().getString();
				Unittype unittypeToCopyFrom = null;
				if (copyFrom != null)
					unittypeToCopyFrom = xaps.getUnittype(copyFrom);

				if (unittypeToCopyFrom != null) {
					if (!unittypeToCopyFrom.getProtocol().equals(protocol)) {
						outputHandler.setDirectResponse("Cannot copy parameters from a unittype of different protocol.");
						return;
					}
				}

				Unittype unittype = new Unittype(modelName, vendor, description, ProvisioningProtocol.toEnum(protocol));
				xaps.getUnittypes().addOrChangeUnittype(unittype, xaps);

				if (unittypeToCopyFrom != null) {
					for (UnittypeParameter utp : getParametersFromUnittype(unittypeToCopyFrom).getUnittypeParameters()) {
						if (unittype.getUnittypeParameters().getByName(utp.getName()) == null) {
							UnittypeParameter newUtp = new UnittypeParameter(unittype, utp.getName(), utp.getFlag());
							if (utp.getValues() != null)
								newUtp.setValues(utp.getValues());
							unittype.getUnittypeParameters().addOrChangeUnittypeParameter(newUtp, xaps);
						}
					}
				}

				inputData.getUnittype().setValue(unittype.getName());
				SessionCache.getSessionData(sessionId).setUnittypeName(unittype.getName());

				outputHandler.setDirectToPage(Page.UNITTYPE);
				return;
			}
		}

		outputHandler.getTemplateMap().put("unittypesInProtocol", unittypesToCopyFrom);
		DropDownSingleSelect<String> protocols = InputSelectionFactory.getDropDownSingleSelect(inputData.getNewProtocol(), inputData.getNewProtocol().getString(), Arrays.asList(UnittypePage.NA_PROTOCOL, UnittypePage.TR069_PROTOCOL));
		outputHandler.getTemplateMap().put("protocols", protocols);

		outputHandler.setTemplatePath("unit-type/create");
	}

	/**
	 * Gets the parameters from unittype.
	 *
	 * @param unittypeToCopyFrom the unittype to copy from
	 * @return the parameters from unittype
	 */
	private UnittypeParameters getParametersFromUnittype(Unittype unittypeToCopyFrom) {
		if (unittypeToCopyFrom == null)
			return null;
		UnittypeParameters unittypeParameters = unittypeToCopyFrom.getUnittypeParameters();
		return unittypeParameters;
	}

	/**
	 * Gets the unittypes with protocol.
	 *
	 * @param xaps the xaps
	 * @param sessionId the session id
	 * @param protocol the protocol
	 * @return the unittypes with protocol
	 * @throws NoAvailableConnectionException the no available connection exception
	 * @throws SQLException the sQL exception
	 */
	private List<Unittype> getUnittypesWithProtocol(XAPS xaps, String sessionId, String protocol) throws NoAvailableConnectionException, SQLException {
		List<Unittype> unittypes = getAllowedUnittypes(sessionId);
		List<Unittype> allowedUnittypes = new ArrayList<Unittype>();
		if (protocol == null)
			protocol = UnittypePage.NA_PROTOCOL;
		for (Unittype ut : unittypes) {
			if (ut.getProtocol().equals(protocol))
				allowedUnittypes.add(ut);
		}
		return allowedUnittypes;
	}
}
