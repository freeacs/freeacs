package com.github.freeacs.service;

import io.vavr.collection.List;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserDtoTest {

    @Test
    public void isAdmin() {
        UserDto user = new UserDto(null, "admin", null, null, null, List.empty());
        assertTrue(user.isAdmin());
    }

    @Test
    public void isNotAdmin() {
        UserDto user = new UserDto(null, "notadmin", null, null, null, List.empty());
        assertFalse(user.isAdmin());
    }

    @Test
    public void hasAdminPermission() {
        UserDto user = new UserDto(null, "notadmin", null, null, "Admin", List.empty());
        assertTrue(user.hasAdminPermission());
    }

    @Test
    public void hasNotAdminPermission() {
        UserDto user = new UserDto(null, "notadmin", null, null, "somethingelse", List.empty());
        assertFalse(user.hasAdminPermission());
    }

    @Test
    public void getWebAccessList() {
        UserDto user = new UserDto(null, "notadmin", null, null, "WEB[some,thing]", List.empty());
        assertEquals(List.of("some", "thing"), user.getAccessList());
    }

    @Test
    public void getOtherAccessList() {
        UserDto user = new UserDto(null, "notadmin", null, null, "some,thing,else", List.empty());
        assertEquals(List.of("some,thing,else"), user.getAccessList());
    }

    @Test
    public void getEmptyAccessList() {
        UserDto user = new UserDto(null, "notadmin", null, null, null, List.empty());
        assertEquals(List.empty(), user.getAccessList());
    }
}
