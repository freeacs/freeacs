package com.github.freeacs;

import com.github.freeacs.config.ACSUserDetailsService;
import com.github.freeacs.controllers.ProfileController;
import com.github.freeacs.controllers.UnitController;
import com.github.freeacs.controllers.UnitTypeController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTest {

    @Autowired
    private UnitTypeController unitTypeController;
    @Autowired
    private ProfileController profileController;
    @Autowired
    private UnitController unitController;
    @Autowired
    private ACSUserDetailsService userService;

    @Test
    public void contextLoads() {
        assertThat(unitTypeController).isNotNull();
        assertThat(profileController).isNotNull();
        assertThat(unitController).isNotNull();
        assertThat(userService).isNotNull();
    }

}
