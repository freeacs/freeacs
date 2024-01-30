package com.github.freeacs.tr069.xml;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ParameterListTest {

  @Test
  public void testItReturnsParameterByName()
  {
    ParameterList params = new ParameterList();
    params.addParameterValueStruct(
            new ParameterValueStruct("X_TEST.Device.Name","SimpleDevice")
    );

    Assertions.assertEquals(params.getParameterValueByKey("X_TEST.Device.Name"),"SimpleDevice");
  }

  @Test
  public void testItReturnsEmptyStringWhenParameterNotFound()
  {
    ParameterList params = new ParameterList();

    Assertions.assertEquals(params.getParameterValueByKey("X_TEST.Device.Blah"),"");
  }
}
