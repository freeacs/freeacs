package com.github.freeacs.web.app.table;

import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.GroupParameter;
import com.github.freeacs.dbi.Parameter;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.web.app.page.search.SearchParameter;

/**
 * Represents a clone for the supplied GroupParameter.
 *
 * <p>Should not be used as a replacement for the original object.
 *
 * <p>Primarily only used in the parameter tables.
 *
 * @author Jarl Andre Hubenthal
 */
public class TableGroupParameter extends GroupParameter {
  private Integer groupId;
  private String groupName;

  public TableGroupParameter(GroupParameter parameter) {
    this(parameter.getId(), parameter.getParameter(), parameter.getGroup());
  }

  private TableGroupParameter(Integer groupId, Parameter parameter, Group group) {
    super(parameter, group);
    setParameter(new TableParameter(getParameter())); // CLONE! :)
    this.groupId = groupId; // Remember group id
  }

  @Override
  public Integer getId() {
    return groupId;
  }

  @Override
  public String getName() {
    if (groupName == null) {
      groupName =
          SearchParameter.convertParameterId(super.getName()).replace("null", groupId.toString());
    }
    return groupName;
  }

  private class TableParameter extends Parameter {
    public TableParameter(Parameter parameter) {
      this(
          parameter.getUnittypeParameter(),
          parameter.getValue(),
          parameter.getOp(),
          parameter.getType());
      setValueWasNull(parameter.valueWasNull());
    }

    private TableParameter(UnittypeParameter utp, String val, Operator op, ParameterDataType type) {
      super(utp, val, op, type);
    }

    @Override
    public String getValue() {
      return SearchParameter.convertParameterValue(super.getValue());
    }
  }
}
