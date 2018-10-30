package com.github.freeacs.web.app.input;

import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.web.app.util.DateUtils;
import com.github.freeacs.web.app.util.WebConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The most fundamental class in the input data retrieval system, and effectively defines a request
 * parameter.
 *
 * <p>Is used in every single InputData implementation.
 *
 * <p>This class provides static factory methods for many different types of Inputs.
 *
 * @author Jarl Andre Hubenthal
 */
public class Input {
  /** The key. */
  private String key;

  /** The value. */
  private Object value;

  /** The array. */
  private boolean array;

  /** The type. */
  private InputType type;

  /** The date format. */
  private DateUtils.Format dateFormat;

  /** The error. */
  private String error;

  private static final Logger logger = LoggerFactory.getLogger(Input.class);

  // Hide constructor
  /**
   * Instantiates a new input.
   *
   * @throws IllegalAccessException
   */
  private Input() throws IllegalAccessException {
    throw new IllegalAccessException("Calling the private constructur of Input is not allowed!");
  }

  // Hide constructor
  /**
   * Instantiates a new input.
   *
   * @param key the key
   * @param value the value
   * @param isArray the is array
   * @param type the type
   */
  private Input(String key, Object value, boolean isArray, InputType type) {
    this.error = null;
    this.key = key;
    this.value = value;
    this.array = isArray;
    if (type == null) {
      throw new IllegalArgumentException("InputType cannot be NULL");
    }
    this.type = type;
  }

  /**
   * Instantiates a new input.
   *
   * @param key the key
   * @param value the value
   * @param isArray the is array
   * @param type the type
   * @param df the df
   */
  private Input(String key, Object value, boolean isArray, InputType type, DateUtils.Format df) {
    this(key, value, isArray, type);
    this.dateFormat = df;
  }

  /**
   * Gets the single input.
   *
   * @param key the key
   * @param type the type
   * @return the single input
   */
  private static Input getSingleInput(String key, InputType type) {
    return new Input(key, null, false, type);
  }

  /**
   * Gets the array input.
   *
   * @param key the key
   * @param type the type
   * @return the array input
   */
  private static Input getArrayInput(String key, InputType type) {
    return new Input(key, null, true, type);
  }

  /**
   * Gets the string input.
   *
   * @param key the key
   * @return the string input
   */
  public static Input getStringInput(String key) {
    return getSingleInput(key, InputType.STRING);
  }

  /**
   * Gets the string array input.
   *
   * @param key the key
   * @return the string array input
   */
  public static Input getStringArrayInput(String key) {
    return getArrayInput(key, InputType.STRING);
  }

  /**
   * Gets the integer input.
   *
   * @param key the key
   * @return the integer input
   */
  public static Input getIntegerInput(String key) {
    return getSingleInput(key, InputType.INTEGER);
  }

  /**
   * Gets the integer array input.
   *
   * @param key the key
   * @return the integer array input
   */
  public static Input getIntegerArrayInput(String key) {
    return getArrayInput(key, InputType.INTEGER);
  }

  /**
   * Gets the double input.
   *
   * @param key the key
   * @return the double input
   */
  public static Input getDoubleInput(String key) {
    return getSingleInput(key, InputType.DOUBLE);
  }

  /**
   * Gets the double array input.
   *
   * @param key the key
   * @return the double array input
   */
  public static Input getDoubleArrayInput(String key) {
    return getArrayInput(key, InputType.DOUBLE);
  }

  /**
   * Gets the float input.
   *
   * @param key the key
   * @return the float input
   */
  public static Input getFloatInput(String key) {
    return getSingleInput(key, InputType.FLOAT);
  }

  /**
   * Gets the float array input.
   *
   * @param key the key
   * @return the float array input
   */
  public static Input getFloatArrayInput(String key) {
    return getArrayInput(key, InputType.FLOAT);
  }

  /**
   * Gets the long input.
   *
   * @param key the key
   * @return the long input
   */
  public static Input getLongInput(String key) {
    return getSingleInput(key, InputType.LONG);
  }

  /**
   * Gets the long array input.
   *
   * @param key the key
   * @return the long array input
   */
  public static Input getLongArrayInput(String key) {
    return getArrayInput(key, InputType.LONG);
  }

  /**
   * Gets the short input.
   *
   * @param key the key
   * @return the short input
   */
  public static Input getShortInput(String key) {
    return getSingleInput(key, InputType.SHORT);
  }

  /**
   * Gets the short array input.
   *
   * @param key the key
   * @return the short array input
   */
  public static Input getShortArrayInput(String key) {
    return getArrayInput(key, InputType.SHORT);
  }

  /**
   * Gets the byte input.
   *
   * @param key the key
   * @return the byte input
   */
  public static Input getByteInput(String key) {
    return getSingleInput(key, InputType.BYTE);
  }

  /**
   * Gets the byte array input.
   *
   * @param key the key
   * @return the byte array input
   */
  public static Input getByteArrayInput(String key) {
    return getArrayInput(key, InputType.BYTE);
  }

  /**
   * Gets the boolean input.
   *
   * @param key the key
   * @return the boolean input
   */
  public static Input getBooleanInput(String key) {
    return getSingleInput(key, InputType.BOOLEAN);
  }

  /**
   * Gets the boolean array input.
   *
   * @param key the key
   * @return the boolean array input
   */
  public static Input getBooleanArrayInput(String key) {
    return getArrayInput(key, InputType.BOOLEAN);
  }

  /**
   * Gets the file input.
   *
   * @param key the key
   * @return the file input
   */
  public static Input getFileInput(String key) {
    return getSingleInput(key, InputType.FILE);
  }

  /**
   * Gets the file array input.
   *
   * @param key the key
   * @return the file array input
   */
  public static Input getFileArrayInput(String key) {
    return getArrayInput(key, InputType.FILE);
  }

  /**
   * Gets the date input.
   *
   * @param key the key
   * @param dateOnly the date only
   * @return the date input
   */
  public static Input getDateInput(String key, DateUtils.Format dateOnly) {
    Input in = getSingleInput(key, InputType.DATE);
    in.dateFormat = dateOnly;
    return in;
  }

  /**
   * Gets the date array input.
   *
   * @param key the key
   * @param sdf the sdf
   * @return the date array input
   */
  public static Input getDateArrayInput(String key, DateUtils.Format sdf) {
    Input in = getArrayInput(key, InputType.DATE);
    in.dateFormat = sdf;
    return in;
  }

  /**
   * Gets the email input.
   *
   * @param key the key
   * @return the email input
   */
  public static Input getEmailInput(String key) {
    return getSingleInput(key, InputType.EMAIL);
  }

  /**
   * Gets the key.
   *
   * @return the key
   */
  public String getKey() {
    return key;
  }

  /**
   * Sets the key.
   *
   * @param key the new key
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   * Gets the string without tags.
   *
   * @return the string without tags
   */
  public String getStringWithoutTags() {
    String string = getString();
    if (string != null && !string.isEmpty()) {
      return Escaping.removeHTMLTags(string, Escaping.EscapeType.TAGS_ONLY);
    }
    return null;
  }

  /**
   * Gets the string without tags and content.
   *
   * @return the string without tags and content
   */
  public String getStringWithoutTagsAndContent() {
    String string = getString();
    if (string != null && !string.isEmpty()) {
      return Escaping.removeHTMLTags(string, Escaping.EscapeType.TAGS_AND_CONTENT);
    }
    return null;
  }

  /**
   * Gets the date.
   *
   * @return the date
   */
  public Date getDate() {
    if (type != InputType.DATE) {
      logger.warn(key + " is not a date");
    }
    if (value instanceof Date) {
      return (Date) value;
    }
    return null;
  }

  /**
   * Gets the date or default.
   *
   * @param def the def
   * @return the date or default
   */
  public Date getDateOrDefault(Date def) {
    Date toReturn = getDate();
    if (toReturn != null) {
      return toReturn;
    }
    return def;
  }

  /**
   * Gets the date or default formatted.
   *
   * @param def the def
   * @return the date or default formatted
   */
  public String getDateOrDefaultFormatted(Date def) {
    Date toFormat = getDateOrDefault(def);
    return DateUtils.formatDate(dateFormat, toFormat);
  }

  /**
   * Gets the date formatted.
   *
   * @return the date formatted
   * @throws IllegalArgumentException the illegal argument exception
   */
  public String getDateFormatted() {
    Date toFormat = getDate();
    return format(toFormat);
  }

  /**
   * Format.
   *
   * @param d the d
   * @return the string
   * @throws IllegalArgumentException the illegal argument exception
   */
  public String format(Date d) {
    if (d == null) {
      return null;
    }
    if (dateFormat == null) {
      throw new IllegalArgumentException("No date formatter available");
    }
    return DateUtils.formatDate(dateFormat, d);
  }

  /**
   * Gets the boolean.
   *
   * @return the boolean
   */
  public Boolean getBoolean() {
    return getBoolean(false);
  }

  /**
   * Gets the boolean.
   *
   * @param def the def
   * @return the boolean
   * @throws IllegalArgumentException the illegal argument exception
   */
  public Boolean getBoolean(Boolean def) {
    if (type != InputType.BOOLEAN) {
      logger.warn(key + " is not a Boolean");
    }
    if (value instanceof Boolean) {
      return (Boolean) value;
    }
    return def;
  }

  /**
   * Gets the integer.
   *
   * @return the integer
   */
  public Integer getInteger() {
    return getInteger(null);
  }

  /**
   * Gets the integer.
   *
   * @param def the def
   * @return the integer
   */
  public Integer getInteger(Integer def) {
    if (type != InputType.INTEGER) {
      logger.warn(key + " is not an Integer");
    }
    if (value instanceof Integer) {
      return (Integer) value;
    }
    return def;
  }

  /**
   * Gets the float.
   *
   * @return the float
   */
  public Float getFloat() {
    return getFloat(null);
  }

  /**
   * Gets the float.
   *
   * @param def the def
   * @return the float
   */
  public Float getFloat(Float def) {
    if (type != InputType.FLOAT) {
      logger.warn(key + " is not a Float");
    }
    if (value instanceof Float) {
      return (Float) value;
    }
    return def;
  }

  /**
   * Gets the byte.
   *
   * @return the byte
   */
  public Byte getByte() {
    if (type != InputType.BYTE) {
      logger.warn(key + " is not a Byte");
    }
    if (value instanceof Byte) {
      return (Byte) value;
    }
    return null;
  }

  /**
   * Gets the short.
   *
   * @return the short
   */
  public Short getShort() {
    if (type != InputType.SHORT) {
      logger.warn(key + " is not a Short");
    }
    if (value instanceof Short) {
      return (Short) value;
    }
    return null;
  }

  /**
   * Gets the character.
   *
   * @return the character
   */
  public Character getCharacter() {
    if (type != InputType.CHAR) {
      logger.warn(key + " is not a Character");
    }
    if (value instanceof Character) {
      return (Character) value;
    }
    return null;
  }

  /**
   * Gets the long.
   *
   * @return the long
   */
  public Long getLong() {
    if (type != InputType.LONG) {
      logger.warn(key + " is not a Long");
    }
    if (value instanceof Long) {
      return (Long) value;
    }
    return null;
  }

  /**
   * Gets the double.
   *
   * @return the double
   */
  public Double getDouble() {
    if (type != InputType.DOUBLE) {
      logger.warn(key + " is not a Double");
    }
    if (value instanceof Double) {
      return (Double) value;
    }
    return null;
  }

  /**
   * Gets the double.
   *
   * <p>Returns the default if not found.
   *
   * @return the double
   */
  public Double getDouble(Double def) {
    if (type != InputType.DOUBLE) {
      logger.warn(key + " is not a Double");
    }
    if (value instanceof Double) {
      return (Double) value;
    }
    return def;
  }

  /**
   * Gets the files.
   *
   * @return the files
   */
  public List<byte[]> getFiles() {
    if (type != InputType.FILE) {
      logger.warn(key + " is not a File");
    }
    if (value instanceof FileItem[]) {
      List<byte[]> files = new ArrayList<>();
      for (FileItem item : (FileItem[]) value) {
        files.add(item.get());
      }
      return files;
    }
    return null;
  }

  /**
   * Gets the file as string.
   *
   * @return the file as string
   */
  public String getFileAsString() {
    if (type != InputType.FILE) {
      logger.warn(key + " is not a File");
    }
    if (value instanceof FileItem) {
      return new String(((FileItem) value).get()).trim();
    }
    return null;
  }

  /**
   * Gets the file as bytes.
   *
   * @return the file as bytes
   */
  public byte[] getFileAsBytes() {
    if (type != InputType.FILE) {
      logger.warn(key + " is not a File");
    }
    if (value instanceof FileItem) {
      return ((FileItem) value).get();
    }
    return null;
  }

  /**
   * Gets the file.
   *
   * @return the file
   */
  public FileItem getFile() {
    if (type != InputType.FILE) {
      logger.warn(key + " is not a File");
    }
    if (value instanceof FileItem) {
      return (FileItem) value;
    }
    return null;
  }

  /**
   * Does not care wether or not the value is an actual String.<br>
   * Will return a string by calling toString on the object if the value is not the protected
   * constant for "all items"
   *
   * @return The string representation of the value
   */
  public String getString() {
    if (value == null) {
      return null;
    }
    String string;
    if (value instanceof String[]) {
      string = StringUtils.join((String[]) value, ",");
    } else if (value instanceof Unittype) {
      string = ((Unittype) value).getName();
    } else if (value instanceof Profile) {
      string = ((Profile) value).getName();
    } else {
      string = value.toString();
    }
    if (string != null && !WebConstants.ALL_ITEMS_OR_DEFAULT.equals(string)) {
      return string.trim();
    }
    return null;
  }

  /**
   * Gets the email.
   *
   * @return the email
   */
  public String getEmail() {
    if (type != InputType.EMAIL) {
      logger.warn(key + " is not an email");
    }
    if (value instanceof String) {
      return (String) value;
    }
    if (value instanceof String[]) {
      return StringUtils.join((String[]) value, ",");
    }
    return null;
  }

  /**
   * Sets the error.
   *
   * @param string the new error
   */
  public void setError(String string) {
    error = string;
  }

  /**
   * Gets the error.
   *
   * @return the error
   */
  public String getError() {
    return error;
  }

  /**
   * If any exceptions occur when converting the input to the InputType<br>
   * this method will return the actual string value that was sent to the servlet. <br>
   * <br>
   * Can for example be used for testing if a number conversion went wrong:<br>
   * if(id.getValue()!=null && id.getInteger()==null)<br>
   * &nbsp;&nbsp;in.setError("Id is not an integer"); <br>
   * <br>
   * Also, this method should be used when binding the form, so the user can see what he typed and
   * the error it generated.
   *
   * @return The value object
   */
  public Object getValue() {
    return value;
  }

  /**
   * Gets the type.
   *
   * @return the type
   */
  public InputType getType() {
    return type;
  }

  /**
   * Sets the type.
   *
   * @param type the new type
   */
  public void setType(InputType type) {
    this.type = type;
  }

  /**
   * Checks if is array.
   *
   * @return true, if is array
   */
  public boolean isArray() {
    return this.array;
  }

  /**
   * Gets the string array.
   *
   * @return the string array
   */
  public String[] getStringArray() {
    if (value instanceof String[]) {
      return (String[]) value;
    }
    return null;
  }

  /**
   * Gets the string list.
   *
   * @return the string list
   */
  public List<String> getStringList() {
    return Arrays.asList(getStringArray());
  }

  /**
   * Checks if is value.
   *
   * @param valueToCheckFor the value to check for
   * @return true, if is value
   */
  public boolean isValue(String valueToCheckFor) {
    String string = getString();
    return valueToCheckFor.equals(string);
  }

  /**
   * Checks if is null or value.
   *
   * @param valueToCheckFor the value to check for
   * @return true, if is null or value
   */
  public boolean isNullOrValue(String valueToCheckFor) {
    String string = getString();
    return string == null || valueToCheckFor.equals(string);
  }

  /**
   * Not null nor value.
   *
   * @param valueToCheckFor the value to check for
   * @return true, if successful
   */
  public boolean notNullNorValue(String valueToCheckFor) {
    String string = getString();
    return string != null && (valueToCheckFor == null || !valueToCheckFor.equals(string));
  }

  /**
   * Checks for value.
   *
   * @param valueToCheckFor the value to check for
   * @return true, if successful
   */
  public boolean hasValue(String valueToCheckFor) {
    String string = getString();
    return valueToCheckFor.equals(string);
  }

  /**
   * Sets the value.
   *
   * @param value the new value
   */
  public void setValue(Object value) {
    if (value instanceof String) {
      this.value = ((String) value).trim();
    } else {
      this.value = value;
    }
  }

  /**
   * Gets the date format.
   *
   * @return the date format
   */
  public DateUtils.Format getDateFormat() {
    return dateFormat;
  }

  public boolean startsWith(String string) {
    return ((String) value).startsWith(string);
  }

  public String getString(String returnIfNull) {
    String mainReturnValue = getString();
    if (mainReturnValue != null) {
      return mainReturnValue;
    }
    return returnIfNull;
  }

  public String getStringOrDefault(String returnIfNull) {
    return getString(returnIfNull);
  }

  public boolean notValue(String oldUnittype) {
    return (getString() == null && oldUnittype != null)
        || (getString() != null && getString().equals(oldUnittype));
  }
}
