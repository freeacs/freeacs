package com.github.freeacs.dbi.util;

public class SystemConstants {
  public static String DEFAULT_INSPECTION_MESSAGE = "N/A";

  public static String KEYROOT_D = "Device.";
  public static String KEYROOT_IGD = "InternetGatewayDevice.";

  public static int DEFAULT_SERVICEWINDOW_FREQUENCY_INT = 7;
  public static int DEFAULT_SERVICEWINDOW_SPREAD_INT = 20;

  public static String DEFAULT_SERVICEWINDOW_FREQUENCY_STR =
      String.valueOf(DEFAULT_SERVICEWINDOW_FREQUENCY_INT);
  public static String DEFAULT_SERVICEWINDOW_SPREAD_STR =
      String.valueOf(DEFAULT_SERVICEWINDOW_SPREAD_INT);
}
