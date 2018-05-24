package com.github.freeacs.dao;

import com.github.freeacs.dbi.UnittypeParameterFlag;

public class UnittypeParameterVO {
    private Long unitTypeId;
    private Long unitTypeParamId;
    private String name;
    private String flags;

    public Long getUnitTypeId() {
        return unitTypeId;
    }

    public void setUnitTypeId(Long unitTypeId) {
        this.unitTypeId = unitTypeId;
    }

    public Long getUnitTypeParamId() {
        return unitTypeParamId;
    }

    public void setUnitTypeParamId(Long unitTypeParamId) {
        this.unitTypeParamId = unitTypeParamId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFlags() {
        return flags;
    }

    public void setFlags(String flags) {
        this.flags = new UnittypeParameterFlag(flags).getFlag();
    }

    public String toString() {
        return "[" + unitTypeParamId + "] " + "[" + unitTypeId + "] [" + name + "] [" + flags + "]";
    }
}
