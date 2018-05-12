package com.owera.xaps.tr069.methods;

import com.owera.xaps.base.Log;
import com.owera.xaps.dbi.util.ProvisioningMode;
import com.owera.xaps.tr069.HTTPReqResData;
import com.owera.xaps.tr069.HTTPResData;
import com.owera.xaps.tr069.exception.TR069Exception;
import com.owera.xaps.tr069.exception.TR069ExceptionShortMessage;
import com.owera.xaps.tr069.test.system2.TestUnit;
import com.owera.xaps.tr069.test.system2.TestUnit.TestState;
import com.owera.xaps.tr069.test.system2.TestUnitCache;
import com.owera.xaps.tr069.test.system2.Util;

import java.lang.reflect.Method;

/* This class is responsible for choosing the next response in the
 * TR-069 conversation. Depending upon the request, different logic
 * applies. 
 */
public class DecisionMaker {

	public static void process(HTTPReqResData reqRes) throws TR069Exception {
		HTTPResData resData = reqRes.getResponse();
		if (reqRes.getThrowable() != null) {
			resData.setMethod(TR069Method.EMPTY);
			return;
		}
		String reqMethod = null;
		HTTPRequestAction reqAction = null;
		Method decisionMakerMethod = null;
		try {
			if (reqRes.getSessionData().isTestMode() && Util.testEnabled(reqRes, false)) {
				// TODO:TF - find next method - completed
				TestUnit tu = TestUnitCache.get(reqRes.getSessionData().getUnitId());
				if (tu == null) {
					Log.error(DecisionMaker.class, "Test aborted, since testUnit object was not defined");
				} else {
					tu.next(); // Responsible for updating TestState
					if (tu.getTestState() == TestState.ENDTEST) {
						Log.notice(EMDecision.class, "A Test session has been completed - will return to " + ProvisioningMode.REGULAR + " provisioning");
						Log.info(DecisionMaker.class, "Decision is " + TR069Method.EMPTY);
						TestUnitCache.remove(tu.getUnit().getId());
						Util.testDisable(reqRes);
						reqRes.getResponse().setMethod(TR069Method.EMPTY);
					} else {
						String tr069Method = Util.step2TR069Method(tu.getCurrentStep(), Util.getTestCaseMethod(tu.getUnit()));
						Log.info(EMDecision.class, "Decision is " + tr069Method + " since ACS is in test-mode (new type) and test-state is " + tu.getTestState() + " and case-step is "
								+ tu.getCurrentCase().getId() + "," + tu.getCurrentStep());
						reqRes.getResponse().setMethod(tr069Method);
					}
				}
			} else {
				reqMethod = reqRes.getRequest().getMethod();
				reqAction = TR069Method.requestMap.get(reqMethod);
				decisionMakerMethod = reqAction.getDecisionMakerMethod();
				if (decisionMakerMethod != null)
					decisionMakerMethod.invoke(null, reqRes);
				else
					resData.setMethod(reqAction.getNextMethod());
			}
		} catch (Throwable t) {
			int loopCount = 0;
			while (t.getCause() != null) {
				t = t.getCause();
				if (++loopCount > 10)
					break;
			}
			throw new TR069Exception("An error occurred in DecisionMaker: " +t.getMessage(), TR069ExceptionShortMessage.MISC, t);
		}
	}
}
