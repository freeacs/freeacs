package com.github.freeacs.tr069.methods.decision.GetParameterValues;

import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.tr069.CPEParameters;
import com.github.freeacs.tr069.base.ACSParameters;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

@Getter
public class GetScriptVersion {
    private static final Logger logger = LoggerFactory.getLogger(GetScriptVersion.class);

    private final ACSParameters oweraParams;
    private final CPEParameters cpeParams;
    private String scriptVersion;
    private String scriptName;

    public GetScriptVersion(ACSParameters oweraParams, CPEParameters cpeParams) {
        this.oweraParams = oweraParams;
        this.cpeParams = cpeParams;
    }

    public GetScriptVersion build() {
        oweraParams.getAcsParams().entrySet().stream().flatMap(entry -> {
            if (SystemParameters.isTR069ScriptVersionParameter(entry.getKey())) {
                // The config-file-name is the same as the script-name retrieved from the system-parameter
                String name = SystemParameters.getTR069ScriptName(entry.getKey());
                String scriptVersionFromCPE = cpeParams.getConfigFileMap().get(name);
                if (scriptVersionFromCPE == null) {
                    logger.error("No script version found for " + name);
                    return Stream.empty();
                }
                String svDB = entry.getValue().getValue();
                if (svDB != null && !svDB.equals(scriptVersionFromCPE)) {
                    // upgrade
                    scriptVersion = svDB;
                    scriptName = name;
                    return Stream.of(new NameAndValue<>(name, svDB));
                }
            }
            return Stream.empty();
        }).findFirst().ifPresent(tuple -> {
            scriptName = tuple.name;
            scriptVersion = tuple.value;
        });
        return this;
    }

    private static class NameAndValue<T1, T2> {
        T1 name;
        T2 value;
        NameAndValue(T1 name, T2 value) {
            this.name = name;
            this.value = value;
        }
    }
}
