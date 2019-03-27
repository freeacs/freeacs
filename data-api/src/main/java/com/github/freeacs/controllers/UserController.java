package com.github.freeacs.controllers;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/details")
    public Object details(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken) {
            return ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        }
        return principal;
    }
}
