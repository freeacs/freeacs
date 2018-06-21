package com.github.freeacs.springshell;

import org.springframework.stereotype.Component;

@Component
public class ShellContext {

    private String unitType;
    private String profile;
    private String unit;

    public String getUnitType() {
        return unitType;
    }

    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
