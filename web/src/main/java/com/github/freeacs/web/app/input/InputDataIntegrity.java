package com.github.freeacs.web.app.input;

import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.context.ContextItem;
import com.github.freeacs.web.app.util.SessionCache;
import com.github.freeacs.web.app.util.SessionData;
import java.sql.SQLException;

/**
 * This class ensures that:
 *
 * <ol>
 *   <li>If Input has an incoming value from request, then the SessionData object is updated with
 *       this value.
 *   <li>If Input has no incoming value, the Input's value is set to the value of that from the
 *       SessionData object.
 * </ol>
 *
 * In earlier times this class also checked the session data integrity, eg if the unittype, profile
 * and unit was related and valid. This has been scrapped to avoid high loading time.
 *
 * @author Jarl Andre Hubenthal
 */
public class InputDataIntegrity {
  /**
   * Gets the input by name.
   *
   * @param inputs the inputs
   * @param n the n
   * @return the input by name
   */
  private static Input getInputByName(Input[] inputs, String n) {
    for (Input i : inputs) {
      if (i != null && i.getKey().equals(n)) {
        return i;
      }
    }
    return null;
  }

  /**
   * In addition to calling the same method with a few different parameters, this method also adds a
   * context item to the responsehandler, also called a trail point.
   *
   * @param params The parameter parser
   * @param response The response handler
   * @param inputData The InputData instance
   * @param inputs varargs of Input
   * @throws Exception Throws any exception.
   */
  public static void loadAndStoreSession(
      ParameterParser params, Output response, InputData inputData, Input... inputs)
      throws Exception {
    rememberAndCheck(params.getSession().getId(), inputs);
    response.addNewTrailPoint(new ContextItem(inputData, params));
  }

  /**
   * Takes a sessionId and a variable number of Input's, and keeps the Input's value in sync with
   * the SessionData object.
   *
   * @param sessionId the session id
   * @param inputs the inputs the no available connection exception
   * @throws SQLException the sQL exception
   */
  public static void rememberAndCheck(String sessionId, Input... inputs) throws SQLException {
    SessionData sessionData = SessionCache.getSessionData(sessionId);

    Input unittypeInput = getInputByName(inputs, "unittype");
    boolean unittypeChanged = false;
    if (unittypeInput != null && unittypeInput.getValue() == null) {
      if (sessionData.getUnittypeName() != null) {
        unittypeInput.setValue(sessionData.getUnittypeName());
      }
    } else if (unittypeInput != null) {
      if (unittypeInput.getValue() != null
          && !unittypeInput.getValue().equals(sessionData.getUnittypeName())) {
        unittypeChanged = true;
      }
      sessionData.setUnittypeName((String) unittypeInput.getValue());
    }

    Input profileInput = getInputByName(inputs, "profile");
    if (profileInput != null && profileInput.getValue() == null) {
      if (!unittypeChanged
          && sessionData.getUnittypeName() != null
          && sessionData.getProfileName() != null) {
        profileInput.setValue(sessionData.getProfileName());
      }
    } else if (profileInput != null) {
      sessionData.setProfileName((String) profileInput.getValue());
    }

    Input unitInput = getInputByName(inputs, "unit");
    if (unitInput != null && unitInput.getValue() == null) {
      if (sessionData.getUnitId() != null
          && sessionData.getProfileName() != null
          && sessionData.getUnittypeName() != null) {
        unitInput.setValue(sessionData.getUnitId());
      }
    } else if (unitInput != null) {
      sessionData.setUnitId(unitInput.getString());
    }

    Input jobInput = getInputByName(inputs, "job");
    if (jobInput != null && jobInput.getValue() == null) {
      if (sessionData.getJobname() != null) {
        jobInput.setValue(sessionData.getJobname());
      }
    } else if (jobInput != null) {
      sessionData.setJobname((String) jobInput.getValue());
    }

    Input flagInput = getInputByName(inputs, "filterflag");
    if (flagInput != null && flagInput.getValue() == null) {
      if (sessionData.getFilterFlag() != null) {
        flagInput.setValue(sessionData.getFilterFlag());
      }
    } else if (flagInput != null) {
      sessionData.setFilterFlag((String) flagInput.getValue());
    }

    Input typeInput = getInputByName(inputs, "filtertype");
    if (typeInput != null && typeInput.getValue() == null) {
      if (sessionData.getFilterType() != null) {
        typeInput.setValue(sessionData.getFilterType());
      }
    } else if (typeInput != null) {
      sessionData.setFilterType((String) typeInput.getValue());
    }

    Input stringInput = getInputByName(inputs, "filterstring");
    if (stringInput != null && stringInput.getValue() == null) {
      if (sessionData.getFilterString() != null) {
        stringInput.setValue(sessionData.getFilterString());
      }
    } else if (stringInput != null) {
      sessionData.setFilterString((String) stringInput.getValue());
    }

    Input groupInput = getInputByName(inputs, "group");
    if (groupInput != null && groupInput.getValue() == null) {
      if (sessionData.getGroup() != null) {
        groupInput.setValue(sessionData.getGroup());
      }
    } else if (groupInput != null) {
      sessionData.setGroup((String) groupInput.getValue());
    }
  }
}
