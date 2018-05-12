package com.github.freeacs.tr069.test.system2;

import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.UnitParameter;
import com.github.freeacs.dbi.tr069.Steps.Step;
import com.github.freeacs.dbi.tr069.TestCase.TestCaseMethod;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.tr069.HTTPReqResData;
import com.github.freeacs.tr069.background.ScheduledKickTask;
import com.github.freeacs.tr069.methods.TR069Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {

  private static Logger logger = LoggerFactory.getLogger(Util.class);

  /**
   * Checks whether a test of the "new test framework" is initialized for the
   * unit in this session This method will never throw an exception, all objects
   * will be checked before used. Defaults to false. If test-enable is
   * explicitly set to 0 or false, remove TestUnit from TestUnitCache
   * 
   * @param reqRes
   * @return
   */
  public static boolean testEnabled(HTTPReqResData reqRes, boolean initiateKick) {
    if (reqRes == null)
      return false;
    if (reqRes.getSessionData() == null)
      return false;
    if (!initiateKick && (reqRes.getSessionData().isFactoryReset() || reqRes.getSessionData().isValueChange()))
      return false; // Must allow one normal provisioning to establish
                    // UDP-connection-request address (to allow kick)
    if (reqRes.getSessionData().getUnit() == null)
      return false;
    Unit unit = reqRes.getSessionData().getUnit();
    if (unit.getUnitParameters() == null)
      return false;
    UnitParameter testEnabled = unit.getUnitParameters().get(SystemParameters.TEST_ENABLE);
    if (testEnabled != null && (testEnabled.getValue().equals("1") || testEnabled.getValue().equalsIgnoreCase("true"))) {
      return true;
    } else if (testEnabled != null && (testEnabled.getValue().equals("0") || testEnabled.getValue().equalsIgnoreCase("false"))) {
      TestUnitCache.put(unit.getId(), null); // Remove test from cache as soon
                                             // as we detect a change in the
                                             // enable setting
      return false;
    } else
      return false;
  }

  public static void testDisable(HTTPReqResData reqRes) {
    if (reqRes != null && reqRes.getSessionData() != null && reqRes.getSessionData().getUnit() != null) {
      Unit unit = reqRes.getSessionData().getUnit();
      unit.toWriteQueue(SystemParameters.TEST_ENABLE, "0");
      ScheduledKickTask.removeUnit(unit.getId());
    }
  }

  public static String step2TR069Method(Step step, TestCaseMethod method) {
    if (step == Step.RESET)
      return TR069Method.FACTORY_RESET;
    if (step == Step.REBOOT)
      return TR069Method.REBOOT;
    if (step == Step.EXECUTE)
      return TR069Method.CUSTOM;
    if (step == Step.GET && method == TestCaseMethod.VALUE)
      return TR069Method.GET_PARAMETER_VALUES;
    if (step == Step.SET && method == TestCaseMethod.VALUE)
      return TR069Method.SET_PARAMETER_VALUES;
    if (step == Step.GET && method == TestCaseMethod.ATTRIBUTE)
      return TR069Method.GET_PARAMETER_ATTRIBUTES;
    if (step == Step.SET && method == TestCaseMethod.ATTRIBUTE)
      return TR069Method.SET_PARAMETER_ATTRIBUTES;
    if (step == Step.EMPTY)
      return TR069Method.EMPTY;
    return TR069Method.EMPTY;
  }

  public static TestCaseMethod getTestCaseMethod(Unit unit) {
    UnitParameter methodUp = unit.getUnitParameters().get(SystemParameters.TEST_METHOD);
    TestCaseMethod method = TestCaseMethod.VALUE;
    if (methodUp != null && methodUp.getValue().equalsIgnoreCase(TestCaseMethod.ATTRIBUTE.toString()))
      return TestCaseMethod.ATTRIBUTE;
    if (methodUp != null && methodUp.getValue().equalsIgnoreCase(TestCaseMethod.FILE.toString()))
      return TestCaseMethod.FILE;
    if (methodUp == null || !methodUp.getValue().equalsIgnoreCase(TestCaseMethod.VALUE.toString()))
      logger.warn("The unit parameter " + SystemParameters.TEST_METHOD + " is set to a non-valid value (" + (methodUp == null ? "NULL" : methodUp.getValue())
          + ") - defaults to VALUE (= run GetParameterValue and SetParameterValue)");
    return method;
  }

  public static String getTagFilter(Unit unit) {
    UnitParameter tagFilterUp = unit.getUnitParameters().get(SystemParameters.TEST_TAG_FILTER);
    String tagFilter = null;
    if (tagFilterUp != null && tagFilterUp.getValue() != null && !tagFilterUp.getValue().trim().equals("") && !tagFilterUp.getValue().trim().equals("NULL"))
      tagFilter = tagFilterUp.getValue();
    return tagFilter;
  }

  public static String getParamFilter(Unit unit) {
    UnitParameter paramFilterUp = unit.getUnitParameters().get(SystemParameters.TEST_PARAM_FILTER);
    String paramFilter = null;
    if (paramFilterUp != null && paramFilterUp.getValue() != null && !paramFilterUp.getValue().trim().equals("") && !paramFilterUp.getValue().trim().equals("NULL"))
      paramFilter = paramFilterUp.getValue();
    return paramFilter;
  }

}
