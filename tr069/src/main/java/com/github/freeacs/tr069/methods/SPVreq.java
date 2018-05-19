package com.github.freeacs.tr069.methods;

import com.github.freeacs.tr069.Namespace;
import com.github.freeacs.tr069.xml.Body;
import com.github.freeacs.tr069.xml.ParameterValueStruct;

import java.util.List;

public class SPVreq extends Body {

	private static final String START = "\t\t<cwmp:SetParameterValues>\n";
	private static final String END = "\t\t</cwmp:SetParameterValues>\n";
	private static final String PARAMETER_LIST_START_1 = "\t\t\t<ParameterList " + Namespace.getSoapEncNS() + ":arrayType=\"cwmp:ParameterValueStruct[";
	private static final String PARAMETER_LIST_START_2 = "]\">\n";
	private static final String PARAMETER_KEY_START = "\t\t\t<ParameterKey>";
	private static final String PARAMETER_KEY_END = "</ParameterKey>\n";
	private static final String PARAMETER_VALUE_STRUCT_START = "\t\t\t\t<ParameterValueStruct>\n";
	private static final String NAME_START = "\t\t\t\t\t<Name>";
	private static final String NAME_END = "</Name>\n";
	private static final String PARAMETER_VALUE_STRUCT_END = "\t\t\t\t</ParameterValueStruct>\n";
	private static final String PARAMETER_LIST_END = "\t\t\t</ParameterList>\n";

	private List<ParameterValueStruct> parameterValueList;
	private String parameterKey;

	public SPVreq(List<ParameterValueStruct> parameterValueList, String parameterKey) {
		this.parameterValueList = parameterValueList;
		this.parameterKey = parameterKey;
	}

	@Override
	public String toXmlImpl() {
		StringBuilder sb = new StringBuilder(50);
		sb.append(START);
		sb.append(PARAMETER_LIST_START_1);
		sb.append(parameterValueList.size());
		sb.append(PARAMETER_LIST_START_2);

		for (ParameterValueStruct pvs : parameterValueList) {
			sb.append(PARAMETER_VALUE_STRUCT_START);
			sb.append(NAME_START);
			sb.append(pvs.getName());
			sb.append(NAME_END);
			sb.append("\t\t\t\t\t<Value xsi:type=\"" + pvs.getType() + "\">");
			if (pvs.getType() != null && pvs.getType().contains("int") && (pvs.getValue() == null || pvs.getValue().trim().equals("")))
				sb.append("0");
			else
				sb.append(pvs.getValue());
			sb.append("</Value>\n");
			sb.append(PARAMETER_VALUE_STRUCT_END);

		}
		sb.append(PARAMETER_LIST_END);
		if (parameterKey != null)
			sb.append(PARAMETER_KEY_START + parameterKey + PARAMETER_KEY_END);
		sb.append(END);
		return sb.toString();
	}
}
