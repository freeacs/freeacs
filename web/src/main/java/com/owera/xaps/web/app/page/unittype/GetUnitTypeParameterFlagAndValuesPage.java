package com.owera.xaps.web.app.page.unittype;

import org.json.JSONObject;

import com.owera.xaps.dbi.Unittype;
import com.owera.xaps.dbi.UnittypeParameter;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.web.app.Output;
import com.owera.xaps.web.app.input.ParameterParser;
import com.owera.xaps.web.app.page.AbstractWebPage;
import com.owera.xaps.web.app.util.XAPSLoader;



/**
 * Used by the javascript callback function that executes after closing the {@link UnittypeParametersPage} dialog.
 * If the {@link UnittypeParametersPage} dialog has changed a parameters flag, the callback function will "get the value" of the flags variable on the parameter.
 * 
 * @author Jarl Andre Hubenthal
 */
public class GetUnitTypeParameterFlagAndValuesPage extends AbstractWebPage {

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.WebPage#process(com.owera.xaps.web.app.input.ParameterParser, com.owera.xaps.web.app.output.ResponseHandler)
	 */
	public void process(ParameterParser params, Output res) throws Exception {
		String type = params.getParameter("type");
		String name = params.getParameter("name");
		String unittype = params.getParameter("unittype");
		res.setContentType("text/html");
		if(type!=null && name!=null){
			if(type.equals("unittype") && unittype!=null){
				XAPS xaps = XAPSLoader.getXAPS(params.getSession().getId());
				Unittype ut = xaps.getUnittype(unittype);
				if(ut!=null){
					UnittypeParameter utp = ut.getUnittypeParameters().getByName(name);
					String flag = utp.getFlag().getFlag();
					boolean hasValues = (utp.getValues()!=null&&utp.getValues().getValues().size()>0?true:false);
					if(flag!=null){
						JSONObject toReturn = new JSONObject();
						toReturn.put("hasValues", hasValues);
						toReturn.put("flags", flag);
						res.setDirectResponse(toReturn.toString());
					}
				}else
					res.setDirectResponse("err: flag was not found");
			}else
				res.setDirectResponse("err: unittype is null or type is not recognized");
		}else
			res.setDirectResponse("err: specify name and type");
	}
}