package com.owera.xaps.web.app.page.unit;

import com.owera.common.db.NoAvailableConnectionException;
import com.owera.xaps.dbi.*;
import com.owera.xaps.web.app.util.WebConstants;
import com.owera.xaps.web.app.util.XAPSLoader;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * Performs parameter related tasks such as retrieving and storing parameter values.
 * 
 * @author Jarl André Hübebthal
 *
 */
public class Parameters {
	/**
	 * Lazy parameter retriever.
	 * 
	 * Get unit parameter value by using the full parameter name, or by excluding the keyroot.
	 * 
	 * @param unit The unit to retreive from
	 * @param key The key, either with or without keyroot. The function will automatically try all known keyroots if the provided key is not valid (returns no data).
	 * 
	 * @return A string representing the value, could be anything from number to password.
	 */
	public static String getUnitParameterValue(Unit unit,String key){
		String value = unit.getParameters().get(key);
		if(value==null){
			value = unit.getParameters().get(WebConstants.KEYROOT_INTERNET_GATEWAY_DEVICE+key);
			if(value==null)
				value = unit.getParameters().get(WebConstants.KEYROOT_DEVICE+key);
			if(value==null)
				value = unit.getParameters().get(WebConstants.KEYROOT_SYSTEM+key);
		}
		return value;
	}
	
	/**
	 * Adds or changes a single unit parameter.
	 *
	 * @param key the key
	 * @param value the value
	 * @param unit the unit
	 * @param sessionId the session id
	 * @throws NoAvailableConnectionException the no available connection exception
	 * @throws SQLException the sQL exception
	 */
	public static void setUnitParameterValue(String key,String value,Unit unit,String sessionId) throws NoAvailableConnectionException, SQLException{
		XAPS xaps = XAPSLoader.getXAPS(sessionId);
		XAPSUnit xapsUnit = XAPSLoader.getXAPSUnit(sessionId);
		
		List<UnitParameter> toAddOrChange = new ArrayList<UnitParameter>();
		
		UnitParameter parameterToUpdate = unit.getUnitParameters().get(key);
		
		if(parameterToUpdate==null){
			UnittypeParameter unittypeParameter = xaps.getUnittypeParameter(unit.getProfile().getUnittype().getName(), key);
			Parameter parameter = new Parameter(unittypeParameter, value);
			parameterToUpdate = new UnitParameter(parameter,unit.getId(),unit.getProfile());
		}else
			parameterToUpdate.setValue(value);
		
		toAddOrChange.add(parameterToUpdate);
		
		xapsUnit.addOrChangeUnitParameters(toAddOrChange, unit.getProfile());
	}
}