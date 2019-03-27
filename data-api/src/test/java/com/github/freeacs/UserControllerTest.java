package com.github.freeacs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin", password = "freeacs", roles = "USER")
    public void getsUserDetailsWhenLoggedIn() throws Exception {
        // This test uses mock user, and expects mock user details
        mockMvc.perform(get("/user/details")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"password\":\"freeacs\",\"username\":\"admin\"," +
                                "\"authorities\":[{\"authority\":\"ROLE_USER\"}]," +
                                "\"accountNonExpired\":true,\"accountNonLocked\":true," +
                                "\"credentialsNonExpired\":true,\"enabled\":true}"));
    }

    @Test
    public void getsUserDetailsWhenAuthenticates() throws Exception {
        // this test expects actual user details
        mockMvc.perform(get("/user/details")
                .header("Authorization", "Basic YWRtaW46ZnJlZWFjcw==")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"enabled\":true,\"authorities\":[{\"authority\":\"Admin\"}],\"username\":\"admin\"," +
                                "\"accountNonExpired\":true,\"accountNonLocked\":true,\"credentialsNonExpired\":true}"));
    }
}
