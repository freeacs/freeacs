package com.github.freeacs.tr069.methods;

public enum Method {
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

    Method(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getAbbreviation() {
        return abbreviation;
    }
}
