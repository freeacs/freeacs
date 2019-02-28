package com.github.freeacs.tr069.xml;

import java.util.Optional;

public class DeviceIdStruct {
  private static final char[] ALLOWED_CHARS_IN_PRODUCT_CLASS =
          "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_".toCharArray();

  public static final String ID = "DeviceIdStruct";

  private String manufacturer;
  private String oui;
  private String productClass;
  private String serialNumber;

  public DeviceIdStruct() {
    this.manufacturer = null;
    this.oui = null;
    this.productClass = null;
    this.serialNumber = null;
  }

  public String getManufacturer() {
    return manufacturer;
  }

  public void setManufacturer(String manufacturer) {
    this.manufacturer = manufacturer;
  }

  public String getOui() {
    return oui;
  }

  public void setOui(String oui) {
    this.oui = oui;
  }

  public String getProductClass() {
    return productClass;
  }

  public void setProductClass(String productClass) {
    this.productClass = DeviceIdStructHelper.parseProductClass(productClass);
  }

  public String getSerialNumber() {
    return serialNumber;
  }

  public void setSerialNumber(String serialNumber) {
    this.serialNumber = serialNumber;
  }

  private interface DeviceIdStructHelper {
    static String parseProductClass(String productClass) {
      return Optional.ofNullable(productClass)
          .map(DeviceIdStruct::safeChar)
          .orElse(productClass);
    }
  }

  // to hell with regex, this method is EASY to read.
  private static String safeChar(String input) {
    final char[] charArray = input.toCharArray();
    final StringBuffer result = new StringBuffer();
    for (char c : charArray) {
      if (c == '/') {
        result.append('-');
      } else if (c == '\\') {
        result.append('-');
      } else {
        for (char a : ALLOWED_CHARS_IN_PRODUCT_CLASS) {
          if (c == a) result.append(a);
        }
      }
    }
    return result.toString();
  }
}
