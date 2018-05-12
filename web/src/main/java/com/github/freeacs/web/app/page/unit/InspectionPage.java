package com.github.freeacs.web.app.page.unit;

import com.github.freeacs.dbi.*;
import com.github.freeacs.web.app.input.*;
import com.github.freeacs.web.app.util.SessionCache;
import com.github.freeacs.web.app.util.XAPSLoader;
import com.owera.xaps.dbi.*;
import com.github.freeacs.dbi.util.SystemParameters;
import com.owera.xaps.web.app.Output;
import com.owera.xaps.web.app.input.*;
import com.owera.xaps.web.app.page.AbstractWebPage;
import com.owera.xaps.web.app.util.SessionCache;
import com.owera.xaps.web.app.util.XAPSLoader;

import java.util.Arrays;
import java.util.Map;



/**
 * Used by the {@link UnitPage} to continuously poll for changes in the inspection mode and state.
 * 
 * @author Jarl Andre Hubenthal
 */
public class InspectionPage extends AbstractWebPage {
	
	/** The xaps. */
	private XAPS xaps;
	
	/** The xaps unit. */
	private XAPSUnit xapsUnit;
	
	/** The unit. */
	private Unit unit;
	
	/** The profile. */
	private Profile profile;
	
	/** The unittype. */
	private Unittype unittype;
	
	/** The session id. */
	private String sessionId;
	
	/** The input data. */
	private InspectionData inputData;
	
	/**
	 * Action populate.
	 *
	 * @param sessionId the session id
	 * @throws Exception the exception
	 */
	private void actionPopulate(String sessionId) throws Exception {
		unit = null;
		profile = null;
		unittype = null;
		if (inputData.getUnit().getString() != null) {
			unit = xapsUnit.getUnitById(inputData.getUnit().getString());
			SessionCache.putUnit(sessionId, unit);
			if (unit != null) {
				profile = unit.getProfile();
				if (inputData.getProfile().getString() == null)
					inputData.getProfile().setValue(profile.getName());
				unittype = unit.getUnittype();
				inputData.getUnittype().setValue(unittype.getName());
			}
			if (unittype == null && inputData.getUnittype().getString() != null) {
				unittype = xaps.getUnittype(inputData.getUnittype().getString());
			}
			if (profile == null && inputData.getProfile().getString()!= null && unittype != null) {
				profile = unittype.getProfiles().getByName(inputData.getProfile().getString());
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.WebPage#process(com.owera.xaps.web.app.input.ParameterParser, com.owera.xaps.web.app.output.ResponseHandler)
	 */
	public void process(ParameterParser params, Output res) throws Exception {
		inputData = (InspectionData) InputDataRetriever.parseInto(new InspectionData(), params);
		res.setContentType("text/html");
		try {
			sessionId = params.getSession().getId();

			xaps = XAPSLoader.getXAPS(sessionId);
			xapsUnit = XAPSLoader.getXAPSUnit(sessionId);

			if (xaps == null || xapsUnit == null) {
				throw new Exception("Could not load xaps objects!");
			}
			
			InputDataIntegrity.rememberAndCheck(params.getSession().getId(), inputData.getUnittype(), inputData.getProfile());

			actionPopulate(sessionId);
			
			String message = null;
			if(unit!=null && profile!=null){
				UnitParameter up = unit.getUnitParameters().get(SystemParameters.INSPECTION_MESSAGE);
				if(up!=null && !up.getValue().equals("N/A")){
					message = up.getValue();
					up.setValue("N/A");
					xapsUnit.addOrChangeUnitParameters(Arrays.asList(new UnitParameter[]{up}), profile);
				}
			}

			UnitParameter mode = unit.getUnitParameters().get(SystemParameters.PROVISIONING_MODE);
//			UnitParameter state = unit.getUnitParameters().get(SystemParameters.PROVISIONING_STATE);
			if (mode == null /*|| state == null*/)
				res.setDirectResponse("relo");
			else {
				String modeString = inputData.getMode().getString();
//				String stateString = inputData.getState().getString();
				if (mode.getParameter().getValue().equals(modeString) /*&& state.getParameter().getValue().equals(stateString)*/)
					res.setDirectResponse("wait" + (message != null && (message.trim().length() > 0) ? message : ""));
				else {
					res.setDirectResponse("relo");
				}
			}
		} catch (Throwable e) {
			res.setDirectResponse(e.getLocalizedMessage());
		}
	}

	/**
	 * The Class InspectionData.
	 */
	public class InspectionData extends InputData {

		/** The mode. */
		private Input mode = Input.getStringInput("mode");
		
		/** The state. */
		private Input state = Input.getStringInput("state");

		/**
		 * Gets the mode.
		 *
		 * @return the mode
		 */
		public Input getMode() {
			return this.mode;
		}

		/**
		 * Sets the mode.
		 *
		 * @param mode the new mode
		 */
		public void setMode(Input mode) {
			this.mode = mode;
		}

		/**
		 * Gets the state.
		 *
		 * @return the state
		 */
		public Input getState() {
			return this.state;
		}

		/**
		 * Sets the state.
		 *
		 * @param state the new state
		 */
		public void setState(Input state) {
			this.state = state;
		}

		/* (non-Javadoc)
		 * @see com.owera.xaps.web.app.input.InputData#bindForm(java.util.Map)
		 */
		@Override
		public void bindForm(Map<String, Object> root) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see com.owera.xaps.web.app.input.InputData#validateForm()
		 */
		@Override
		public boolean validateForm() {
			// TODO Auto-generated method stub
			return false;
		}
	}

}
