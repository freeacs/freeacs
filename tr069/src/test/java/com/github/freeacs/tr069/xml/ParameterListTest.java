package com.github.freeacs.tr069.xml;

import org.junit.Assert;
import org.junit.Test;

public class ParameterListTest {

  @Test
  public void testItReturnsParameterByName()
  {
    ParameterList params = new ParameterList();
    params.addParameterValueStruct(
            new ParameterValueStruct("X_TEST.Device.Name","SimpleDevice")
    );

    Assert.assertEquals(params.getParameterValueByKey("X_TEST.Device.Name"),"SimpleDevice");
  }

  @Test
  public void testItReturnsEmptyStringWhenParameterNotFound()
  {
    ParameterList params = new ParameterList();

    Assert.assertEquals(params.getParameterValueByKey("X_TEST.Device.Blah"),"");
  }
}
