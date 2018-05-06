package com.owera.xaps.shell.menu;

import java.util.ArrayList;
import java.util.List;

import com.owera.xaps.dbi.Parameter;
import com.owera.xaps.dbi.Parameter.ParameterDataType;
import com.owera.xaps.dbi.UnittypeParameter;
import com.owera.xaps.dbi.Parameter.Operator;
import com.owera.xaps.shell.Context;

public class ParameterParser {
	public static List<Parameter> parse(Context context, String[] args) {
		List<Parameter> params = new ArrayList<Parameter>();
		int i = 1; // Parameter-parsing always start from index 1, index 0 is the "setparam" command
		while (true) {
			try {
				String[] utpArgArr = args[i].split("#");
				UnittypeParameter utp = context.getUnittype().getUnittypeParameters().getByName(utpArgArr[0]);
				if (utp == null)
					throw new IllegalArgumentException("The unittype parameter " + args[i] + " does not exist.");
				i++;
				Operator op = Operator.getOperatorFromLiteral(args[i]);
				i++;
				String value = args[i];
				if (value.equals("NULL"))
					value = null;
				i++;
				ParameterDataType type = null;
				Parameter p = null;
				try {
					type = ParameterDataType.getDataType(args[i]);
					p = new Parameter(utp, value, op, type);
					i++;
				} catch (IllegalArgumentException iae) {
					p = new Parameter(utp, value, op, ParameterDataType.TEXT);
				} catch (ArrayIndexOutOfBoundsException aioobeInner) {
					p = new Parameter(utp, value, op, ParameterDataType.TEXT);
				}
				if (utpArgArr.length == 2 && !utpArgArr[1].equals("null"))
					p.setGroupParameterId(new Integer(utpArgArr[1]));
				params.add(p);
			} catch (ArrayIndexOutOfBoundsException aioobe) {
				break;
			}
		}
		return params;
	}

}
