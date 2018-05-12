package com.github.freeacs.tr069.methods;

import com.github.freeacs.tr069.xml.Body;

public class GPNreq extends Body {

  private static final String START = "\t\t<cwmp:GetParameterNames>\n";
  private static final String PARAMETER_PATH_START = "\t\t\t<ParameterPath>";
  private static final String PARAMETER_PATH_END = "</ParameterPath>\n";
  private static final String NEXTLEVEL_F = "\t\t\t<NextLevel>false</NextLevel>\n";
  private static final String NEXTLEVEL_0 = "\t\t\t<NextLevel>0</NextLevel>\n";
  // private static final String NEXTLEVEL =
  // "\t\t\t<NextLevel>true</NextLevel>\n";
  private static final String END = "\t\t</cwmp:GetParameterNames>\n";

  private String parameter;
  private boolean nextLevel0;

  public GPNreq(String parameter, boolean nextLevel0) {
    this.parameter = parameter;
    this.nextLevel0 = nextLevel0;
  }

  @Override
  public String toXmlImpl() {
    StringBuilder sb = new StringBuilder(3);
    sb.append(START);
    sb.append(PARAMETER_PATH_START);
    sb.append(parameter);
    sb.append(PARAMETER_PATH_END);
    if (nextLevel0)
      sb.append(NEXTLEVEL_0);
    else
      sb.append(NEXTLEVEL_F);
    sb.append(END);
    return sb.toString();
  }

}
