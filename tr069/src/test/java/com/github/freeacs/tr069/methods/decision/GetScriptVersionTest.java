package com.github.freeacs.tr069.methods.decision;

import com.github.freeacs.tr069.base.ACSParameters;
import com.github.freeacs.tr069.CPEParameters;
import com.github.freeacs.tr069.methods.decision.GetParameterValues.GetScriptVersion;
import com.github.freeacs.tr069.xml.ParameterValueStruct;
import org.junit.Test;

import static org.junit.Assert.*;

public class GetScriptVersionTest {

    @Test
    public void scriptVersionIsNotFoundWhenThereIsNoVersionParameterInCpeParameters() {
        // Given:
        ACSParameters acsParameters = new ACSParameters();
        acsParameters.putPvs("System.X_FREEACS-COM.TR069Script.SomeName.Version", new ParameterValueStruct(
                "Device.DeviceInfo.VendorConfigFile.1.Version",
                "1",
                "xsd:string"
        ));
        CPEParameters cpeParameters = new CPEParameters("Device.");
        cpeParameters.getCpeParams().put("Device.DeviceInfo.VendorConfigFile.1.Name", new ParameterValueStruct(
                "Device.DeviceInfo.VendorConfigFile.1.Name",
                "SomeName",
                "xsd:string"
        ));
        GetScriptVersion getter = new GetScriptVersion(acsParameters, cpeParameters);

        // When:
        getter = getter.build();

        // Then:
        assertNull(getter.getScriptVersion());
        assertNull(getter.getScriptName());
    }

    @Test
    public void scriptVersionIsFoundWhenThereIsVersionParameterInCpeParameters() {
        // Given:
        ACSParameters acsParameters = new ACSParameters();
        acsParameters.putPvs("System.X_FREEACS-COM.TR069Script.SomeName.Version", new ParameterValueStruct(
                "Device.DeviceInfo.VendorConfigFile.1.Version",
                "versionFromDB",
                "xsd:string"
        ));
        CPEParameters cpeParameters = new CPEParameters("Device.");
        cpeParameters.getCpeParams().put("Device.DeviceInfo.VendorConfigFile.1.Name", new ParameterValueStruct(
                "Device.DeviceInfo.VendorConfigFile.1.Name",
                "SomeName",
                "xsd:string"
        ));
        cpeParameters.getCpeParams().put("Device.DeviceInfo.VendorConfigFile.1.Version", new ParameterValueStruct(
                "Device.DeviceInfo.VendorConfigFile.1.Version",
                "versionFromCPE",
                "xsd:string"
        ));
        GetScriptVersion getter = new GetScriptVersion(acsParameters, cpeParameters);

        // When:
        getter = getter.build();

        // Then:
        assertEquals("versionFromDB", getter.getScriptVersion());
        assertEquals("SomeName", getter.getScriptName());
    }
}
