package com.github.freeacs.web;

import org.springframework.security.core.userdetails.UserDetails;

public abstract class ThreadUser {

    private static ThreadLocal<UserDetails> userDetails = new ThreadLocal<>();

    public static void setUserDetails(UserDetails userDetails) {
        ThreadUser.userDetails.set(userDetails);
    }

    public static UserDetails getUserDetails() {
        return ThreadUser.userDetails.get();
    }
}
