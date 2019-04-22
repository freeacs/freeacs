package com.github.freeacs.tr069.xml;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ParameterList {
    private List<ParameterValueStruct> parameterValueStructArrayList = new ArrayList<>();
    private List<ParameterInfoStruct> parameterInfoList = new ArrayList<>();
    private List<ParameterAttributeStruct> parameterAttributeList = new ArrayList<>();

    public void addParameterValueStruct(ParameterValueStruct param) {
        this.parameterValueStructArrayList.add(param);
    }

    void addParameterInfoStruct(ParameterInfoStruct param) {
        this.parameterInfoList.add(param);
    }

    void addParameterAttributeStruct(ParameterAttributeStruct attr) {
        this.parameterAttributeList.add(attr);
    }

    public String getParameterValueByKey(String keyName) {
        return this.parameterValueStructArrayList
                .stream()
                .filter(parameter -> keyName.equals(parameter.getName()))
                .findFirst()
                .orElse(new ParameterValueStruct(keyName, ""))
                .getValue();
    }

    public void addOrChangeParameterValueStruct(String key, String value, String type) {
        boolean changed = false;
        for (ParameterValueStruct struct : this.parameterValueStructArrayList) {
            if (struct.getName().equals(key)) {
                struct.setValue(value);
                changed = true;
                break;
            }
        }
        if (!changed) {
            this.parameterValueStructArrayList.add(new ParameterValueStruct(key, value, type));
        }
    }
}
