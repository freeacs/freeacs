package com.owera.xaps.web.app.page.search;

import java.util.*;
import java.util.Map.Entry;
import org.json.*;
import com.owera.xaps.dbi.*;
import com.owera.xaps.dbi.Parameter.*;
import com.owera.xaps.web.Page;
import com.owera.xaps.web.app.Output;
import com.owera.xaps.web.app.input.DropDownSingleSelect;
import com.owera.xaps.web.app.input.InputDataIntegrity;
import com.owera.xaps.web.app.input.InputDataRetriever;
import com.owera.xaps.web.app.input.InputSelectionFactory;
import com.owera.xaps.web.app.input.ParameterParser;
import com.owera.xaps.web.app.page.AbstractWebPage;
import com.owera.xaps.web.app.util.CertificateVerification;
import com.owera.xaps.web.app.util.SessionCache;
import com.owera.xaps.web.app.util.WebConstants;
import com.owera.xaps.web.app.util.XAPSLoader;

/**
 * The search page.
 * 
 * @author Jarl Andre Hubenthal
 * 
 */
public class SearchPage extends AbstractWebPage {

	/** The xaps. */
	private XAPS xaps;

	/** The xaps unit. */
	private XAPSUnit xapsUnit;

	/** The input data. */
	private SearchData inputData;

	/** The group. */
	private Group group;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.owera.xaps.web.app.page.WebPage#process(com.owera.xaps.web.app.input
	 * .ParameterParser, com.owera.xaps.web.app.output.ResponseHandler)
	 */
	public void process(ParameterParser params, Output outputHandler) throws Exception {
		inputData = (SearchData) InputDataRetriever.parseInto(new SearchData(), params);
		xaps = XAPSLoader.getXAPS(params.getSession().getId());
		if (xaps == null) {
			outputHandler.setRedirectTarget(WebConstants.DB_LOGIN_URL);
			return;
		}
		xapsUnit = XAPSLoader.getXAPSUnit(params.getSession().getId());
		InputDataIntegrity.loadAndStoreSession(params, outputHandler, inputData, inputData.getUnittype(), inputData.getProfile());

		DropDownSingleSelect<Unittype> unittypes = InputSelectionFactory.getUnittypeSelection(inputData.getUnittype(), xaps);
		DropDownSingleSelect<Profile> profiles = InputSelectionFactory.getProfileSelection(inputData.getProfile(), inputData.getUnittype(), xaps);

		Map<String, Object> map = outputHandler.getTemplateMap();

		boolean advanced = inputData.getAdvanced().getBoolean();

		if (unittypes.getSelected() != null && advanced)
			prepareSearchableParameters(map, params, unittypes.getSelected());
		
		map.put("unit", inputData.getUnit());
		map.put("unittypes", unittypes);
		map.put("profiles", profiles);
		map.put("unitparam", inputData.getUnitParamValue());
		map.put("dummy", WebConstants.ALL_ITEMS_OR_DEFAULT);
		map.put("cmd", inputData.getCmd());
		map.put("advanced", advanced);
		map.put("operators", Parameter.Operator.values());
		map.put("datatypes", Parameter.ParameterDataType.values());
		/* Morten jan 2014 - certificate checks are not necessary after going open-source */
		boolean isReportCertValid = true/*CertificateVerification.isCertificateValid(Certificate.CERT_TYPE_REPORT, params.getSession().getId())*/;
		boolean isReportAllowed = true/*SessionCache.getSessionData(params.getHttpServletRequest().getSession().getId()).getUser().isReportsAllowed()*/;
		map.put("reportvalid", isReportCertValid && isReportAllowed);

		String cmd = params.getParameter("cmd");

		if (inputData.getFormSubmit().isValue("Search")) {
			int limit = inputData.getLimit().getInteger(100);
			List<Unit> result = getSearchResults(limit, unittypes.getSelected(), profiles.getSelected(), params);
			result = xapsUnit.getUnitsWithParameters(unittypes.getSelected(),
					profiles.getSelected(),	result);
			List<SearchResultWrapper> wrappedResults = new ArrayList<>();
			for (Unit u : result) {
				SearchResultWrapper srw = new SearchResultWrapper();
				srw.unit = u;
				wrappedResults.add(srw);
			}
			if (cmd != null && cmd.equals("follow-single-unit")) {
				if (result != null && result.size() == 1) {
					outputHandler.setDirectToPage(Page.UNITSTATUS, "unit=" + result.get(0).getId(), "profile=" + result.get(0).getProfile().getName(), "unittype=" + result.get(0).getUnittype().getName());
					return;
				}
			}

			// The contents of the following if is only relevant if there are
			// results to display
			if (result != null && result.size() > 0 && unittypes.getSelected() != null) {
				Map<String, String> displayableMap = unittypes.getSelected().getUnittypeParameters().getDisplayableNameMap();
				List<String> displayHeaders = new ArrayList<>();
				for (SearchResultWrapper srw : wrappedResults) {
					for (String paramname : displayableMap.keySet()) {
						UnitParameter up = srw.unit.getUnitParameters().get(paramname);
						if (up == null) {
							srw.displayables.add("");
						} else {
							srw.displayables.add(up.getValue());
						}
					}
				}
				for (String parameter : displayableMap.keySet()) {
					displayHeaders.add(displayableMap.get(parameter));
				}
				map.put("displayheaders", displayHeaders);
			}
			map.put("wrappedresults", wrappedResults);
			map.put("result", result);
			map.put("limit", getLimit(limit));
		} else if (params.getParameter("term") != null) {
			int limit = 10;
			inputData.getUnitParamValue().setValue(params.getParameter("term"));
			List<Unit> result = getSearchResults(limit, unittypes.getSelected(), profiles.getSelected(), params);
			String out = null;
			JSONArray json = new JSONArray();
			if (result != null && result.size() > 0) {
				for (Unit u : result) {
					JSONObject j = new JSONObject();
					j.put("value", u.getId());
					json.put(j);
				}
			}
			out = json.toString();
			outputHandler.setDirectResponse(out);
			return;
		} else {
			map.put("limit", getDefaultLimit());
		}
		outputHandler.setTemplatePathWithIndex("search");
	}

	public List<GroupParameter> getAllGroupParameters(Group group) {
		List<GroupParameter> groupParams_ = new ArrayList<GroupParameter>();
		groupParams_.addAll(Arrays.asList(group.getGroupParameters().getGroupParameters()));
		Group parent = group;
		while ((parent = parent.getParent()) != null) {
			groupParams_.addAll(Arrays.asList(parent.getGroupParameters().getGroupParameters()));
		}
		return groupParams_;
	}

	/**
	 * Gets the results.
	 * 
	 * @param limit
	 *            the limit
	 * @param unittype
	 *            the unittype
	 * @param profile
	 *            the profile
	 * @return the results
	 * @throws Exception
	 *             the exception
	 */
	private List<Unit> getSearchResults(int limit, Unittype unittype, Profile profile, ParameterParser req) throws Exception {
		List<Unit> results = null;

		int more = limit + 1;

		List<Profile> allowedProfiles = getAllowedProfiles(req.getSession().getId(), unittype);

		List<Parameter> searchParams = geSearchableParametersFromRequest(req, unittype);

		if (simpleMode()) {
			String unitParamValue = null;
			if (inputData.getUnitParamValue().notNullNorValue(""))
				unitParamValue = "%" + inputData.getUnitParamValue().getString() + "%";
			if (profile == null) {
				results = new ArrayList<Unit>(xapsUnit.getUnits(unitParamValue, allowedProfiles, more).values());
			} else {
				results = new ArrayList<Unit>(xapsUnit.getUnits(unitParamValue, unittype, profile, more).values());
			}
		} else if (advancedMode() && unittype != null) {
			if (profile == null) {
				results = new ArrayList<Unit>(xapsUnit.getUnits(unittype, allowedProfiles, searchParams, more).values());
			} else {
				results = new ArrayList<Unit>(xapsUnit.getUnits(unittype, profile, searchParams, more).values());
			}
		}

		return results;
	}

	private boolean advancedMode() {
		return inputData.getAdvanced().getBoolean() == true;
	}

	private boolean simpleMode() {
		return inputData.getAdvanced().getBoolean() == false;
	}

	/**
	 * Gets the default limit.
	 * 
	 * @return the default limit
	 */
	private Map<String, Object> getDefaultLimit() {
		Map<String, Object> rootMap = new HashMap<String, Object>();
		rootMap.put("key", inputData.getLimit().getKey());
		rootMap.put("value", inputData.getLimit().getInteger(100));
		return rootMap;
	}

	/**
	 * Gets the limit.
	 * 
	 * @param limit
	 *            the limit
	 * @return the limit
	 */
	private Map<String, Object> getLimit(int limit) {
		Map<String, Object> rootMap = new HashMap<String, Object>();
		rootMap.put("key", inputData.getLimit().getKey());
		rootMap.put("value", limit);
		return rootMap;
	}

	/**
	 * Gets the common name.
	 * 
	 * @param utp
	 *            the utp
	 * @param allGroupUtps
	 *            the all group utps
	 * @return the common name
	 */
	public String getCommonName(UnittypeParameter utp, Map<String, String> allGroupUtps) {
		return getCommonName(utp.getName(), allGroupUtps);
	}

	/**
	 * Gets the common name.
	 * 
	 * @param name
	 *            the name
	 * @param map
	 *            the map
	 * @return the common name
	 */
	public String getCommonName(String name, Map<String, String> map) {
		for (Entry<String, String> entry : map.entrySet()) {
			if (entry.getKey().startsWith(name))
				return entry.getValue();
		}
		return null;
	}

	/**
	 * Prepare parameters.
	 * 
	 * @param rootMap
	 *            the root map
	 * @param req
	 *            the req
	 * @param unittype
	 *            the unittype
	 */
	private void prepareSearchableParameters(Map<String, Object> rootMap, ParameterParser req, Unittype unittype) {
		Collection<UnittypeParameter> searchables = unittype.getUnittypeParameters().getSearchableMap().values();
		
		List<GroupParameter> groupParams = null;
		
		rootMap.put("group", inputData.getGroup());
		if (inputData.getGroup().getString() != null) {
			group = unittype.getGroups().getByName(inputData.getGroup().getString());
			if (group != null)
				groupParams = getAllGroupParameters(group);
		}
		
		if (groupParams != null) {
			for (GroupParameter param : groupParams) {
				if (searchables.contains(param.getParameter().getUnittypeParameter()))
					searchables.remove(param.getParameter().getUnittypeParameter());
			}

			List<SearchParameter> groupParamsList = new ArrayList<SearchParameter>();
			for (GroupParameter param : groupParams) {
				String theParameterName = param.getName();
				String theParameterValue = param.getParameter().getValue();
				String theParameterDisplayText = param.getParameter().getUnittypeParameter().getName();
				String convertedParameterName = SearchParameter.convertParameterId(theParameterName);

				Parameter parameter = param.getParameter();
				
				Boolean isEnabled = req.getBooleanParameter("enabled::"+convertedParameterName);
				if (req.getStringParameter(convertedParameterName) != null){
					if(isEnabled)
						theParameterValue = req.getStringParameter(convertedParameterName);
					else
						theParameterValue = SearchParameter.convertParameterValue(theParameterValue);
				}else{
					isEnabled = true;
					theParameterValue = SearchParameter.convertParameterValue(theParameterValue);
				}
				
				if(theParameterValue==null || theParameterValue.isEmpty())
					theParameterValue = null;

				Operator operator = parameter.getOp();
				if (req.getStringParameter("operator::" + convertedParameterName) != null)
					operator = Operator.getOperator(req.getStringParameter("operator::" + convertedParameterName));

				ParameterDataType type = parameter.getType();
				if (req.getStringParameter("datatype::" + convertedParameterName) != null)
					type = ParameterDataType.getDataType(req.getStringParameter("datatype::" + convertedParameterName));

				groupParamsList.add(new SearchParameter(theParameterName, theParameterDisplayText, theParameterValue, operator, type,isEnabled));
			}

			rootMap.put("groupparams", groupParamsList);
		}

		List<SearchParameter> searchableParamsList = new ArrayList<SearchParameter>();
		for (UnittypeParameter searchUtp : searchables) {
			String utpName = searchUtp.getName();
			Boolean isEnabled = req.getBooleanParameter("enabled::"+utpName);
			String value = null;
			if(isEnabled)
				value = req.getParameter(utpName);
			String common = searchUtp.getName();

			Operator operator = Operator.EQ;
			if (req.getStringParameter("operator::" + utpName) != null)
				operator = Operator.getOperator(req.getStringParameter("operator::" + utpName));

			ParameterDataType type = ParameterDataType.TEXT;
			if (req.getStringParameter("datatype::" + utpName) != null)
				type = ParameterDataType.getDataType(req.getStringParameter("datatype::" + utpName));

			searchableParamsList.add(new SearchParameter(utpName, common, value, operator, type,isEnabled));
		}
		
		rootMap.put("searchables", searchableParamsList);
		
		List<SearchParameter> volatileParameters = new ArrayList<SearchParameter>();
		Enumeration<?> keys = req.getHttpServletRequest().getParameterNames();
		while(keys.hasMoreElements()){
			String key = (String) keys.nextElement();
			if(key.startsWith("remember::")){
				String utpName = key.substring(10);
				Boolean isEnabled = req.getBoolean("enabled::"+key);
				String value = null;
				if(isEnabled)
					value = req.getParameter(key);
				String common = utpName;
				
				Operator operator = Operator.EQ;
				if (req.getStringParameter("operator::" + key) != null)
					operator = Operator.getOperator(req.getStringParameter("operator::" + key));

				ParameterDataType type = ParameterDataType.TEXT;
				if (req.getStringParameter("datatype::" + key) != null)
					type = ParameterDataType.getDataType(req.getStringParameter("datatype::" + key));
				
				volatileParameters.add(new SearchParameter(utpName, common, value, operator, type,isEnabled));
			}
		}
		
		rootMap.put("volatiles", volatileParameters);
	}
	
	public List<GroupParameter> getAllParameters(Group group) {
		List<GroupParameter> groupParams = new ArrayList<GroupParameter>();
		groupParams.addAll(Arrays.asList(group.getGroupParameters().getGroupParameters()));
		Group parent = group;
		while ((parent = parent.getParent()) != null) {
			groupParams.addAll(Arrays.asList(parent.getGroupParameters().getGroupParameters()));
		}
		return groupParams;
	}

	/**
	 * Gets the unit params.
	 * 
	 * @param req
	 *            the req
	 * @param unittype
	 *            the unittype
	 * @param profile
	 *            the profile
	 * @return the unit params
	 */
	private List<Parameter> geSearchableParametersFromRequest(ParameterParser req, Unittype unittype) {
		List<Parameter> unitParamsTmp = new ArrayList<Parameter>();
		if(unittype==null)
			return unitParamsTmp;
		
		if(inputData.getCmd().isValue("auto")){
			List<Parameter> allGroupParameters = group.getGroupParameters().getAllParameters(group);
			unitParamsTmp.addAll(allGroupParameters);
		}else{
			Enumeration<?> enumeration = req.getKeyEnumeration();
			while (enumeration.hasMoreElements()) {
				String name = (String) enumeration.nextElement();
				UnittypeParameter utp = unittype.getUnittypeParameters().getByName(name);
				if (utp == null && group != null && group.getGroupParameters().getByName(SearchParameter.convertParameterId(name))!=null) {
					String parameterNameConverted = SearchParameter.convertParameterId(name);
					GroupParameter gp = group.getGroupParameters().getByName(parameterNameConverted);
					Boolean isEnabled = req.getBooleanParameter("enabled::"+name);
					String value = req.getStringParameter(name);
					if (gp != null && isEnabled) {
						utp = gp.getParameter().getUnittypeParameter();
						Parameter param = gp.getParameter();
	
						if (value.equals(SearchParameter.NULL_VALUE))
							value = null;
						else if (value.equals(SearchParameter.EMPTY_VALUE))
							value = "";
						else if(value!=null && value.isEmpty()){
							value = null;
						}
	
						value = SearchParameter.convertParameterValue(value);
	
						Operator operator = param.getOp();
						if (req.getStringParameter("operator::" + name) != null)
							operator = Operator.getOperator(req.getStringParameter("operator::" + name));
	
						ParameterDataType type = param.getType();
						if (req.getStringParameter("datatype::" + name) != null)
							type = ParameterDataType.getDataType(req.getStringParameter("datatype::" + name));
	
						Parameter clonedParameter = new Parameter(param.getUnittypeParameter(), value, operator, type);
	
						unitParamsTmp.add(clonedParameter);
					}
				} else if(utp == null && name.startsWith("remember::")){
					String convertedName = name.substring(10);
					utp = unittype.getUnittypeParameters().getByName(convertedName);
					if(utp!=null){
						String value = req.getParameter(name);
						Boolean isEnabled = req.getBooleanParameter("enabled::"+name);
						if (isEnabled) {
							if (value.equals(SearchParameter.NULL_VALUE))
								value = null;
							else if(value.equals(SearchParameter.EMPTY_VALUE))
								value = "";
							
							value = SearchParameter.convertParameterValue(value);
							
							Operator operator = Operator.EQ;
							if (req.getStringParameter("operator::" + name) != null)
								operator = Operator.getOperator(req.getStringParameter("operator::" + name));
	
							ParameterDataType type = ParameterDataType.TEXT;
							if (req.getStringParameter("datatype::" + name) != null)
								type = ParameterDataType.getDataType(req.getStringParameter("datatype::" + name));
	
							Parameter param = new Parameter(utp, value, operator, type);
	
							unitParamsTmp.add(param);
						}
					}
				} else if (utp != null) {
					String value = req.getParameter(name);
					Boolean isEnabled = req.getBooleanParameter("enabled::"+name);
					if(isEnabled){
						if (value.equals(SearchParameter.NULL_VALUE))
							value = null;
						else if(value.equals(SearchParameter.EMPTY_VALUE))
							value = "";
						
						value = SearchParameter.convertParameterValue(value);
	
						Operator operator = Operator.EQ;
						if (req.getStringParameter("operator::" + name) != null)
							operator = Operator.getOperator(req.getStringParameter("operator::" + name));
	
						ParameterDataType type = ParameterDataType.TEXT;
						if (req.getStringParameter("datatype::" + name) != null)
							type = ParameterDataType.getDataType(req.getStringParameter("datatype::" + name));
	
						Parameter param = new Parameter(utp, value, operator, type);
	
						unitParamsTmp.add(param);
					}
				}
			}
		}
		return unitParamsTmp;
	}
}