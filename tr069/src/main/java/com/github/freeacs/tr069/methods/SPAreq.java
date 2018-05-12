package com.github.freeacs.tr069.methods;

import com.github.freeacs.tr069.Namespace;
import com.github.freeacs.tr069.xml.Body;
import com.github.freeacs.tr069.xml.ParameterAttributeStruct;

import java.util.List;

public class SPAreq extends Body {

	private static final String START = "\t\t<cwmp:SetParameterAttributes>\n";
	private static final String END = "\t\t</cwmp:SetParameterAttributes>\n";
	private static final String PARAMETER_LIST_START_1 = "\t\t\t<ParameterList " + Namespace.getSoapEncNS() + ":arrayType=\"cwmp:SetParameterAttributesStruct[";
	private static final String PARAMETER_LIST_START_2 = "]\">\n";
	private static final String PARAMETER_VALUE_STRUCT_START = "\t\t\t\t<SetParameterAttributesStruct>\n";
	private static final String NAME_START = "\t\t\t\t\t<Name>";
	private static final String NAME_END = "</Name>\n";
	private static final String NOTIFICATION_START = "\t\t\t\t\t<Notification>";
	private static final String NOTIFICATION_END = "</Notification>\n";
	private static final String NOTIFICATION_CHANGE_START = "\t\t\t\t\t<NotificationChange>";
	private static final String NOTIFICATION_CHANGE_END = "</NotificationChange>\n";
	private static final String PARAMETER_VALUE_STRUCT_END = "\t\t\t\t</SetParameterAttributesStruct>\n";
	private static final String PARAMETER_LIST_END = "\t\t\t</ParameterList>\n";

	private List<ParameterAttributeStruct> parameterAttributeList;

	public SPAreq(List<ParameterAttributeStruct> parameterAttributeList) {
		this.parameterAttributeList = parameterAttributeList;
	}

	@Override
	public String toXmlImpl() {
		StringBuilder sb = new StringBuilder(50);
		sb.append(START);
		sb.append(PARAMETER_LIST_START_1);
		sb.append(parameterAttributeList.size());
		sb.append(PARAMETER_LIST_START_2);

		for (ParameterAttributeStruct pvs : parameterAttributeList) {
			sb.append(PARAMETER_VALUE_STRUCT_START);
			sb.append(NAME_START);
			sb.append(pvs.getName());
			sb.append(NAME_END);
			sb.append(NOTIFICATION_START);
			sb.append(pvs.getNotifcation());
			sb.append(NOTIFICATION_END);
			sb.append(NOTIFICATION_CHANGE_START);
			sb.append("1");
			sb.append(NOTIFICATION_CHANGE_END);
			sb.append(PARAMETER_VALUE_STRUCT_END);

		}
		sb.append(PARAMETER_LIST_END);
		sb.append(END);
		return sb.toString();
	}
}
