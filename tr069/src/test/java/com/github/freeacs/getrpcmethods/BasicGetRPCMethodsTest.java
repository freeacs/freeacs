package com.github.freeacs.getrpcmethods;

import com.github.freeacs.Main;
import com.github.freeacs.provisioning.AbstractProvisioningTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.SQLException;

import static com.github.freeacs.provisioning.AbstractProvisioningTest.UNIT_ID;
import static com.github.freeacs.provisioning.AbstractProvisioningTest.UNIT_PASSWORD;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = Main.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = {
        "classpath:application.properties",
        "classpath:application-h2-datasource.properties",
        "classpath:application-basic-security.properties",
        "classpath:application-discovery-off.properties"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BasicGetRPCMethodsTest extends AbstractGetRPCMethodsTest {

    @BeforeEach
    public void init() throws SQLException {
        AbstractProvisioningTest.addUnitsToProvision(dbi);
    }

    @Test
    public void canAskForRPCMethods() throws Exception {
        getRPCMethods(httpBasic(UNIT_ID, UNIT_PASSWORD), mvc);
    }
}
