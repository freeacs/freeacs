package com.github.freeacs.controllers;

import com.github.freeacs.config.UserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/details")
    public UserPrincipal login(Principal principal) {
        return (UserPrincipal) ((UsernamePasswordAuthenticationToken)principal).getPrincipal();
    }
}
