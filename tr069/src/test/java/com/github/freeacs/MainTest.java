package com.github.freeacs;

import com.github.freeacs.common.util.FileSlurper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import java.io.IOException;
import java.sql.SQLException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = Main.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = {
        "classpath:application.properties",
        "classpath:application-no-security.properties",
        "classpath:application-discovery-mode.properties"
})
public class MainTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private DataSource dataSource;

    @Before
    public void init() throws SQLException, IOException {
        ValueInsertHelper.insert(dataSource);
    }

    @Test
    public void getEmptyResponseOnEmptyRequest() throws Exception {
        mvc.perform(post("/tr069")).andExpect(status().isNoContent());
    }

    @Test
    public void getInformResponseOnInformRequest() throws Exception {
        mvc.perform(post("/tr069")
                .content(FileSlurper.getFileAsString("/provision/cpe/Inform.xml")))
                .andExpect(status().isOk())
                .andExpect(
                        xpath("/*[local-name() = 'Envelope']/*[local-name() = 'Body']/*[local-name() = 'InformResponse']/MaxEnvelopes")
                                .string("1"));
    }

}
