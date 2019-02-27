package com.github.freeacs.tr069;

import com.github.freeacs.base.ACSParameters;
import com.github.freeacs.tr069.xml.ParameterValueStruct;
import org.junit.Test;

import static org.junit.Assert.*;

public class GetScriptVersionTest {

    @Test
    public void scriptVersionIsNotFoundWhenThereIsNoVersionParameterInCpeParameters() {
        // Given:
        ACSParameters acsParameters = new ACSParameters();
        acsParameters.putPvs("System.X_FREEACS-COM.TR069Script.Device.DeviceInfo.VendorConfigFile.1.Version.Version", new ParameterValueStruct(
                "Device.DeviceInfo.VendorConfigFile.1.Version",
                "1",
                "xsd:string"
        ));
        CPEParameters cpeParameters = new CPEParameters("Device.");
        cpeParameters.putPvs("Device.DeviceInfo.VendorConfigFile.1.Name", new ParameterValueStruct(
                "Device.DeviceInfo.VendorConfigFile.1.Name",
                "Current ROS configuration",
                "xsd:string"
        ));
        GetScriptVersion getter = new GetScriptVersion(acsParameters, cpeParameters);

        // When:
        getter = getter.build();

        // Then:
        assertNull(getter.getScriptVersion());
        assertNull(getter.getScriptName());
    }
}