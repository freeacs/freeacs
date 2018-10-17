package com.github.freeacs.web.app.input;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A static helper class for parsing the incoming request parameters and translating them to the
 * correct type.
 *
 * @author Jarl Amdré Hübenthal
 */
public class InputDataRetriever {
  /**
   * This method iterates on the methods of the InputData instance reflectively and tries to
   * retrieve the request parameter value for each Input those methods return.
   *
   * @param inputData Any instance that extends InputData
   * @param params The parameter parser
   * @return The same InputData instance
   * @throws IllegalArgumentException the illegal argument exception
   * @throws IllegalAccessException the illegal access exception
   * @throws InvocationTargetException the invocation target exception
   */
  public static InputData parseInto(InputData inputData, ParameterParser params)
      throws IllegalAccessException, InvocationTargetException {
    Method[] methods = inputData.getClass().getMethods();
    for (Method m : methods) {
      if (!Modifier.isStatic(m.getModifiers())
          && m.getReturnType() == Input.class
          && Modifier.isPublic(m.getModifiers())) {
        Input input = (Input) m.invoke(inputData, (Object[]) null);
        setInputValue(input, params);
      }
    }
    return inputData;
  }

  /**
   * Returns a request parameter value only if its trimmed length is longer than zero.
   *
   * @param params The parameter parser
   * @param key The request parameter key
   * @return The request parameter value
   */
  private static String getNonEmptyStringParameter(ParameterParser params, String key) {
    try {
      String s = params.getStringParameter(key);
      if (s != null && s.trim().isEmpty()) {
        return null;
      }
      return s;
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Sets the input value.
   *
   * @param in the in
   * @param params the params
   */
  private static void setInputValue(Input in, ParameterParser params) {
    try {
      switch (in.getType()) {
        case STRING:
          if (in.isArray()) {
            in.setValue(params.getStringParameterArray(in.getKey()));
          } else {
            in.setValue(getNonEmptyStringParameter(params, in.getKey()));
          }
          break;
        case INTEGER:
          if (in.isArray()) {
            in.setValue(params.getIntegerParameterArray(in.getKey()));
          } else {
            in.setValue(params.getIntegerParameter(in.getKey()));
          }
          break;
        case DOUBLE:
          if (in.isArray()) {
            in.setValue(params.getDoubleParameterArray(in.getKey()));
          } else {
            in.setValue(params.getDoubleParameter(in.getKey()));
          }
          break;
        case LONG:
          if (in.isArray()) {
            in.setValue(params.getLongParameterArray(in.getKey()));
          } else {
            in.setValue(params.getLongParameter(in.getKey()));
          }
          break;
        case FLOAT:
          if (in.isArray()) {
            in.setValue(params.getFloatParameterArray(in.getKey()));
          } else {
            in.setValue(params.getFloatParameter(in.getKey()));
          }
          break;
        case BYTE:
          if (in.isArray()) {
            in.setValue(params.getByteParameterArray(in.getKey()));
          } else {
            in.setValue(params.getByteParameter(in.getKey()));
          }
          break;
        case SHORT:
          if (in.isArray()) {
            in.setValue(params.getShortParameterArray(in.getKey()));
          } else {
            in.setValue(params.getShortParameter(in.getKey()));
          }
          break;
        case BOOLEAN:
          if (in.isArray()) {
            in.setValue(params.getBooleanParameterArray(in.getKey()));
          } else {
            in.setValue(params.getBooleanParameter(in.getKey()));
          }
          break;
        case DATE:
          if (in.isArray()) {
            String arr[] = params.getStringParameterArray(in.getKey());
            if (arr != null) {
              List<Date> dates = new ArrayList<>();
              for (String value : arr) {
                try {
                  dates.add(in.getDateFormat().parse(value));
                } catch (ParseException e) {
                  in.setError("Could not parse the date string: " + value);
                }
              }
              in.setValue(dates.toArray(new Date[] {}));
            }
          } else {
            String value = params.getStringParameter(in.getKey());
            if (value != null) {
              try {
                in.setValue(in.getDateFormat().parse(value));
              } catch (Exception e) {
                in.setError("Could not parse the date string: " + value);
              }
            }
          }
          break;
        case FILE:
          if (in.isArray()) {
            in.setValue(params.getFileUploadArray(in.getKey()));
          } else {
            in.setValue(params.getFileUpload(in.getKey()));
          }
          break;
        case EMAIL:
          String values = params.getStringParameter(in.getKey());
          if (values != null) {
            Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
            String[] arr = values.split(",");
            List<String> emails = new ArrayList<>();
            for (String email : arr) {
              if (email.trim().isEmpty()) {
                continue;
              }
              if (p.matcher(email.trim()).matches()) {
                emails.add(email);
              } else {
                in.setError("Unparseable email [" + email + "]");
                in.setValue(values);
                return;
              }
            }
            in.setValue(emails.toArray(new String[] {}));
          }
          break;
        default:
          break;
      }
    } catch (NumberFormatException e) {
      if (in.isArray()) {
        String arr[] = params.getStringParameterArray(in.getKey());
        if (arr != null) {
          in.setValue(Arrays.toString(arr));
        }
      } else {
        in.setValue(params.getStringParameter(in.getKey()));
      }
      in.setError("Could not parse number");
    } catch (Exception e) {
      in.setError("Checked, but not managed, error occured: " + e.getLocalizedMessage());
      return;
    }
  }
}
