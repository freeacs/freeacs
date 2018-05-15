package com.github.freeacs.tr069.methods;


import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HttpRequestProcessorTest {

    @Test
    public void testNormalResponse() {
        // Given:
        String rebootResponse = "<soap:Body><cwmp:RebootResponse><Irreleevant /></cwmp:RebootResponse></soap:Body>";

        // When:
        String methodName = HTTPRequestProcessor.extractMethodName(rebootResponse);

        // Then:
        assertEquals("Reboot", methodName);
    }

    @Test
    public void testClosedElement() {
        // Given:
        String rebootResponse = "<soap:Body><cwmp:RebootResponse/></soap:Body>";

        // When:
        String methodName = HTTPRequestProcessor.extractMethodName(rebootResponse);

        // Then:
        assertEquals("Reboot", methodName);
    }


    @Test
    public void testNoXml() {
        // Given:
        String rebootResponse = "";

        // When:
        String methodName = HTTPRequestProcessor.extractMethodName(rebootResponse);

        // Then:
        assertNull(methodName);
    }

    @Test
    public void testRealshit() {
        // Given:
        String rebootResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                " <soap_env:Envelope\n" +
                "xmlns:soap_env=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "xmlns:soap_enc=\"http://schemas.xmlsoap.org/soap/encoding/\"\n" +
                "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "xmlns:cwmp=\"urn:dslforum-org:cwmp-1-2\">\n" +
                "  <soap_env:Header>\n" +
                "   <cwmp:ID soap_env:mustUnderstand=\"1\">18</cwmp:ID>\n" +
                "  </soap_env:Header>\n" +
                "  <soap_env:Body>\n" +
                "   <cwmp:Inform>\n" +
                "    <DeviceId>\n" +
                "     <Manufacturer>easycwmp</Manufacturer>\n" +
                "     <OUI>FFFFFF</OUI>\n" +
                "     <ProductClass>easycwmp</ProductClass>\n" +
                "     <SerialNumber>FFFFFF123456</SerialNumber>\n" +
                "    </DeviceId>\n" +
                "    <Event soap_enc:arrayType=\"cwmp:EventStruct[1]\">\n" +
                "     <EventStruct>\n" +
                "      <EventCode>2 PERIODIC</EventCode>\n" +
                "      <CommandKey />\n" +
                "     </EventStruct>\n" +
                "    </Event>\n" +
                "    <MaxEnvelopes>1</MaxEnvelopes>\n" +
                "    <CurrentTime>2018-05-13T18:16:44+00:00</CurrentTime>\n" +
                "    <RetryCount>0</RetryCount>\n" +
                "    <ParameterList soap_enc:arrayType=\"cwmp:ParameterValueStruct[10]\">\n" +
                "     <ParameterValueStruct>\n" +
                "      <Name>Device.DeviceInfo.HardwareVersion</Name>\n" +
                "      <Value xsi:type=\"xsd:string\">example_hw_version</Value>\n" +
                "     </ParameterValueStruct>\n" +
                "     <ParameterValueStruct>\n" +
                "      <Name>Device.DeviceInfo.Manufacturer</Name>\n" +
                "      <Value xsi:type=\"xsd:string\">easycwmp</Value>\n" +
                "     </ParameterValueStruct>\n" +
                "     <ParameterValueStruct>\n" +
                "      <Name>Device.DeviceInfo.ManufacturerOUI</Name>\n" +
                "      <Value xsi:type=\"xsd:string\">FFFFFF</Value>\n" +
                "     </ParameterValueStruct>\n" +
                "     <ParameterValueStruct>\n" +
                "      <Name>Device.DeviceInfo.ProductClass</Name>\n" +
                "      <Value xsi:type=\"xsd:string\">easycwmp</Value>\n" +
                "     </ParameterValueStruct>\n" +
                "     <ParameterValueStruct>\n" +
                "      <Name>Device.DeviceInfo.ProvisioningCode</Name>\n" +
                "      <Value xsi:type=\"xsd:string\"></Value>\n" +
                "     </ParameterValueStruct>\n" +
                "     <ParameterValueStruct>\n" +
                "      <Name>Device.DeviceInfo.SerialNumber</Name>\n" +
                "      <Value xsi:type=\"xsd:string\">FFFFFF123456</Value>\n" +
                "     </ParameterValueStruct>\n" +
                "     <ParameterValueStruct>\n" +
                "      <Name>Device.DeviceInfo.SoftwareVersion</Name>\n" +
                "      <Value xsi:type=\"xsd:string\">example_sw_version</Value>\n" +
                "     </ParameterValueStruct>\n" +
                "     <ParameterValueStruct>\n" +
                "      <Name>Device.DeviceInfo.SpecVersion</Name>\n" +
                "      <Value xsi:type=\"xsd:string\">1.0</Value>\n" +
                "     </ParameterValueStruct>\n" +
                "     <ParameterValueStruct>\n" +
                "      <Name>Device.ManagementServer.ConnectionRequestURL</Name>\n" +
                "      <Value xsi:type=\"xsd:string\"></Value>\n" +
                "     </ParameterValueStruct>\n" +
                "     <ParameterValueStruct>\n" +
                "      <Name>Device.ManagementServer.ParameterKey</Name>\n" +
                "      <Value xsi:type=\"xsd:string\"></Value>\n" +
                "     </ParameterValueStruct>\n" +
                "    </ParameterList>\n" +
                "   </cwmp:Inform>\n" +
                "  </soap_env:Body>\n" +
                " </soap_env:Envelope>\n";

        // When:
        String methodName = HTTPRequestProcessor.extractMethodName(rebootResponse);

        // Then:
        assertEquals("Inform", methodName);
    }

    @Test
    public void testOtherCornerCase() {
        // Given:
        String informReq = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/informReq/envelope/\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/informReq/encoding/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n" +
                "<SOAP-ENV:Header>\n" +
                "<cwmp:ID SOAP-ENV:mustUnderstand=\"1\">inform</cwmp:ID>\n" +
                "</SOAP-ENV:Header>\n" +
                "<SOAP-ENV:Body SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/informReq/encoding/\">\n" +
                "<cwmp:Inform>\n" +
                "<DeviceId>\n" +
                "<Manufacturer>Manufact</Manufacturer>\n" +
                "<OUI>00000</OUI>\n" +
                "<ProductClass>fffffff</ProductClass>\n" +
                "<SerialNumber>ffffffffffffffffff</SerialNumber>\n" +
                "</DeviceId>\n" +
                "<Event xsi:type=\"SOAP-ENC:Array\" SOAP-ENC:arrayType=\"cwmp:EventStruct[2]\">\n" +
                "<EventStruct>\n" +
                "<EventCode>0 BOOTSTRAP</EventCode>\n" +
                "<CommandKey></CommandKey>\n" +
                "</EventStruct>\n" +
                "<EventStruct>\n" +
                "<EventCode>1 BOOT</EventCode>\n" +
                "<CommandKey></CommandKey>\n" +
                "</EventStruct>\n" +
                "</Event>\n" +
                "<MaxEnvelopes>1</MaxEnvelopes>\n" +
                "<CurrentTime>2018-05-15T10:44:03</CurrentTime>\n" +
                "<RetryCount>0</RetryCount>\n" +
                "</cwmp:Inform>\n" +
                "</SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>";

        // When:
        String methodName = HTTPRequestProcessor.extractMethodName(informReq);

        // Then:
        assertEquals("Inform", methodName);
    }

    @Test
    public void testPrettyMuchIrregularRequest() {
        // Given:
        String rebootResponse = "<soap:Body ddsd=ssdsdsddd ><cwmp:RebootResponse><Irreleevant /></cwmp:RebootResponse></soap:Body>";

        // When:
        String methodName = HTTPRequestProcessor.extractMethodName(rebootResponse);

        // Then:
        assertEquals("Reboot", methodName);
    }

    @Test
    public void testPrettyMuchIrregularRequestWithLineBreak() {
        // Given:
        String rebootResponse = "<soap:Body ddsd=ssdsdsddd\n ><cwmp:RebootResponse><Irreleevant /></cwmp:RebootResponse></soap:Body>";

        // When:
        String methodName = HTTPRequestProcessor.extractMethodName(rebootResponse);

        // Then:
        assertEquals("Reboot", methodName);
    }

}