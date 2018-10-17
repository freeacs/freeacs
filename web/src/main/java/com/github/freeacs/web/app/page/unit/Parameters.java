package com.github.freeacs.web.app.page.unit;

import com.github.freeacs.dbi.Unit;
import com.github.freeacs.web.app.util.WebConstants;

/**
 * Performs parameter related tasks such as retrieving and storing parameter values.
 *
 * @author Jarl André Hübebthal
 */
public class Parameters {
  /**
   * Lazy parameter retriever.
   *
   * <p>Get unit parameter value by using the full parameter name, or by excluding the keyroot.
   *
   * @param unit The unit to retreive from
   * @param key The key, either with or without keyroot. The function will automatically try all
   *     known keyroots if the provided key is not valid (returns no data).
   * @return A string representing the value, could be anything from number to password.
   */
  public static String getUnitParameterValue(Unit unit, String key) {
    String value = unit.getParameters().get(key);
    if (value == null) {
      value = unit.getParameters().get(WebConstants.KEYROOT_INTERNET_GATEWAY_DEVICE + key);
      if (value == null) {
        value = unit.getParameters().get(WebConstants.KEYROOT_DEVICE + key);
      }
      if (value == null) {
        value = unit.getParameters().get(WebConstants.KEYROOT_SYSTEM + key);
      }
    }
    return value;
  }
}
