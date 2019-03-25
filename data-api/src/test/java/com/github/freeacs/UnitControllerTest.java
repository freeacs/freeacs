package com.github.freeacs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freeacs.dao.Profile;
import com.github.freeacs.service.ProfileDto;
import com.github.freeacs.service.UnitDto;
import com.github.freeacs.service.UnitTypeDto;
import com.hazelcast.core.HazelcastInstance;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.freeacs.ProfileControllerTest.createProfile;
import static com.github.freeacs.UnitTypeControllerTest.createUnitType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class UnitControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Autowired
    private Jdbi jdbi;

    @Before
    public void init() {
        hazelcastInstance.getMap("unitTypesById").removeAll(o -> true);
        hazelcastInstance.getMap("unitTypesByName").removeAll(o -> true);
        hazelcastInstance.getMap("profilesById").removeAll(o -> true);
        jdbi.withHandle(handle -> handle.createUpdate("DROP ALL OBJECTS").execute());
    }

    @Test
    public void shouldFailWith401() throws Exception {
        mockMvc.perform(post("/unit")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", password = "freeacs", roles = "USER")
    public void shouldBeAbleToCreateProfile() throws Exception {
        UnitTypeDto unitType = createUnitType(mockMvc, objectMapper);
        ProfileDto profile = createProfile(mockMvc, objectMapper, unitType);
        createUnit(mockMvc, objectMapper, profile, unitType);
    }

    public static UnitDto createUnit(MockMvc mockMvc, ObjectMapper objectMapper, ProfileDto profile, UnitTypeDto unitType) throws Exception {
        UnitDto request = new UnitDto("123", profile, unitType);
        mockMvc.perform(post("/unit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(request)));
        return request;
    }

}
