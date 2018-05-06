package com.owera.xaps.web.app.table;

import com.owera.xaps.dbi.Group;
import com.owera.xaps.dbi.GroupParameter;
import com.owera.xaps.dbi.Parameter;
import com.owera.xaps.dbi.UnittypeParameter;
import com.owera.xaps.web.app.page.search.SearchParameter;

/**
 * Represents a clone for the supplied GroupParameter.
 * 
 * Should not be used as a replacement for the original object.
 * 
 * Primarily only used in the parameter tables.
 * 
 * @author Jarl Andre Hubenthal
 */
public class TableGroupParameter extends GroupParameter {
	private Integer groupId;
	private String groupName;
	
	public TableGroupParameter(GroupParameter parameter){
		this(parameter.getId(),parameter.getParameter(),parameter.getGroup());
	}

	private TableGroupParameter(Integer groupId, Parameter parameter, Group group) {
		super(parameter, group);
		this.setParameter(new TableParameter(getParameter())); // CLONE! :)
		this.groupId = groupId; // Remember group id
	}
	
	@Override
	public Integer getId(){
		return groupId;
	}
	
	@Override
	public String getName(){
		if(groupName==null)
			groupName = SearchParameter.convertParameterId(super.getName()).replace("null", groupId.toString());
		return groupName;
	}
	
	private class TableParameter extends Parameter {
		public TableParameter(Parameter parameter){
			this(parameter.getUnittypeParameter(),parameter.getValue(),parameter.getOp(),parameter.getType());
			this.setValueWasNull(parameter.valueWasNull());
		}

		private TableParameter(UnittypeParameter utp, String val, Operator op, ParameterDataType type) {
			super(utp, val, op, type);
		}

		@Override
		public String getValue(){
			return SearchParameter.convertParameterValue(super.getValue());
		}
	}
}