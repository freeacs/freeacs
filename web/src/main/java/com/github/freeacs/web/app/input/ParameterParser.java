package com.github.freeacs.web.app.input;

import com.github.freeacs.web.app.util.SessionCache;
import com.github.freeacs.web.app.util.SessionData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Wraps the HttpServletRequest so file uploads can be done transparently.
 *
 * @author Jarl Andre Hubenthal
 */
public class ParameterParser {
  //	private static final String URL_STRING_ENCODING = "UTF-8";

  /** The req. */
  private AbstractRequest req;

  /** The is multipart. */
  private boolean isMultipart;

  /** The params. */
  private Map<String, List<String>> params;

  /** The files. */
  private Map<String, List<FileItem>> files;

  /**
   * Instantiates a new parameter parser.
   *
   * @param req the req
   * @throws FileUploadException the file upload exception
   */
  public ParameterParser(HttpServletRequest req) throws FileUploadException {
    this.req = new AbstractRequest(req);
    isMultipart = ServletFileUpload.isMultipartContent(req);
    // define files anyway so i can use it to return empty enumeration from getFileUploadNames()
    files = new HashMap<>();
    if (isMultipart) {
      // only define params if i need it
      params = new HashMap<>();
      parseUploadParams(req);
    }
  }

  /**
   * Gets the request url.
   *
   * @return the request url
   */
  public String getRequestURL() {
    return getRequestURLButExcludeSomeParameters(new String[] {});
  }

  /**
   * Gets the request url but exclude some parameters.
   *
   * @param exclude the exclude
   * @return the request url but exclude some parameters
   */
  public String getRequestURLButExcludeSomeParameters(String... exclude) {
    StringBuffer target = req.getRequestURL();
    Enumeration<?> parms = req.getParameterNames();
    if (parms.hasMoreElements()) {
      target.append("?");
    }
    List<String> excludedList = Arrays.asList(exclude);
    while (parms.hasMoreElements()) {
      String object = (String) parms.nextElement();
      if ("index".equals(object) || excludedList.contains(object)) {
        continue;
      }
      target.append(object).append("=").append(req.getParameter(object));
      if (parms.hasMoreElements()) {
        target.append("&");
      }
    }
    return target.toString();
  }

  /**
   * Parses the upload params.
   *
   * @param request the request
   * @throws FileUploadException the file upload exception
   */
  private void parseUploadParams(HttpServletRequest request) throws FileUploadException {
    ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
    List<?> items = upload.parseRequest(request);
    for (Object item : items) {
      FileItem fileItem = (FileItem) item;
      if (fileItem.isFormField()) {
        List<String> arr = params.get(fileItem.getFieldName());
        if (arr == null) {
          arr = new ArrayList<>();
          params.put(fileItem.getFieldName(), arr);
        }
        arr.add(fileItem.getString());
      } else {
        List<FileItem> arr = files.get(fileItem.getFieldName());
        if (arr == null) {
          arr = new ArrayList<>();
          files.put(fileItem.getFieldName(), arr);
        }
        arr.add(fileItem);
      }
    }
  }

  /**
   * Gets the http servlet request.
   *
   * @return the http servlet request
   */
  public AbstractRequest getHttpServletRequest() {
    return req;
  }

  /**
   * Retrieves an uploaded file from the HttpUploadServletRequest.
   *
   * @param name the name
   * @return FileItem
   */
  public FileItem getFileUpload(String name) {
    List<FileItem> arr = files.get(name);
    if (arr != null) {
      return arr.get(0);
    }
    return null;
  }

  /**
   * Will only return one of each fieldName.
   *
   * @return The enumeration of file upload names
   */
  public Enumeration<String> getFileUploadNames() {
    return Collections.enumeration(files.keySet());
  }

  /**
   * Gets the file upload array.
   *
   * @param name the name
   * @return the file upload array
   */
  public FileItem[] getFileUploadArray(String name) {
    List<FileItem> arr = files.get(name);
    if (arr != null) {
      return arr.toArray(new FileItem[] {});
    }
    return null;
  }

  /**
   * Gets the key enumeration.
   *
   * @return the key enumeration
   */
  public Enumeration<?> getKeyEnumeration() {
    return req.getParameterNames();
  }

  /**
   * Gets the string parameter array.
   *
   * @param name the name
   * @return the string parameter array
   */
  public String[] getStringParameterArray(String name) {
    return getStringParameterArray(name, null);
  }

  /**
   * Gets the string parameter array.
   *
   * @param name the name
   * @param def the def
   * @return the string parameter array
   */
  private String[] getStringParameterArray(String name, String[] def) {
    try {
      String[] values = null;

      if (isMultipart) {
        List<String> arr = params.get(name);
        if (arr != null) {
          values = arr.toArray(new String[] {});
        }
      } else {
        values = req.getParameterValues(name);
      }

      if (values == null) {
        throw new ParameterNotFoundException(name + " not found");
      }

      return values;
    } catch (Exception e) {
      return def;
    }
  }

  /**
   * Gets the string parameter.
   *
   * @param name the name
   * @return the string parameter
   */
  public String getStringParameter(String name) {
    return getStringParameter(name, null);
  }

  /**
   * Gets the string parameter.
   *
   * @param name the name
   * @param def the def
   * @return the string parameter
   */
  public String getStringParameter(String name, String def) {
    try {
      String value = null;

      if (isMultipart) {
        List<String> arr = params.get(name);
        if (arr != null) {
          value = arr.get(0);
        }
      } else {
        value = req.getParameter(name);
      }

      if (value == null) {
        throw new ParameterNotFoundException(name + " not found");
      }

      return value.trim();
    } catch (Exception e) {
      return def;
    }
  }

  /**
   * Gets the boolean.
   *
   * @param name the name
   * @return the boolean
   */
  public Boolean getBoolean(String name) {
    return getBooleanParameter(name);
  }

  /**
   * Gets the boolean.
   *
   * @param name the name
   * @param def the def
   * @return the boolean
   */
  public Boolean getBoolean(String name, boolean def) {
    return getBooleanParameter(name, def);
  }

  /**
   * Gets the boolean parameter.
   *
   * @param name the name
   * @return the boolean parameter
   */
  public Boolean getBooleanParameter(String name) {
    return getBooleanParameter(name, false);
  }

  /**
   * Gets the boolean parameter.
   *
   * @param name the name
   * @param def the def
   * @return the boolean parameter
   */
  public Boolean getBooleanParameter(String name, Boolean def) {
    try {
      String s = getStringParameter(name);
      if (s == null) {
        return def;
      }
      if ("on".equals(s)) {
        return true;
      }
      return Boolean.valueOf(s);
    } catch (Exception e) {
      return def;
    }
  }

  /**
   * Gets the byte parameter.
   *
   * @param name the name
   * @return the byte parameter
   */
  public Byte getByteParameter(String name) {
    return getByteParameter(name, null);
  }

  /**
   * Gets the byte parameter.
   *
   * @param name the name
   * @param def the def
   * @return the byte parameter
   */
  private Byte getByteParameter(String name, Byte def) {
    try {
      return Byte.parseByte(getStringParameter(name));
    } catch (Exception e) {
      return def;
    }
  }

  /**
   * Gets the char parameter.
   *
   * @param name the name
   * @return the char parameter
   */
  public Character getCharParameter(String name) {
    return getCharParameter(name, null);
  }

  /**
   * Gets the char parameter.
   *
   * @param name the name
   * @param def the def
   * @return the char parameter
   */
  private Character getCharParameter(String name, Character def) {
    try {
      String param = getStringParameter(name);
      return param.charAt(0);
    } catch (Exception e) {
      return def;
    }
  }

  /**
   * Gets the double parameter.
   *
   * @param name the name
   * @return the double parameter
   */
  public Double getDoubleParameter(String name) {
    return getDoubleParameter(name, null);
  }

  /**
   * Gets the double parameter.
   *
   * @param name the name
   * @param def the def
   * @return the double parameter
   */
  private Double getDoubleParameter(String name, Double def) {
    try {
      return Double.parseDouble(getStringParameter(name));
    } catch (Exception e) {
      return def;
    }
  }

  /**
   * Gets the float parameter.
   *
   * @param name the name
   * @return the float parameter
   */
  public Float getFloatParameter(String name) {
    return getFloatParameter(name, null);
  }

  /**
   * Gets the float parameter.
   *
   * @param name the name
   * @param def the def
   * @return the float parameter
   */
  private Float getFloatParameter(String name, Float def) {
    try {
      return Float.parseFloat(getStringParameter(name));
    } catch (Exception e) {
      return def;
    }
  }

  /**
   * Gets the integer parameter.
   *
   * @param name the name
   * @return the integer parameter
   * @throws Exception the exception
   */
  public Integer getIntegerParameter(String name) {
    return getIntegerParameter(name, null);
  }

  /**
   * This method returns zero if the parameter in question is <code>"0"</code>.
   *
   * @param name The parameter name
   * @param def The default value to return
   * @return The converted Integer
   * @throws Exception
   */
  public Integer getIntegerParameter(String name, Integer def) {
    try {
      String toParse = getStringParameter(name);
      if ("0".equals(toParse)) {
        return 0;
      } else if (toParse != null) {
        return Integer.parseInt(toParse);
      } else {
        return def;
      }
    } catch (NumberFormatException e) {
      if (def != null) {
        return def;
      }
      // TODO Log this
      throw e;
    }
  }

  /**
   * Gets the long parameter.
   *
   * @param name the name
   * @return the long parameter
   */
  public Long getLongParameter(String name) {
    return getLongParameter(name, null);
  }

  /**
   * Gets the long parameter.
   *
   * @param name the name
   * @param def the def
   * @return the long parameter
   */
  private Long getLongParameter(String name, Long def) {
    try {
      return Long.parseLong(getStringParameter(name));
    } catch (Exception e) {
      return def;
    }
  }

  /**
   * Gets the short parameter.
   *
   * @param name the name
   * @return the short parameter
   */
  public Short getShortParameter(String name) {
    return getShortParameter(name, null);
  }

  /**
   * Gets the short parameter.
   *
   * @param name the name
   * @param def the def
   * @return the short parameter
   */
  private Short getShortParameter(String name, Short def) {
    try {
      return Short.parseShort(getStringParameter(name));
    } catch (Exception e) {
      return def;
    }
  }

  /**
   * Gets the integer parameter array.
   *
   * @param name the name
   * @return the integer parameter array
   * @throws NumberFormatException the number format exception
   */
  public Integer[] getIntegerParameterArray(String name) {
    List<Integer> ints = new ArrayList<>();
    String arr[] = getStringParameterArray(name, new String[] {});
    for (String value : arr) {
      ints.add(Integer.parseInt(value));
    }
    return ints.toArray(new Integer[] {});
  }

  /**
   * Gets the double parameter array.
   *
   * @param name the name
   * @return the double parameter array
   * @throws NumberFormatException the number format exception
   */
  public Double[] getDoubleParameterArray(String name) {
    List<Double> ints = new ArrayList<>();
    String arr[] = getStringParameterArray(name, new String[] {});
    for (String value : arr) {
      ints.add(Double.parseDouble(value));
    }
    return ints.toArray(new Double[] {});
  }

  /**
   * Gets the float parameter array.
   *
   * @param name the name
   * @return the float parameter array
   * @throws NumberFormatException the number format exception
   */
  public Float[] getFloatParameterArray(String name) {
    List<Float> ints = new ArrayList<>();
    String arr[] = getStringParameterArray(name, new String[] {});
    for (String value : arr) {
      ints.add(Float.parseFloat(value));
    }
    return ints.toArray(new Float[] {});
  }

  /**
   * Gets the long parameter array.
   *
   * @param name the name
   * @return the long parameter array
   * @throws NumberFormatException the number format exception
   */
  public Long[] getLongParameterArray(String name) {
    List<Long> ints = new ArrayList<>();
    String arr[] = getStringParameterArray(name, new String[] {});
    for (String value : arr) {
      ints.add(Long.parseLong(value));
    }
    return ints.toArray(new Long[] {});
  }

  /**
   * Gets the short parameter array.
   *
   * @param name the name
   * @return the short parameter array
   * @throws NumberFormatException the number format exception
   */
  public Short[] getShortParameterArray(String name) {
    List<Short> ints = new ArrayList<>();
    String arr[] = getStringParameterArray(name, new String[] {});
    for (String value : arr) {
      ints.add(Short.parseShort(value));
    }
    return ints.toArray(new Short[] {});
  }

  /**
   * Gets the byte parameter array.
   *
   * @param name the name
   * @return the byte parameter array
   * @throws NumberFormatException the number format exception
   */
  public Byte[] getByteParameterArray(String name) {
    List<Byte> ints = new ArrayList<>();
    String arr[] = getStringParameterArray(name, new String[] {});
    for (String value : arr) {
      ints.add(Byte.parseByte(value));
    }
    return ints.toArray(new Byte[] {});
  }

  /**
   * Gets the boolean parameter array.
   *
   * @param name the name
   * @return the boolean parameter array
   */
  public Boolean[] getBooleanParameterArray(String name) {
    List<Boolean> ints = new ArrayList<>();
    String arr[] = getStringParameterArray(name, new String[] {});
    for (String value : arr) {
      ints.add(Boolean.parseBoolean(value));
    }
    return ints.toArray(new Boolean[] {});
  }

  /**
   * Convenience method for getHttpServletRequest().getSession()
   *
   * @return The HttpSession
   */
  public HttpSession getSession() {
    return req.getSession();
  }

  /**
   * Convenience method that call getStringParameter.
   *
   * @param string the string
   * @return The parameter string
   */
  public String getParameter(String string) {
    return getStringParameter(string);
  }

  /**
   * Convenience method that calls the private getStringParameter(name,def).
   *
   * @param string the string
   * @param def the def
   * @return The parameter string
   */
  public String getParameter(String string, String def) {
    return getStringParameter(string, def);
  }

  /**
   * Gets the session data.
   *
   * @return the session data
   */
  public SessionData getSessionData() {
    return SessionCache.getSessionData(req.getSession().getId());
  }
}
