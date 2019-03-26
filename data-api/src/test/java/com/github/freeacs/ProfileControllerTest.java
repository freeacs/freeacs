package com.github.freeacs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freeacs.service.ProfileDto;
import com.github.freeacs.service.UnitTypeDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.freeacs.UnitTypeControllerTest.createUnitType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ProfileControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void shouldFailWith401() throws Exception {
        mockMvc.perform(post("/profile")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", password = "freeacs", roles = "USER")
    public void shouldBeAbleToCreateProfile() throws Exception {
        UnitTypeDto unitType = createUnitType(mockMvc, objectMapper);
        createProfile(mockMvc, objectMapper, unitType);
    }

    public static ProfileDto createProfile(MockMvc mockMvc, ObjectMapper objectMapper, UnitTypeDto unitType) throws Exception {
        ProfileDto request = new ProfileDto(null, "Test", unitType);
        ProfileDto withId = request.withId(1L);
        mockMvc.perform(post("/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(withId)));
        return withId;
    }

}
