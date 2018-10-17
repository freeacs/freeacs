package com.github.freeacs.web.app.page.unittype;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.util.ACSLoader;
import javax.sql.DataSource;

/**
 * Used by the javascript callback function that executes after closing the {@link
 * UnittypeParametersPage} dialog. If the {@link UnittypeParametersPage} dialog has changed a
 * parameters flag, the callback function will "get the value" of the flags variable on the
 * parameter.
 *
 * @author Jarl Andre Hubenthal
 */
public class GetUnitTypeParameterFlagAndValuesPage extends AbstractWebPage {
  protected class FlagsHolder {
    public Boolean hasValues;
    public String flag;

    public FlagsHolder(Boolean hasValues, String flag) {
      this.hasValues = hasValues;
      this.flag = flag;
    }
  }

  public void process(
      ParameterParser params, Output res, DataSource xapsDataSource, DataSource syslogDataSource)
      throws Exception {
    String type = params.getParameter("type");
    String name = params.getParameter("name");
    String unittype = params.getParameter("unittype");
    res.setContentType("text/html");
    if (type != null && name != null) {
      if ("unittype".equals(type) && unittype != null) {
        ACS acs = ACSLoader.getXAPS(params.getSession().getId(), xapsDataSource, syslogDataSource);
        Unittype ut = acs.getUnittype(unittype);
        if (ut != null) {
          UnittypeParameter utp = ut.getUnittypeParameters().getByName(name);
          String flag = utp.getFlag().getFlag();
          boolean hasValues = utp.getValues() != null && !utp.getValues().getValues().isEmpty();
          if (flag != null) {
            FlagsHolder toReturn = new FlagsHolder(hasValues, flag);
            res.setDirectResponse(OBJECT_MAPPER.writeValueAsString(toReturn));
          }
        } else {
          res.setDirectResponse("err: flag was not found");
        }
      } else {
        res.setDirectResponse("err: unittype is null or type is not recognized");
      }
    } else {
      res.setDirectResponse("err: specify name and type");
    }
  }
}
