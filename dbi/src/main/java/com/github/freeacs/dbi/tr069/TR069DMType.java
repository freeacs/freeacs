package com.github.freeacs.dbi.tr069;

public enum TR069DMType {
  BOOLEAN(0, 1, "xsd:boolean"),
  DATETIME(0, 0, "xsd:dateTime"),
  BASE64(0, 32, "xsd:base64"),
  HEXBINARY(0, 32, "xsd:hexBinary"),
  STRING(0, 32, "xsd:string"),
  ALIAS(0, 64, "xsd:string"),
  MACADDRESS(17, 17, "xsd:string"),
  IPADDRESS(0, 45, "xsd:string"),
  IPV6ADDRESS(0, 45, "xsd:string"),
  IPV4ADDRESS(0, 15, "xsd:string"),
  IPPREFIX(0, 49, "xsd:string"),
  IPV4PREFIX(0, 18, "xsd:string"),
  IPV6PREFIX(0, 49, "xsd:string"),
  INTEGER(Integer.MIN_VALUE, Integer.MAX_VALUE, "xsd:integer"),
  INT(Integer.MIN_VALUE, Integer.MAX_VALUE, "xsd:int"),
  LONG(Long.MIN_VALUE, Long.MAX_VALUE, "xsd:long"),
  UNSIGNEDINT(0, 4294967295L, "xsd:unsignedInt"),
  UNSIGNEDLONG(0, Long.MAX_VALUE, "xsd:unsignedLong"),
  /** Real-max is 18446744073709551615. */
  STATSCOUNTER32(0, 4294967295L, "xsd:unsignedInt"),
  STATSCOUNTER64(0, Long.MAX_VALUE, "xsd:unsignedLong"),
  /** Real-max is 18446744073709551615. */
  DBM1000(Integer.MIN_VALUE, Integer.MAX_VALUE, "xsd:integer");

  private long min;
  private long max;
  private String xsdType;

  TR069DMType(long min, long max, String xsdType) {
    this.min = min;
    this.max = max;
    this.xsdType = xsdType;
  }

  /** Private TR069DMType(int min, int max, String xsdType) { this.min = min; this.max = max; } */
  public long getMin() {
    return min;
  }

  public long getMax() {
    return max;
  }

  public String getXsdType() {
    return xsdType;
  }
}
