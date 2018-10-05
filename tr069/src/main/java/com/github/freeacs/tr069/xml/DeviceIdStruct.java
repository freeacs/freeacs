package com.github.freeacs.tr069.xml;

import static com.github.freeacs.tr069.xml.DeviceIdStructHelper.parseProductClass;

import java.util.Optional;

public class DeviceIdStruct {

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
    this.productClass = parseProductClass(productClass);
  }

  public String getSerialNumber() {
    return serialNumber;
  }

  public void setSerialNumber(String serialNumber) {
    this.serialNumber = serialNumber;
  }
}

interface DeviceIdStructHelper {
  static String parseProductClass(String productClass) {
    return Optional.ofNullable(productClass)
        .map(s -> s.replaceAll("\\\\", "-").replaceAll("/", "-"))
        .orElse(productClass);
  }
}
