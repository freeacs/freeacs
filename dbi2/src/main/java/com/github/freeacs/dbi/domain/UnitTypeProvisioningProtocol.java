package com.github.freeacs.dbi.domain;

public enum UnitTypeProvisioningProtocol {
    TR069,
    HTTP,
    OPP,
    NA,
    TFTP;

    public static UnitTypeProvisioningProtocol toEnum(String s) {
        if (s == null || "TR-069".equals(s)) {
            return TR069;
        }
        if ("N/A".equals(s)) {
            return NA;
        }
        return valueOf(s);
    }
}