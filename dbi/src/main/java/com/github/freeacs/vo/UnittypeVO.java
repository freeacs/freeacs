package com.github.freeacs.vo;

import com.github.freeacs.dbi.ProvisioningProtocol;

public class UnittypeVO {
    private Long unitTypeId;
    private String matcherId;
    private String unitTypeName;
    private String vendorName;
    private String description;
    private ProvisioningProtocol protocol;

    public Long getUnitTypeId() {
        return unitTypeId;
    }

    public void setUnitTypeId(Long unitTypeId) {
        this.unitTypeId = unitTypeId;
    }

    public String getMatcherId() {
        return matcherId;
    }

    public void setMatcherId(String matcherId) {
        this.matcherId = matcherId;
    }

    public String getUnitTypeName() {
        return unitTypeName;
    }

    public void setUnitTypeName(String unitTypeName) {
        this.unitTypeName = unitTypeName;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProvisioningProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(ProvisioningProtocol protocol) {
        this.protocol = protocol;
    }

    public String toString() {
        return "[" + unitTypeId + "] [" + unitTypeName + "] [" + matcherId + "] [" + vendorName + "] [" + protocol + "]";
    }
}
