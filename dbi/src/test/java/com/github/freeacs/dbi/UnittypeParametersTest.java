package com.github.freeacs.dbi;

import static org.junit.Assert.*;

import com.github.freeacs.dbi.util.MapWrapper;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class UnittypeParametersTest extends BaseDBITest {

  @Test
  public void addOrChangeUnittypeParameters() throws SQLException {
    Unittype unittype = TestUtils.createUnittype("Test Unit Type", acs);
    UnittypeParameterFlag flag = new UnittypeParameterFlag("R");

    Map<Integer, UnittypeParameter> idMap = new HashMap<>();
    MapWrapper<UnittypeParameter> mw = new MapWrapper<UnittypeParameter>(ACS.isStrictOrder());
    Map<String, UnittypeParameter> nameMap = mw.getMap();

    UnittypeParameters unittypeParameters = new UnittypeParameters(idMap, nameMap, unittype);

    unittypeParameters.addOrChangeUnittypeParameters(
            Arrays.asList(
                    new UnittypeParameter(unittype,"TEST.Parameter1", flag),
                    new UnittypeParameter(unittype,"TEST.Parameter2", flag)
            ),
            acs
    );

    assertNotNull(unittypeParameters.getByName("TEST.Parameter1"));
    assertNotNull(unittypeParameters.getByName("TEST.Parameter2"));
  }

  @Test
  public void addOrChangeUnittypeParameter() throws SQLException {
    Unittype unittype = TestUtils.createUnittype("Test Unit Type", acs);
    UnittypeParameterFlag flag = new UnittypeParameterFlag("R");
    UnittypeParameter unittypeParameter = new UnittypeParameter(unittype,
            "TEST.Parameter.Subparameter",flag);

    Map<Integer, UnittypeParameter> idMap = new HashMap<>();
    MapWrapper<UnittypeParameter> mw = new MapWrapper<UnittypeParameter>(ACS.isStrictOrder());
    Map<String, UnittypeParameter> nameMap = mw.getMap();

    UnittypeParameters unittypeParameters = new UnittypeParameters(idMap, nameMap, unittype);

    unittypeParameters.addOrChangeUnittypeParameter(unittypeParameter, acs);

    UnittypeParameter createdParam = unittypeParameters.getByName("TEST.Parameter.Subparameter");

    assertEquals(createdParam.getName(), unittypeParameter.getName());
    assertEquals(createdParam.getFlag().toString(), unittypeParameter.getFlag().toString());

  }

  @Test
  public void deleteUnittypeParameter() {}

  @Test
  public void deleteUnittypeParameters() {}
}
