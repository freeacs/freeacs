package com.github.freeacs.discover;

import com.github.freeacs.Main;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.util.SystemParameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static com.github.freeacs.provisioning.AbstractProvisioningTest.UNIT_ID_AUTO;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = Main.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = {
        "classpath:application.properties",
        "classpath:application-h2-datasource.properties",
        "classpath:application-no-security.properties",
        "classpath:application-discovery-on.properties"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AutoDiscoverTest extends AbstractDiscoverTest {

    @Test
    public void noContentOnMissingAuthentication() throws Exception {
        mvc.perform(post("/tr069")).andExpect(status().isNoContent());
    }

    @Test
    public void unitAndUnittypeIsDiscovered() throws Exception {
        discoverUnit();
        ACSUnit acsUnit = dbi.getACSUnit();
        Unit unit = acsUnit.getUnitById(UNIT_ID_AUTO);
        assertEquals("FakeProductClass", unit.getUnittype().getName());
        assertEquals("Default", unit.getProfile().getName());
        String discoverValue = unit.getUnitParameters().get(SystemParameters.SECRET).getValue();
        // Secret parameter is added, but populated with blank value
        assertEquals("", discoverValue);
    }
}
