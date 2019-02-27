package com.github.freeacs.tr069;

import com.github.freeacs.base.ACSParameters;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.tr069.xml.ParameterValueStruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class GetScriptVersion {
    private static Logger logger = LoggerFactory.getLogger(GetScriptVersion.class);

    private ACSParameters oweraParams;
    private CPEParameters cpeParams;
    private String scriptVersion;
    private String scriptName;

    GetScriptVersion(ACSParameters oweraParams, CPEParameters cpeParams) {
        this.oweraParams = oweraParams;
        this.cpeParams = cpeParams;
    }

    String getScriptVersion() {
        return scriptVersion;
    }

    String getScriptName() {
        return scriptName;
    }

    GetScriptVersion build() {
        Map<String, ParameterValueStruct> opMap = oweraParams.getAcsParams();
        for (Map.Entry<String, ParameterValueStruct> entry : opMap.entrySet()) {
            if (SystemParameters.isTR069ScriptVersionParameter(entry.getKey())) {
                String svDB = entry.getValue().getValue();
                // The config-file-name is the same as the script-name retrieved from
                // the system-parameter
                String name = SystemParameters.getTR069ScriptName(entry.getKey());
                String scriptVersionFromCPE = cpeParams.getConfigFileMap().get(name);
                if (scriptVersionFromCPE == null) {
                    logger.error("No script version found for " + name);
                } else if (svDB != null && !svDB.equals(scriptVersionFromCPE)) {
                    // upgrade
                    scriptVersion = svDB;
                    scriptName = name;
                    break;
                }
            }
        }
        return this;
    }
}