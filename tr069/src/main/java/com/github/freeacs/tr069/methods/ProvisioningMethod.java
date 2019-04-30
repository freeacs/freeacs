package com.github.freeacs.tr069.methods;

import java.util.stream.Stream;

public enum ProvisioningMethod {
    Empty("EM"),
    Fault("FA"),
    Inform("IN"),
    GetParameterValues("GPV"),
    GetParameterNames("GPN"),
    SetParameterValues("SPV"),
    TransferComplete("TC"),
    AutonomousTransferComplete("ATC"),
    Download("DO"),
    Reboot("RE"),
    FactoryReset("FR"),
    GetRPCMethods("GRPC");

    private static String RESPONSE_POSTFIX = "Response";

    private final String abbreviation;

    ProvisioningMethod(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    /**
     * Extract request method name.
     */
    public static ProvisioningMethod extractMethodFrom(final String xml) {
        return Stream.of(values())
                .filter(m -> xml != null)
                .filter(m -> xml.contains("<cwmp:" + m.name()) || xml.contains("<cwmp:" + m.name() + RESPONSE_POSTFIX))
                .findFirst()
                .orElse(Empty);
    }
}
