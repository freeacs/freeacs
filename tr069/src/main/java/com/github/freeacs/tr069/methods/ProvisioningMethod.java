package com.github.freeacs.tr069.methods;

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
    GetRPCMethodsResponse(null); // like, its not used..

    private final String abbreviation;

    ProvisioningMethod(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getAbbreviation() {
        return abbreviation;
    }
}
