package com.github.freeacs.tr069.methods;

import lombok.Getter;

import java.util.stream.Stream;

@Getter
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

    private static final String RESPONSE_POSTFIX = "Response";

    private final String abbreviation;

    ProvisioningMethod(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    /**
     * Extract request method name.
     */
    public static ProvisioningMethod fromString(final String mehtodAsString) {
        return Stream.of(values())
                .filter(m -> mehtodAsString != null)
                .filter(m -> mehtodAsString.equals(m.name()) || mehtodAsString.equals(m.name() + RESPONSE_POSTFIX))
                .findFirst()
                .orElse(Empty);
    }
}
