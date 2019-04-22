package com.github.freeacs.tr069;

import com.github.freeacs.tr069.xml.ParameterValueStruct;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class CPEParametersTest {

    @Test
    public void shouldNotFailWithNoParameters() {
        // Given:
        CPEParameters cpeParameters = new CPEParameters("Device.");
        // When:
        Map<String, String> result = cpeParameters.getConfigFileMap();
        // Then:
        assertNotNull(result);
    }

    @Test
    public void shouldNotFailOnMissingVersion() {
        // Given:
        CPEParameters cpeParameters = new CPEParameters("Device.");
        cpeParameters.getCpeParams().put("Device.DeviceInfo.VendorConfigFile.1.Name", new ParameterValueStruct(
                "Device.DeviceInfo.VendorConfigFile.1.Name",
                "Current ROS configuration",
                "xsd:string"
        ));
        cpeParameters.getCpeParams().put("Device.DeviceInfo.VendorConfigFile.1.Description", new ParameterValueStruct(
                "Device.DeviceInfo.VendorConfigFile.1.Description",
                "Currently applied ROS configuration which will return dynamic /export output when Uploaded.",
                "xsd:string"
        ));
        cpeParameters.getCpeParams().put("Device.DeviceInfo.VendorConfigFile.1.UseForBackupRestore", new ParameterValueStruct(
                "Device.DeviceInfo.VendorConfigFile.1.UseForBackupRestore",
                "0",
                "xsd:boolean"
        ));
        // When:
        assertNotNull(cpeParameters.getConfigFileMap());
    }

    @Test
    public void shouldReturnVersion() {
        // Given:
        CPEParameters cpeParameters = new CPEParameters("Device.");
        cpeParameters.getCpeParams().put("Device.DeviceInfo.VendorConfigFile.1.Name", new ParameterValueStruct(
                "Device.DeviceInfo.VendorConfigFile.1.Name",
                "SomeName",
                "xsd:string"
        ));
        cpeParameters.getCpeParams().put("Device.DeviceInfo.VendorConfigFile.1.Version", new ParameterValueStruct(
                "Device.DeviceInfo.VendorConfigFile.1.Version",
                "123",
                "xsd:string"
        ));
        cpeParameters.getCpeParams().put("Device.DeviceInfo.VendorConfigFile.1.Description", new ParameterValueStruct(
                "Device.DeviceInfo.VendorConfigFile.1.Description",
                "Currently applied ROS configuration which will return dynamic /export output when Uploaded.",
                "xsd:string"
        ));
        cpeParameters.getCpeParams().put("Device.DeviceInfo.VendorConfigFile.1.UseForBackupRestore", new ParameterValueStruct(
                "Device.DeviceInfo.VendorConfigFile.1.UseForBackupRestore",
                "0",
                "xsd:boolean"
        ));
        // When:
        assertNotNull(cpeParameters.getConfigFileMap());
        assertEquals("123", cpeParameters.getConfigFileMap().get("SomeName"));
    }
}
