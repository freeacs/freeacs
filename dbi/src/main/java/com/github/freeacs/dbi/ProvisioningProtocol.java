package com.github.freeacs.dbi;

public enum ProvisioningProtocol {
    TR069, HTTP, OPP, NA, TFTP;

    public static ProvisioningProtocol toEnum(String s) {
        if (s == null || s.equals("TR-069"))
            return TR069;
        if (s.equals("N/A"))
            return NA;
        return valueOf(s);
    }
}
