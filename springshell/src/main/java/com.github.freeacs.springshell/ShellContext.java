package com.github.freeacs.springshell;

import org.springframework.stereotype.Component;

@Component
public class ShellContext {

    private String unitType;
    private String profile;
    private String unit;

    void setUnitType(String unitType) {
        this.unitType = unitType;
    }

    void setProfile(String profile) {
        this.profile = profile;
    }

    void setUnit(String unit) {
        this.unit = unit;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (unitType != null) {
            sb.append("(").append(unitType).append(":ut):");
        }
        if (profile != null) {
            sb.append("(").append(profile).append(":pr):");
        }
        if (unit != null) {
            sb.append("(").append(unit).append(":u):");
        }
        return sb.toString();
    }
}
