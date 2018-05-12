package com.github.freeacs.web.app.page.staging;

import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.Parameter;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.web.app.util.XAPSLoader;
import com.owera.xaps.web.app.util.XAPSLoader;
import freemarker.template.SimpleNumber;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.util.List;


/**
 * The Class GetGroupSizeMethod.
 */
public class GetGroupSizeMethod implements TemplateMethodModel {
	
	/** The unittype. */
	private Unittype unittype;
	
	/** The session id. */
	private String sessionId;
	
	/**
	 * Instantiates a new gets the group size method.
	 *
	 * @param unittype the unittype
	 * @param sessionId the session id
	 */
	public GetGroupSizeMethod(Unittype unittype,String sessionId){
		this.unittype = unittype;
		this.sessionId = sessionId;
	}
	
	/* (non-Javadoc)
	 * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
	 */
	@SuppressWarnings("rawtypes")
	public TemplateModel exec(List args) throws TemplateModelException {
		if (args.size() != 1)
			throw new TemplateModelException("Wrong arguments");
		Integer group_id = Integer.valueOf((String) args.get(0));
		Group group = unittype.getGroups().getById(group_id);
		List<Parameter> gParams = group.getGroupParameters().getAllParameters(group);
		Integer count = null;
		try {
			count = XAPSLoader.getXAPSUnit(sessionId).getUnitCount(unittype, group.getProfile(), gParams);
		} catch (Exception e) {
			throw new TemplateModelException(e);
		}
		return new SimpleNumber(count);
	}
}