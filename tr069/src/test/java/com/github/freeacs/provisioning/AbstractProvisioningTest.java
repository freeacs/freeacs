package com.github.freeacs.provisioning;

import com.github.freeacs.dbi.*;
import com.github.freeacs.dbi.util.SystemParameters;
import com.github.freeacs.utils.RestTemplateConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpCookie;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static com.github.freeacs.dbi.Unittype.ProvisioningProtocol.TR069;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.xml.HasXPath.hasXPath;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("WeakerAccess")
@Import(RestTemplateConfig.class)
public abstract class AbstractProvisioningTest {
    public static final String UNIT_ID = "test123";
    public static final String UNIT_TYPE_NAME = "Test";
    public static final String UNIT_PASSWORD = "password";
    public static final String PROFILE_NAME = "Default";
    public static final String UNIT_ID_AUTO = "000000-FakeProductClass-FakeSerialNumber";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected DBI dbi;

    @Autowired
    protected RestTemplate restTemplate;

    protected void addUnitsToProvision() throws SQLException {
        addUnitsToProvision(dbi, UNIT_TYPE_NAME, UNIT_ID);
    }

    public static void addUnitsToProvision(DBI dbi, String unitTypeName, String unitId) throws SQLException {
        Unittype unittype = addUnittype(dbi, unitTypeName);

        ACSUnit acsUnit = dbi.getACSUnit();

        acsUnit.addUnits(Collections.singletonList(unitId), unittype.getProfiles().getByName(PROFILE_NAME));
        Unit unit = acsUnit.getUnitById(unitId);
        acsUnit.addOrChangeUnitParameter(unit, SystemParameters.SECRET, UNIT_PASSWORD);
    }

    public static Unittype addUnittype(DBI dbi, String unitTypeName) throws SQLException {
        ACSUnit acsUnit = dbi.getACSUnit();
        Unittypes unittypes = dbi.getAcs().getUnittypes();
        Unittype unittype = unittypes.getByName(unitTypeName);
        if (unittype != null) {
            Unit unit = acsUnit.getUnitById(UNIT_ID);
            if (unit != null) {
                acsUnit.deleteUnit(unit);
            }
            unit = acsUnit.getUnitById(UNIT_ID_AUTO);
            if (unit != null) {
                acsUnit.deleteUnit(unit);
            }
            unittypes.deleteUnittype(unittype, dbi.getAcs(), true);
        }
        unittypes.addOrChangeUnittype(new Unittype(unitTypeName, "","", TR069), dbi.getAcs());
        unittype = unittypes.getByName(unitTypeName);
        unittype.getUnittypeParameters().addOrChangeUnittypeParameter(new UnittypeParameter(unittype, "InternetGatewayDevice.ManagementServer.PeriodicInformInterval", new UnittypeParameterFlag("RW")), dbi.getAcs());
        return unittype;
    }

    protected Element parseXmlResponse(ResponseEntity<String> response) throws SAXException, IOException, ParserConfigurationException {
        return DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream(response.getBody().getBytes()))
                .getDocumentElement();
    }

    protected ResponseEntity<String> sendRequestWithHeaders(String url, HttpMethod method, HttpEntity<String> entity) {
        return restTemplate.exchange(url, method, entity, String.class);
    }

    protected void updateHeadersWithCookies(HttpHeaders headers, ResponseEntity<String> response) {
        List<HttpCookie> cookies = HttpCookie.parse(response.getHeaders().getFirst("Set-Cookie"));
        headers.set("Cookie", cookies.get(0).toString());
    }

    protected void assertGetParameterValuesRequest(ResponseEntity<String> response) throws Exception {
        assertNotNull(response.getBody());
        Element xml = parseXmlResponse(response);
        assertThat(xml, hasXPath("/*[local-name() = 'Envelope']" +
                        "/*[local-name() = 'Body']" +
                        "/*[local-name() = 'GetParameterValues']" +
                        "/ParameterNames" +
                        "/string[1]",
                is("InternetGatewayDevice.DeviceInfo.VendorConfigFile.")));
        assertThat(xml, hasXPath("/*[local-name() = 'Envelope']" +
                        "/*[local-name() = 'Body']" +
                        "/*[local-name() = 'GetParameterValues']" +
                        "/ParameterNames" +
                        "/string[2]",
                is("InternetGatewayDevice.ManagementServer.PeriodicInformInterval")));
    }

    protected void assertInformResponse(ResponseEntity<String> response) throws IOException, ParserConfigurationException, SAXException {
        assertNotNull(response.getBody());
        Element xml = parseXmlResponse(response);
        assertThat(xml, hasXPath("/*[local-name() = 'Envelope']" +
                "/*[local-name() = 'Body']" +
                "/*[local-name() = 'InformResponse']" +
                "/MaxEnvelopes", is("1")));
    }

    protected void assertSetParameterValuesRequest(ResponseEntity<String> response) throws IOException, ParserConfigurationException, SAXException {
        assertNotNull(response.getBody());
        Element xml = parseXmlResponse(response);
        assertThat(xml, hasXPath("/*[local-name() = 'Envelope']" +
                "/*[local-name() = 'Body']" +
                "/*[local-name() = 'SetParameterValues']" +
                "/ParameterList" +
                "/ParameterValueStruct" +
                "/Name", is("InternetGatewayDevice.ManagementServer.PeriodicInformInterval")));
    }

    protected void assertSessionEndResponse(ResponseEntity<String> response) {
        assertEquals(204, response.getStatusCode().value());
        assertNull(response.getBody());
    }

    protected void assertSessionInvalidated(ResponseEntity<String> response) {
        assertEquals(204, response.getStatusCode().value());
        assertNull(response.getBody());
    }
}
