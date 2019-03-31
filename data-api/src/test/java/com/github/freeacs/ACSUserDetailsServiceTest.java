package com.github.freeacs;

import com.github.freeacs.config.ACSUserDetailsService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ACSUserDetailsServiceTest extends BaseTest  {

    @Autowired
    private ACSUserDetailsService userService;

    @Test
    public void shouldLoadUserThatExist() {
        assertNotNull(userService.loadUserByUsername("admin"));
    }

    @Test(expected = UsernameNotFoundException.class)
    public void shouldFailToLoadUserThatDoesNotExist() {
        userService.loadUserByUsername("doesnotexist");
    }
}
