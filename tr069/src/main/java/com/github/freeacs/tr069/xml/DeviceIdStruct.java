package com.github.freeacs.tr069.xml;

import lombok.Data;

import java.util.Optional;
import java.util.stream.Stream;

@Data
public class DeviceIdStruct {
  private static final char[] ALLOWED_CHARS_IN_PRODUCT_CLASS =
          "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_".toCharArray();

  public static final String ID = "DeviceIdStruct";

  private String manufacturer;
  private String oui;
  private String productClass;
  private String serialNumber;

  DeviceIdStruct() {
    this.manufacturer = null;
    this.oui = null;
    this.productClass = null;
    this.serialNumber = null;
  }

  void setProductClass(String productClass) {
    this.productClass = DeviceIdStructHelper.parseProductClass(productClass);
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
    return Stream.of(input.toCharArray())
        .reduce(new StringBuffer(), (stringBuffer, charArray) -> {
            for (char c : charArray) {
                if (c == '/' || c == '\\') {
                    stringBuffer.append('-');
                } else if (c == '²') {
                    stringBuffer.append('2');
                } else {
                    for (char a : ALLOWED_CHARS_IN_PRODUCT_CLASS) {
                        if (c == a) stringBuffer.append(a);
                    }
                }
            }
            return stringBuffer;
        }, StringBuffer::append).toString();
  }
}
