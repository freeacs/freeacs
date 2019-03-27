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
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    public void failsToGetUserDetails() throws Exception {
        // This test uses mock user, and expects mock user details
        mockMvc.perform(get("/user/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", password = "freeacs", roles = "USER")
    public void getsUserDetailsWhenLoggedIn() throws Exception {
        // This test uses mock user, and expects mock user details
        mockMvc.perform(get("/user/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"roles\":[\"ROLE_USER\"],\"username\":\"admin\"}"));
    }

    @Test
    public void getsUserDetailsWhenAuthenticates() throws Exception {
        MvcResult result = mockMvc.perform(post("/user/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"username\": \"admin\", \"password\": \"freeacs\" }"))
                .andReturn();
        HashMap<String, String> myMap = objectMapper.readValue(result.getResponse().getContentAsString(), HashMap.class);
        String token = myMap.get("token");
        // this test expects actual user details
        mockMvc.perform(get("/user/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"roles\":[\"Admin\"],\"username\":\"admin\"}"));
    }
}
