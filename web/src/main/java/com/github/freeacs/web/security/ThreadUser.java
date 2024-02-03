package com.github.freeacs.web.security;

public abstract class ThreadUser {

  private static final ThreadLocal<WebUser> userDetails = new ThreadLocal<>();

  public static void setUserDetails(WebUser userDetails) {
    ThreadUser.userDetails.set(userDetails);
  }

  public static WebUser getUserDetails() {
    return ThreadUser.userDetails.get();
  }
}
